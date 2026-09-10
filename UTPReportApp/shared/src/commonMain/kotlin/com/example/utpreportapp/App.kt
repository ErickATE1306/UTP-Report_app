package com.example.utpreportapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.utpreportapp.ui.screens.auth.LoginScreen
import com.example.utpreportapp.ui.screens.seguridad.SecurityModuleScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        var isSecuritySessionStarted by rememberSaveable { mutableStateOf(false) }
        var loginError by rememberSaveable { mutableStateOf<String?>(null) }

        if (isSecuritySessionStarted) {
            SecurityModuleScreen()
        } else {
            LoginScreen(
                authenticationError = loginError,
                demoCredentialsHint = "Cuenta demo: seguridad.demo / 123456",
                onLogin = { username, password ->
                    if (username.equals("seguridad.demo", ignoreCase = true) && password == "123456") {
                        loginError = null
                        isSecuritySessionStarted = true
                    } else {
                        loginError = "Usuario o contraseña incorrectos"
                    }
                },
            )
        }
    }
}
