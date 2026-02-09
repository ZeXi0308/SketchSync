package com.sketchsync.ui.canvas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, fontSize: Float) -> Unit,
    currentColor: Int
) {
    var text by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf(FontSize.MEDIUM) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Text",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 文字输入框
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Enter text") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(currentColor),
                        focusedLabelColor = Color(currentColor)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 字号选择
                Text(
                    text = "Font Size",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FontSize.entries.forEach { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { 
                                Text(
                                    text = size.label,
                                    fontSize = size.previewSize.sp
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(currentColor).copy(alpha = 0.2f),
                                selectedLabelColor = Color(currentColor)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onConfirm(text, selectedSize.fontSize)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(currentColor)
                        )
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        }
    }
}


enum class FontSize(val label: String, val fontSize: Float, val previewSize: Int) {
    SMALL("S", 32f, 12),
    MEDIUM("M", 48f, 16),
    LARGE("L", 72f, 20),
    XLARGE("XL", 96f, 24)
}
