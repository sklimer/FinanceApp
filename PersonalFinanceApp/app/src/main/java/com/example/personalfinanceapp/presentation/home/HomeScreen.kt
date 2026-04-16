package com.example.personalfinanceapp.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddTransactionClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои финансы") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "Добавить транзакцию"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Баланс",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${"%.2f".format(uiState.balance)} ₽",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Type Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = uiState.currentType == "income",
                    onClick = { viewModel.setCurrentType("income") },
                    label = { Text("Доходы") }
                )
                FilterChip(
                    selected = uiState.currentType == "expense",
                    onClick = { viewModel.setCurrentType("expense") },
                    label = { Text("Расходы") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("День", "Неделя", "Месяц", "Год").forEach { period ->
                    FilterChip(
                        selected = uiState.currentPeriod == period,
                        onClick = { viewModel.setCurrentPeriod(period) },
                        label = { Text(period) }
                    )
            }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Summary
            Text(
                text = "Итого: ${"%.2f".format(uiState.totalSum)} ₽",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.categorySums.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет транзакций за выбранный период")
                }
            } else {
                LazyColumn {
                    items(uiState.categorySums.size) { index ->
                        val entry = uiState.categorySums.entries.elementAt(index)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.key)
                                Text("${"%.2f".format(entry.value)} ₽")
                            }
                        }
                    }
                }
            }
        }
    }
}
