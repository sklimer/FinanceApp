package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * ViewModel для аутентификации (LoginActivity, RegisterActivity)
 * Управляет входом и регистрацией пользователей
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    
    // StateFlow для состояния загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // StateFlow для сообщений об ошибках
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // StateFlow для успешной аутентификации
    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    /**
     * Вход пользователя
     */
    fun login(email: String, password: String, onAuthComplete: () -> Unit) {
        if (!isValidEmail(email)) {
            _errorMessage.value = "Некорректный email"
            return
        }
        
        if (!isValidPassword(password)) {
            _errorMessage.value = "Некорректный пароль"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val hashedPassword = hashPassword(password)
                val user = db.userDao().getUserByEmail(email)
                
                if (user != null && user.passwordHash == hashedPassword) {
                    _authSuccess.value = true
                    onAuthComplete()
                } else {
                    _errorMessage.value = "Неверный email или пароль"
                    _authSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка входа: ${e.message}"
                _authSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Регистрация нового пользователя
     */
    fun register(email: String, password: String, onComplete: () -> Unit) {
        if (!isValidEmail(email)) {
            _errorMessage.value = "Некорректный email. Требования: до 40 символов, только латинские буквы, цифры и символы (без кириллицы)"
            return
        }
        
        if (!isValidPassword(password)) {
            _errorMessage.value = "Некорректный пароль. Требования: минимум 8 символов, минимум одна заглавная буква, одна строчная буква, одна цифра, один специальный символ (!@#$%^&*), только латинские буквы (без кириллицы)"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val existingUser = db.userDao().getUserByEmail(email)
                if (existingUser == null) {
                    val hashedPassword = hashPassword(password)
                    db.userDao().insertUser(User(email, hashedPassword))
                    _authSuccess.value = true
                    onComplete()
                } else {
                    _errorMessage.value = "Пользователь уже существует"
                    _authSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка регистрации: ${e.message}"
                _authSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Валидация email
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]{1,40}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Валидация пароля
     */
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    /**
     * Хэширование пароля (SHA-256)
     * Примечание: для продакшена рекомендуется использовать BCrypt или Argon2
     */
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
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
    fun resetAuthSuccess() {
        _authSuccess.value = false
    }
}
