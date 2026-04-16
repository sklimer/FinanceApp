package com.example.personalfinanceapp.domain.model

import java.util.Date

/**
 * Модель транзакции в доменном слое
 */
data class TransactionDomain(
    val id: Long = 0,
    val userEmail: String,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val date: Date
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
