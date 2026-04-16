package com.example.personalfinanceapp.domain.model

/**
 * Модель пользователя в доменном слое
 */
data class UserDomain(
    val email: String,
    val passwordHash: String
)
