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
import com.bls.autobooking.data.model.Applicant
import com.bls.autobooking.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditApplicantScreen(
    viewModel: MainViewModel = viewModel(),
    applicant: Applicant? = null,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf(applicant?.fullName ?: "") }
    var email by remember { mutableStateOf(applicant?.email ?: "") }
    var passportNumber by remember { mutableStateOf(applicant?.passportNumber ?: "") }
    var nationality by remember { mutableStateOf(applicant?.nationality ?: "") }
    var visaType by remember { mutableStateOf(applicant?.visaType ?: "") }
    var location by remember { mutableStateOf(applicant?.location ?: "") }
    
    val statusMessage by viewModel.statusMessage.observeAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (applicant == null) "Add Applicant" else "Edit Applicant") },
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
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = { Text("Passport Number") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = visaType,
                onValueChange = { visaType = it },
                label = { Text("Visa Type") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val newApplicant = Applicant(
                        id = applicant?.id ?: 0,
                        fullName = fullName,
                        email = email,
                        passportNumber = passportNumber,
                        nationality = nationality,
                        visaType = visaType,
                        location = location
                    )
                    
                    if (applicant == null) {
                        viewModel.addApplicant(newApplicant)
                    } else {
                        viewModel.updateApplicant(newApplicant)
                    }
                    
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = fullName.isNotBlank() && email.isNotBlank() && 
                         passportNumber.isNotBlank() && nationality.isNotBlank() &&
                         visaType.isNotBlank() && location.isNotBlank()
            ) {
                Text(if (applicant == null) "Add Applicant" else "Update Applicant")
            }
        }
    }
}