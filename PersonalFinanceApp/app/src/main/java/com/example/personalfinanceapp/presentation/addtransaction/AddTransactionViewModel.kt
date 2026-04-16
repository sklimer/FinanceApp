package com.example.personalfinanceapp.presentation.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.Transaction
import com.example.personalfinanceapp.repository.AuthRepository
import com.example.personalfinanceapp.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val category: String = "",
    val type: String = "expense",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val transactionAdded: Boolean = false,
    val currentUserEmail: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserEmail().collect { email ->
                _uiState.value = _uiState.value.copy(currentUserEmail = email)
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, errorMessage = null)
    }

    fun onCategoryChange(category: String) {
        _uiState.value = _uiState.value.copy(category = category, errorMessage = null)
    }

    fun onTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun addTransaction() {
        val amountValue = _uiState.value.amount.toDoubleOrNull()
        
        if (amountValue == null || amountValue <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Введите корректную сумму")
            return
        }

        if (_uiState.value.category.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Выберите категорию")
            return
        }

        val email = _uiState.value.currentUserEmail
        if (email == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Пользователь не авторизован")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val transaction = Transaction(
                userEmail = email,
                type = _uiState.value.type,
                amount = amountValue,
                category = _uiState.value.category,
                date = Date()
            )
            
            val result = transactionRepository.addTransaction(transaction)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                transactionAdded = result.isSuccess,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetTransactionAdded() {
        _uiState.value = _uiState.value.copy(
            transactionAdded = false,
            amount = "",
            category = ""
        )
    }
}
