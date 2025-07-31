package com.blsautobooking.app.data

import android.webkit.WebView
import android.webkit.WebViewClient
import com.blsautobooking.app.data.model.LoggedInUser
import com.blsautobooking.app.network.BLSWebClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {
            try {
                // Simulate BLS website login
                val webClient = BLSWebClient()
                val loginSuccess = webClient.performLogin(username, password)
                
                if (loginSuccess) {
                    val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), username)
                    Result.Success(fakeUser)
                } else {
                    Result.Error(IOException("Error logging in"))
                }
            } catch (e: Throwable) {
                Result.Error(IOException("Error logging in", e))
            }
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}

