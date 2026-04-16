package com.example.personalfinanceapp.repository

import com.example.personalfinanceapp.data.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для транзакций
 * Определяет контракт для работы с данными транзакций
 */
interface TransactionRepository {
    fun getCurrentUserEmail(): Flow<String?>
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    fun getTransactionsForUser(email: String): Flow<List<Transaction>>
    suspend fun getBalance(email: String): Double?
    suspend fun deleteTransaction(transactionId: Long): Result<Unit>
}
