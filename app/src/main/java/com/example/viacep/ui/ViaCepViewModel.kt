package com.example.viacep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viacep.data.repository.Result
import com.example.viacep.data.repository.ViaCepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViaCepViewModel : ViewModel() {
    private val repository = ViaCepRepository()

    private val _enderecoState = MutableStateFlow<EnderecoState>(EnderecoState.Idle)
    val enderecoState: StateFlow<EnderecoState> = _enderecoState

    fun buscarEndereco(cep: String) {
        val cepLimpo = cep.replace("[-\\s]".toRegex(), "")

        viewModelScope.launch {
            _enderecoState.value = EnderecoState.Loading

            when (val result = repository.buscarEndereco(cepLimpo)) {
                is Result.Success -> {
                    _enderecoState.value = EnderecoState.Success(result.data)
                }
                is Result.Error -> {
                    _enderecoState.value = EnderecoState.Error("Erro ao buscar endereço: ${result.exception.message}")
                }
            }
        }
    }
}

sealed class EnderecoState {
    object Idle : EnderecoState()
    object Loading : EnderecoState()
    data class Success(val endereco: com.example.viacep.data.model.Endereco) : EnderecoState()
    data class Error(val message: String) : EnderecoState()
}