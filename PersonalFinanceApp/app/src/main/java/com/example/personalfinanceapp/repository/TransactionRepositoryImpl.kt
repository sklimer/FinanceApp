package com.example.personalfinanceapp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.personalfinanceapp.data.Transaction
import com.example.personalfinanceapp.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация TransactionRepository
 * Управляет транзакциями пользователя
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dataStore: DataStore<Preferences>
) : TransactionRepository {

    companion object {
        private val CURRENT_USER_KEY = stringPreferencesKey("current_user_email")
    }

    override fun getCurrentUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[CURRENT_USER_KEY]
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            if (transaction.amount <= 0) {
                return Result.failure(Exception("Сумма должна быть больше нуля"))
            }
            transactionDao.insertTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            if (transaction.amount <= 0) {
                return Result.failure(Exception("Сумма должна быть больше нуля"))
            }
            transactionDao.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTransactionsForUser(email: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForUser(email)
    }

    override suspend fun getBalance(email: String): Double? {
        return transactionDao.getBalance(email)
    }

    override suspend fun deleteTransaction(transactionId: Long): Result<Unit> {
        return try {
            // Note: You may want to add a delete method to TransactionDao
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
