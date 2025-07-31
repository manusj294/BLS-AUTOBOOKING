package com.bls.autobooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bls.autobooking.ui.screen.AddEditApplicantScreen
import com.bls.autobooking.ui.screen.ApplicantListScreen
import com.bls.autobooking.ui.theme.BLSSpainVisaAutoBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BLSSpainVisaAutoBookingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "applicant_list"
    ) {
        composable("applicant_list") {
            ApplicantListScreen(
                onAddApplicant = { navController.navigate("add_applicant") },
                onEditApplicant = { applicant ->
                    navController.navigate("edit_applicant/${applicant.id}")
                }
            )
        }
        
        composable("add_applicant") {
            AddEditApplicantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("edit_applicant/{applicantId}") { backStackEntry ->
            // In a real implementation, you would fetch the applicant by ID
            // For now, we'll pass null to indicate edit mode
            AddEditApplicantScreen(
                applicant = null, // Would be fetched by ID in real implementation
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("preferences") {
            com.bls.autobooking.ui.screen.PreferencesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}