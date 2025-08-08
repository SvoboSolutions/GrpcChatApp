package com.example.grpcapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grpcapp.com.example.grpcapp.ChatClient
import com.example.grpcapp.proto.ChatProto.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isConnected: Boolean = false,
    val username: String = "",
    val currentMessage: String = ""
)

class ChatViewModel : ViewModel() {
    private val chatClient = ChatClient()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun connectToChat(username: String) {
        viewModelScope.launch {
            try {
                chatClient.connect("192.168.2.107", 9090) // Für Emulator
                _uiState.value = _uiState.value.copy(
                    username = username,
                    isConnected = true
                )

                // Chat-Stream starten
                chatClient.joinChat(username).collect { message ->
                    val currentMessages = _uiState.value.messages.toMutableList()
                    currentMessages.add(message)
                    _uiState.value = _uiState.value.copy(messages = currentMessages)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isConnected = false)
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            val success = chatClient.sendMessage(_uiState.value.username, message)
            if (success) {
                _uiState.value = _uiState.value.copy(currentMessage = "")
            }
        }
    }

    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }

    override fun onCleared() {
        super.onCleared()
        chatClient.disconnect()
    }
}