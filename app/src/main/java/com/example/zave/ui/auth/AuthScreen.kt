package com.example.zave.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.zave.R
import com.example.zave.ui.common.navigation.Screen
import com.example.zave.ui.theme.DarkBackground
import com.example.zave.ui.theme.TextPrimary
import com.example.zave.ui.theme.TextSecondary
import com.example.zave.ui.theme.AccentBlue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// Use the Web Client ID from your Firebase project (must be stored securely)
private const val WEB_CLIENT_ID = "955610946355-s04j8etrj6qmv7t95lcfb5a9ta3dn9ot.apps.googleusercontent.com"
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 1. Handle Navigation on Success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    // 2. Google Sign-In Setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Get the non-null account (!! throws exception caught below if null)
                val account = task.getResult(ApiException::class.java)!!
                viewModel.signInWithGoogle(account)
            } catch (e: ApiException) {
                // The VM expects a non-null account. For API failure, we reset state/show error.
                Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        } else {
            // User cancelled login
            viewModel.clearError()
        }
    }

    // 3. UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for App Logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            // NOTE: The launcher foreground image is usually white/transparent,
            // so we rely on the dark background for contrast.
            modifier = Modifier.size(120.dp).padding(bottom = 32.dp)
        )

        Text(
            text = "Welcome to Shopper's Compass",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Sign in to discover local deals and stores.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        when (uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = AccentBlue
                )
            }
            else -> {
                Button(
                    onClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Sign in with Gmail")
                }
            }
        }

        // 4. Handle Error Display
        if (uiState is AuthUiState.Error) {
            val errorMessage = (uiState as AuthUiState.Error).message
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            LaunchedEffect(errorMessage) {
                viewModel.clearError()
            }
        }
    }
}
