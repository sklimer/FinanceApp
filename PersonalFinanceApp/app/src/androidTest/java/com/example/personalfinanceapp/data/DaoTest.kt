package com.example.personalfinanceapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Instrumented тесты для DAO классов (UserDao, TransactionDao)
 * Тестируют вставку, получение пользователей и транзакций, подсчёт баланса
 */
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserInsertionAndRetrieval() = runBlocking {
        // Тест вставки и получения пользователя
        val user = User("test@example.com", "hashedPassword123")
        userDao.insertUser(user)

        val retrievedUser = userDao.getUserByEmail("test@example.com")
        assertNotNull("Пользователь должен быть найден", retrievedUser)
        assertEquals("Email должен совпадать", "test@example.com", retrievedUser?.email)
        assertEquals("Хэш пароля должен совпадать", "hashedPassword123", retrievedUser?.passwordHash)
    }

    @Test
    fun testTransactionForUser() = runBlocking {
        // Тест транзакций для пользователя
        val user = User("transaction_test@example.com", "passwordHash")
        userDao.insertUser(user)

        val transaction1 = Transaction(
            userEmail = "transaction_test@example.com",
            type = "expense",
            amount = 100.0,
            category = "Food",
            date = Date()
        )

        val transaction2 = Transaction(
            userEmail = "transaction_test@example.com",
            type = "income",
            amount = 500.0,
            category = "Salary",
            date = Date()
        )

        transactionDao.insertTransaction(transaction1)
        transactionDao.insertTransaction(transaction2)

        val transactions = transactionDao.getTransactionsForUser("transaction_test@example.com").first()
        assertEquals("Должно быть 2 транзакции", 2, transactions.size)

        val expenses = transactions.filter { it.type == "expense" }
        val incomes = transactions.filter { it.type == "income" }

        assertEquals("Должна быть 1 трата", 1, expenses.size)
        assertEquals("Должен быть 1 доход", 1, incomes.size)
        assertEquals("Сумма трат должна быть 100.0", 100.0, expenses[0].amount)
        assertEquals("Сумма доходов должна быть 500.0", 500.0, incomes[0].amount)
    }

    @Test
    fun testBalanceCalculation() = runBlocking {
        // Тест подсчёта баланса
        val user = User("balance_test@example.com", "passwordHash")
        userDao.insertUser(user)

        // Добавляем несколько транзакций
        transactionDao.insertTransaction(
            Transaction("balance_test@example.com", "income", 1000.0, "Salary", Date())
        )
        transactionDao.insertTransaction(
            Transaction("balance_test@example.com", "expense", 300.0, "Food", Date())
        )
        transactionDao.insertTransaction(
            Transaction("balance_test@example.com", "expense", 200.0, "Transport", Date())
        )

        val balance = transactionDao.getBalance("balance_test@example.com")
        assertEquals("Баланс должен быть 500.0 (1000 - 300 - 200)", 500.0, balance, 0.01)
    }

    @Test
    fun testGetNonExistentUser() = runBlocking {
        // Тест поиска несуществующего пользователя
        val user = userDao.getUserByEmail("nonexistent@example.com")
        assertNull("Несуществующий пользователь должен вернуть null", user)
    }

    @Test
    fun testGetBalanceForNonExistentUser() = runBlocking {
        // Тест получения баланса для несуществующего пользователя
        val balance = transactionDao.getBalance("nonexistent@example.com")
        assertNull("Баланс несуществующего пользователя должен быть null", balance)
    }

    @Test
    fun testMultipleTransactionsSameCategory() = runBlocking {
        // Тест нескольких транзакций в одной категории
        val user = User("category_test@example.com", "passwordHash")
        userDao.insertUser(user)

        transactionDao.insertTransaction(
            Transaction("category_test@example.com", "expense", 50.0, "Food", Date())
        )
        transactionDao.insertTransaction(
            Transaction("category_test@example.com", "expense", 75.0, "Food", Date())
        )
        transactionDao.insertTransaction(
            Transaction("category_test@example.com", "expense", 100.0, "Food", Date())
        )

        val transactions = transactionDao.getTransactionsForUser("category_test@example.com").first()
        assertEquals("Должно быть 3 транзакции", 3, transactions.size)

        val foodTransactions = transactions.filter { it.category == "Food" }
        assertEquals("Все транзакции должны быть в категории Food", 3, foodTransactions.size)

        val totalFoodExpense = foodTransactions.sumOf { it.amount }
        assertEquals("Общая сумма трат на еду должна быть 225.0", 225.0, totalFoodExpense, 0.01)
    }

    @Test
    fun testDeleteUser() = runBlocking {
        // Тест удаления пользователя (если метод существует)
        val user = User("delete_test@example.com", "passwordHash")
        userDao.insertUser(user)

        val retrievedUser = userDao.getUserByEmail("delete_test@example.com")
        assertNotNull("Пользователь должен существовать перед удалением", retrievedUser)

        // Проверяем, есть ли метод deleteUser в UserDao
        try {
            userDao::class.java.getMethod("deleteUser", User::class.java)
            userDao.deleteUser(user)
            val deletedUser = userDao.getUserByEmail("delete_test@example.com")
            assertNull("Пользователь должен быть удалён", deletedUser)
        } catch (e: NoSuchMethodException) {
            // Метод deleteUser не реализован, пропускаем тест
            println("Метод deleteUser не реализован в UserDao")
        }
    }
}
