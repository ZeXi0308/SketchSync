package com.sketchsync.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sketchsync.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /**
     * 获取当前用户
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    /**
     * 获取当前用户ID
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid
    
    /**
     * 监听认证状态变化
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    /**
     * 邮箱密码登录
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                updateLastLogin(user.uid)
                Result.success(user)
            } ?: Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 邮箱密码注册
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // 创建用户资料
                createUserProfile(user.uid, email, displayName)
                Result.success(user)
            } ?: Result.failure(Exception("Registration failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Google登录
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { user ->
                // 检查是否是新用户，如果是则创建资料
                if (result.additionalUserInfo?.isNewUser == true) {
                    createUserProfile(
                        user.uid,
                        user.email ?: "",
                        user.displayName ?: "User${user.uid.take(6)}"
                    )
                } else {
                    updateLastLogin(user.uid)
                }
                Result.success(user)
            } ?: Result.failure(Exception("Google sign-in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 退出登录
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * 发送密码重置邮件
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户资料
     */
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: emptyMap()
                Result.success(User.fromMap(data, userId))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取当前用户资料
     */
    suspend fun getCurrentUserProfile(): Result<User> {
        val userId = currentUserId ?: return Result.failure(Exception("User is not logged in"))
        return getUserProfile(userId)
    }
    
    /**
     * 更新用户资料
     */
    suspend fun updateUserProfile(userId: String, displayName: String, avatarUrl: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName
            )
            avatarUrl?.let { updates["avatarUrl"] = it }
            
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建用户资料
     */
    private suspend fun createUserProfile(userId: String, email: String, displayName: String) {
        val user = User(
            uid = userId,
            email = email,
            displayName = displayName,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        firestore.collection("users").document(userId).set(user.toMap()).await()
    }
    
    /**
     * 更新最后登录时间
     */
    private suspend fun updateLastLogin(userId: String) {
        try {
            firestore.collection("users").document(userId)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            // 忽略更新失败
        }
    }
}
