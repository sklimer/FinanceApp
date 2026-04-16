package com.example.personalfinanceapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.Transaction
import com.example.personalfinanceapp.repository.AuthRepository
import com.example.personalfinanceapp.repository.TransactionRepository
import com.example.personalfinanceapp.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class HomeUiState(
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val currentType: String = "expense",
    val currentPeriod: String = "Месяц",
    val categorySums: Map<String, Double> = emptyMap(),
    val totalSum: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUserEmail: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserEmail().collect { email ->
                _uiState.value = _uiState.value.copy(currentUserEmail = email)
                email?.let {
                    loadBalance(it)
                    loadTransactions(it)
                }
            }
        }
    }

    private fun loadBalance(email: String) {
        viewModelScope.launch {
            val balance = transactionRepository.getBalance(email) ?: 0.0
            _uiState.value = _uiState.value.copy(balance = balance)
        }
    }

    private fun loadTransactions(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository.getTransactionsForUser(email).collect { transactions ->
                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
                filterTransactions(transactions, _uiState.value.currentType, _uiState.value.currentPeriod)
            }
        }
    }

    fun setCurrentType(type: String) {
        _uiState.value = _uiState.value.copy(currentType = type)
        val email = _uiState.value.currentUserEmail ?: return
        viewModelScope.launch {
            val allTransactions = transactionRepository.getTransactionsForUser(email).first()
            filterTransactions(allTransactions, type, _uiState.value.currentPeriod)
        }
    }

    fun setCurrentPeriod(period: String) {
        _uiState.value = _uiState.value.copy(currentPeriod = period)
        val email = _uiState.value.currentUserEmail ?: return
        viewModelScope.launch {
            val allTransactions = transactionRepository.getTransactionsForUser(email).first()
            filterTransactions(allTransactions, _uiState.value.currentType, period)
        }
    }

    private fun filterTransactions(transactions: List<Transaction>, type: String, period: String) {
        val filteredTransactions = transactions.filter { 
            it.type == type && isInPeriod(it.date, period) 
        }
        
        val categorySumsMap = filteredTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        _uiState.value = _uiState.value.copy(
            categorySums = categorySumsMap,
            totalSum = categorySumsMap.values.sum()
        )
    }

    fun isInPeriod(date: Date, period: String): Boolean {
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        val now = java.util.Calendar.getInstance()

        return when (period) {
            "День" -> cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) && 
                     cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
            "Неделя" -> cal.get(java.util.Calendar.WEEK_OF_YEAR) == now.get(java.util.Calendar.WEEK_OF_YEAR) && 
                        cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
            "Месяц" -> cal.get(java.util.Calendar.MONTH) == now.get(java.util.Calendar.MONTH) && 
                       cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
            "Год" -> cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
            else -> true
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val result = transactionRepository.addTransaction(transaction)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
