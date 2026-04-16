package com.example.personalfinanceapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.personalfinanceapp.data.AppDatabase
import com.example.personalfinanceapp.data.TransactionDao
import com.example.personalfinanceapp.data.UserDao
import com.example.personalfinanceapp.data.RecurringPaymentDao
import com.example.personalfinanceapp.repository.AuthRepository
import com.example.personalfinanceapp.repository.TransactionRepository
import com.example.personalfinanceapp.repository.AuthRepositoryImpl
import com.example.personalfinanceapp.repository.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideRecurringPaymentDao(database: AppDatabase): RecurringPaymentDao {
        return database.recurringPaymentDao()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        dataStore: DataStore<Preferences>
    ): AuthRepository {
        return AuthRepositoryImpl(userDao, dataStore)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        dataStore: DataStore<Preferences>
    ): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao, dataStore)
    }
}
