package com.example.personalfinanceapp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.personalfinanceapp.data.User
import com.example.personalfinanceapp.data.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация AuthRepository
 * Управляет аутентификацией и хранением текущего пользователя
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private val CURRENT_USER_KEY = stringPreferencesKey("current_user_email")
    }

    override fun getCurrentUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[CURRENT_USER_KEY]
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Некорректный email"))
            }
            if (!isValidPassword(password)) {
                return Result.failure(Exception("Некорректный пароль"))
            }

            val hashedPassword = hashPassword(password)
            val user = userDao.getUserByEmail(email)

            if (user != null && user.passwordHash == hashedPassword) {
                saveCurrentUser(email)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Некорректный email"))
            }
            if (!isValidPassword(password)) {
                return Result.failure(Exception("Некорректный пароль"))
            }

            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Пользователь уже существует"))
            }

            val hashedPassword = hashPassword(password)
            userDao.insertUser(User(email, hashedPassword))
            saveCurrentUser(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_KEY)
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    override fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]{1,40}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    override fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    private suspend fun saveCurrentUser(email: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_KEY] = email
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
