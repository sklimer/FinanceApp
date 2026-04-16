package com.example.personalfinanceapp.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для аутентификации
 * Определяет контракт для работы с данными пользователей
 */
interface AuthRepository {
    fun getCurrentUserEmail(): Flow<String?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun getUserByEmail(email: String): com.example.personalfinanceapp.data.User?
    fun isValidEmail(email: String): Boolean
    fun isValidPassword(password: String): Boolean
}
