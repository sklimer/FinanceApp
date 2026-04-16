package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel для добавления транзакций (AddTransactionActivity)
 * Управляет добавлением и валидацией транзакций
 */
class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    
    // StateFlow для состояния загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // StateFlow для сообщений об ошибках
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // StateFlow для успешного добавления транзакции
    private val _transactionSuccess = MutableStateFlow(false)
    val transactionSuccess: StateFlow<Boolean> = _transactionSuccess.asStateFlow()

    /**
     * Добавление транзакции
     * @param email Email пользователя
     * @param type Тип транзакции (income/expense)
     * @param amount Сумма транзакции
     * @param category Категория транзакции
     * @param date Дата транзакции
     */
    fun addTransaction(
        email: String,
        type: String,
        amount: Double,
        category: String,
        date: Date
    ) {
        // Валидация суммы
        if (!validateAmount(amount)) {
            _errorMessage.value = "Введите корректную сумму (больше 0)"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                db.transactionDao().insertTransaction(
                    Transaction(
                        userEmail = email,
                        type = type,
                        amount = amount,
                        category = category,
                        date = date
                    )
                )
                _transactionSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка добавления транзакции: ${e.message}"
                _transactionSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Валидация суммы транзакции
     */
    fun validateAmount(amount: Double): Boolean {
        return amount > 0
    }

    /**
     * Проверка, не пустая ли строка
     */
    fun isNotEmpty(value: String): Boolean {
        return value.isNotBlank()
    }

    /**
     * Сброс состояния ошибки
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Сброс состояния успеха
     */
    fun resetTransactionSuccess() {
        _transactionSuccess.value = false
    }
}
