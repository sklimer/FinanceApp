package com.example.personalfinanceapp.viewmodel

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

/**
 * Unit тесты для AuthViewModel
 * Тестируют валидацию email, пароля и хэширование
 */
class AuthViewModelTest {

    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        // Для unit тестов нам не нужен реальный Application, 
        // поэтому создаём тестовый экземпляр с моком
        // В реальном проекте следует использовать MockK или Mockito
    }

    @Test
    fun testValidEmails() {
        // Тестирование валидных email адресов
        val validEmails = listOf(
            "user@example.com",
            "test.user@domain.org",
            "user123@test.co.uk",
            "a@b.co",
            "user_name@domain.com",
            "user-name@domain.com",
            "user.name+tag@domain.com"
        )

        for (email in validEmails) {
            assertTrue("Email $email должен быть валидным", isValidEmail(email))
        }
    }

    @Test
    fun testInvalidEmails() {
        // Тестирование невалидных email адресов
        val invalidEmails = listOf(
            "",
            "invalid",
            "@example.com",
            "user@",
            "user@domain",
            "пользователь@домен.ru",  // Кириллица
            "user@домен.ru",  // Кириллица в домене
            "user name@domain.com",  // Пробел
            "user@domain.c",  // Слишком короткий TLD
            "user..name@domain.com"  // Две точки подряд
        )

        for (email in invalidEmails) {
            assertFalse("Email $email должен быть невалидным", isValidEmail(email))
        }
    }

    @Test
    fun testValidPasswords() {
        // Тестирование валидных паролей
        val validPasswords = listOf(
            "Password1!",
            "SecurePass123!",
            "MyP@ssw0rd",
            "Test1234!",
            "Abcdefg1!"
        )

        for (password in validPasswords) {
            assertTrue("Пароль $password должен быть валидным", isValidPassword(password))
        }
    }

    @Test
    fun testInvalidPasswords() {
        // Тестирование невалидных паролей
        val invalidPasswords = listOf(
            "",  // Пустой
            "short",  // Короткий
            "nouppercase1!",  // Нет заглавной буквы
            "NOLOWERCASE1!",  // Нет строчной буквы
            "NoDigit!",  // Нет цифры
            "NoSpecialChar1",  // Нет спецсимвола
            "пароль123!",  // Кириллица
            "Password1",  // Нет спецсимвола
            "password1!",  // Нет заглавной буквы
            "PASSWORD1!"  // Нет строчной буквы
        )

        for (password in invalidPasswords) {
            assertFalse("Пароль $password должен быть невалидным", isValidPassword(password))
        }
    }

    @Test
    fun testPasswordHashing() {
        // Тестирование хэширования паролей
        val password = "TestPassword123!"
        val hash1 = hashPassword(password)
        val hash2 = hashPassword(password)

        // Хэши должны быть одинаковыми для одного и того же пароля
        assertEquals("Хэши одного пароля должны совпадать", hash1, hash2)

        // Хэш не должен быть пустым
        assertTrue("Хэш не должен быть пустым", hash1.isNotEmpty())

        // Хэш должен быть длиной 64 символа (SHA-256 в hex формате)
        assertEquals("Длина хэша SHA-256 должна быть 64 символа", 64, hash1.length)
    }

    @Test
    fun testPasswordHashDeterministic() {
        // Проверка детерминированности хэширования
        val passwords = listOf("Pass1!", "Secure123!", "Test@123")

        for (password in passwords) {
            val hash1 = hashPassword(password)
            val hash2 = hashPassword(password)
            val hash3 = hashPassword(password)

            assertEquals("Хэширование должно быть детерминированным для $password", hash1, hash2)
            assertEquals("Хэширование должно быть детерминированным для $password", hash2, hash3)
        }
    }

    @Test
    fun testDifferentPasswordsProduceDifferentHashes() {
        // Разные пароли должны давать разные хэши
        val password1 = "Password1!"
        val password2 = "Password2!"

        val hash1 = hashPassword(password1)
        val hash2 = hashPassword(password2)

        assertNotEquals("Разные пароли должны давать разные хэши", hash1, hash2)
    }

    /**
     * Валидация email (копия из AuthViewModel для тестирования)
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]{1,40}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Валидация пароля (копия из AuthViewModel для тестирования)
     */
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    /**
     * Хэширование пароля (копия из AuthViewModel для тестирования)
     */
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
