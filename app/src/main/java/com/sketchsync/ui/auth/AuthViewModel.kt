package com.sketchsync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sketchsync.data.model.AuthState
import com.sketchsync.data.model.User
import com.sketchsync.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // 认证状态
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // UI状态
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var authStateJob: Job? = null
    
    init {
        checkAuthState()
    }
    
    /**
     * 检查认证状态
     */
    private fun checkAuthState() {
        if (authStateJob?.isActive == true) return
        authStateJob = viewModelScope.launch {
            authRepository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    refreshUserProfile(firebaseUser.uid, firebaseUser.email, firebaseUser.displayName)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
    
    /**
     * 邮箱登录
     */
    fun signInWithEmail(email: String, password: String) {
        if (!validateInput(email, password)) return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e)
                    )
                }
        }
    }
    
    /**
     * 邮箱注册
     */
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        if (!validateInput(email, password, displayName)) return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e)
                    )
                }
        }
    }
    
    /**
     * Google登录
     */
    fun signInWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Google sign-in failed: ${e.message}"
                    )
                }
        }
    }
    
    /**
     * 发送密码重置邮件
     */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email address")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Password reset email sent. Please check your inbox."
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e)
                    )
                }
        }
    }
    
    /**
     * 退出登录
     */
    fun signOut() {
        authRepository.signOut()
    }
    
    /**
     * 更新用户资料
     */
    fun updateProfile(displayName: String) {
        val userId = authRepository.currentUserId ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.updateUserProfile(userId, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Profile updated successfully"
                    )
                    // 单次刷新用户信息，避免重复订阅
                    refreshUserProfile(userId, null, null)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Update failed: ${e.message}"
                    )
                }
        }
    }

    private suspend fun refreshUserProfile(
        userId: String,
        email: String?,
        displayName: String?
    ) {
        val profileResult = authRepository.getUserProfile(userId)
        if (profileResult.isSuccess) {
            _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
        } else {
            val fallbackUser = User(
                uid = userId,
                email = email ?: "",
                displayName = displayName ?: "User${userId.take(6)}"
            )
            _authState.value = AuthState.Authenticated(fallbackUser)
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    private fun validateInput(email: String, password: String, displayName: String? = null): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Invalid email format")
                return false
            }
            password.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter your password")
                return false
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
                return false
            }
            displayName != null && displayName.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Please enter a username")
                return false
            }
        }
        return true
    }
    
    private fun mapFirebaseError(e: Throwable): String {
        val message = e.message ?: "Unknown error"
        return when {
            message.contains("INVALID_EMAIL") -> "Invalid email format"
            message.contains("WRONG_PASSWORD") || message.contains("INVALID_LOGIN_CREDENTIALS") -> "Incorrect email or password"
            message.contains("USER_NOT_FOUND") -> "User not found"
            message.contains("EMAIL_EXISTS") || message.contains("EMAIL_ALREADY_IN_USE") -> "This email is already registered"
            message.contains("WEAK_PASSWORD") -> "Password is too weak"
            message.contains("NETWORK") -> "Network connection failed. Please check your connection."
            message.contains("TOO_MANY_REQUESTS") -> "Too many requests. Please try again later."
            else -> message
        }
    }
}


data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)
