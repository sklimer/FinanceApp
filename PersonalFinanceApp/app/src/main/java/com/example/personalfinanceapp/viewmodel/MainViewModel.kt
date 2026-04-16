package com.example.personalfinanceapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel для главного экрана (MainActivity)
 * Управляет балансом, транзакциями, фильтрами по типу и периоду
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // StateFlow для баланса
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // StateFlow для списка транзакций
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // StateFlow для текущего типа (income/expense)
    private val _currentType = MutableStateFlow("expense")
    val currentType: StateFlow<String> = _currentType.asStateFlow()

    // StateFlow для текущего периода
    private val _currentPeriod = MutableStateFlow("Месяц")
    val currentPeriod: StateFlow<String> = _currentPeriod.asStateFlow()

    // StateFlow для сумм по категориям
    private val _categorySums = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categorySums: StateFlow<Map<String, Double>> = _categorySums.asStateFlow()

    // StateFlow для общей суммы
    private val _totalSum = MutableStateFlow(0.0)
    val totalSum: StateFlow<Double> = _totalSum.asStateFlow()

    /**
     * Получение текущего email пользователя из SharedPreferences
     */
    private fun getCurrentUserEmail(): String {
        val sharedPreferences = getApplication<Application>().applicationContext.getSharedPreferences(
            "user_prefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString("current_user", "") ?: ""
    }

    /**
     * Обновление баланса пользователя
     */
    fun updateBalance(email: String) {
        viewModelScope.launch {
            val balanceValue = db.transactionDao().getBalance(email) ?: 0.0
            _balance.value = balanceValue
        }
    }

    /**
     * Загрузка транзакций для пользователя
     */
    fun loadTransactions(email: String) {
        viewModelScope.launch {
            val allTransactions = db.transactionDao().getTransactionsForUser(email).first()
            _transactions.value = allTransactions
            filterTransactions(allTransactions, _currentType.value, _currentPeriod.value)
        }
    }

    /**
     * Установка текущего типа транзакции
     */
    fun setCurrentType(type: String) {
        _currentType.value = type
        viewModelScope.launch {
            val allTransactions = db.transactionDao().getTransactionsForUser(getCurrentUserEmail()).first()
            filterTransactions(allTransactions, type, _currentPeriod.value)
        }
    }

    /**
     * Установка текущего периода
     */
    fun setCurrentPeriod(period: String) {
        _currentPeriod.value = period
        viewModelScope.launch {
            val allTransactions = db.transactionDao().getTransactionsForUser(getCurrentUserEmail()).first()
            filterTransactions(allTransactions, _currentType.value, period)
        }
    }

    /**
     * Фильтрация транзакций по типу и периоду
     */
    private fun filterTransactions(transactions: List<Transaction>, type: String, period: String) {
        val filteredTransactions = transactions.filter {
            it.type == type && isInPeriod(it.date, period)
        }

        val categorySumsMap = filteredTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        _categorySums.value = categorySumsMap
        _totalSum.value = categorySumsMap.values.sum()
    }

    /**
     * Проверка, попадает ли дата в указанный период
     */
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
            "Выбрать год" -> false // Обработка отдельного года происходит через дополнительный параметр
            else -> true
        }
    }

    /**
     * Проверка, попадает ли дата в выбранный год
     */
    fun isInSelectedYear(date: Date, selectedYear: Int?): Boolean {
        if (selectedYear == null) return false
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        return cal.get(java.util.Calendar.YEAR) == selectedYear
    }
}