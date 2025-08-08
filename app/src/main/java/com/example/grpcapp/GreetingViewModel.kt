package com.example.grpcapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grpcapp.com.example.grpcapp.GrpcClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GreetingViewModel : ViewModel() {
    private val grpcClient = GrpcClient()

    private val _uiState = MutableStateFlow(GreetingUiState())
    val uiState: StateFlow<GreetingUiState> = _uiState

    init {
        // Für Android Emulator: 10.0.2.2 zeigt auf localhost des Host-PCs
        grpcClient.connect("192.168.2.107", 9090)
    }

    fun sendGreeting(name: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val response = grpcClient.sayHello(name)
            _uiState.value = _uiState.value.copy(
                response = response,
                isLoading = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        grpcClient.disconnect()
    }
}

data class GreetingUiState(
    val response: String = "",
    val isLoading: Boolean = false
)