package com.bls.autobooking.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bls.autobooking.data.model.Preferences
import com.bls.autobooking.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val preferences by viewModel.preferences.collectAsState()
    
    var targetVisaType by remember { mutableStateOf(preferences?.targetVisaType ?: "") }
    var targetLocation by remember { mutableStateOf(preferences?.targetLocation ?: "") }
    var earliestDate by remember { mutableStateOf(preferences?.earliestDate ?: "") }
    var latestDate by remember { mutableStateOf(preferences?.latestDate ?: "") }
    var emailNotifications by remember { mutableStateOf(preferences?.emailNotifications ?: true) }
    var notificationEmail by remember { mutableStateOf(preferences?.notificationEmail ?: "") }
    var captchaRetryAttempts by remember { mutableStateOf(preferences?.captchaRetryAttempts?.toString() ?: "3") }
    var bookingRetryAttempts by remember { mutableStateOf(preferences?.bookingRetryAttempts?.toString() ?: "3") }
    
    val statusMessage by viewModel.statusMessage.observeAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            statusMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(bottom = 16.dp),
                    content = { Text(message) }
                )
            }
            
            Text(
                text = "Booking Preferences",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = targetVisaType,
                onValueChange = { targetVisaType = it },
                label = { Text("Target Visa Type") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = targetLocation,
                onValueChange = { targetLocation = it },
                label = { Text("Target Location") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = earliestDate,
                onValueChange = { earliestDate = it },
                label = { Text("Earliest Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = latestDate,
                onValueChange = { latestDate = it },
                label = { Text("Latest Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )
                Text("Enable Email Notifications")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = notificationEmail,
                onValueChange = { notificationEmail = it },
                label = { Text("Notification Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = captchaRetryAttempts,
                onValueChange = { captchaRetryAttempts = it },
                label = { Text("CAPTCHA Retry Attempts") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = bookingRetryAttempts,
                onValueChange = { bookingRetryAttempts = it },
                label = { Text("Booking Retry Attempts") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val prefs = Preferences(
                        targetVisaType = targetVisaType,
                        targetLocation = targetLocation,
                        earliestDate = if (earliestDate.isNotBlank()) earliestDate else null,
                        latestDate = if (latestDate.isNotBlank()) latestDate else null,
                        emailNotifications = emailNotifications,
                        notificationEmail = notificationEmail,
                        captchaRetryAttempts = captchaRetryAttempts.toIntOrNull() ?: 3,
                        bookingRetryAttempts = bookingRetryAttempts.toIntOrNull() ?: 3
                    )
                    
                    viewModel.savePreferences(prefs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = targetVisaType.isNotBlank() && targetLocation.isNotBlank() && 
                         notificationEmail.isNotBlank()
            ) {
                Text("Save Preferences")
            }
        }
    }
}