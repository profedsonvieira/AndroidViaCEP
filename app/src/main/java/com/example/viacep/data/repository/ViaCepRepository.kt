package com.example.viacep.data.repository

import com.example.viacep.data.model.Endereco
import com.example.viacep.data.network.RetrofitClient

class ViaCepRepository {
    private val viaCepService = RetrofitClient.instance

    suspend fun buscarEndereco(cep: String): Result<Endereco> {
        return try {
            val endereco = viaCepService.buscarEndereco(cep)
            Result.Success(endereco)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}