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
import com.example.utpreportapp.ui.screens.usuario.PantallaInicioUsuario

private enum class RolDemostracion {
    SEGURIDAD,
    USUARIO,
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var sessionRoleName by rememberSaveable { mutableStateOf<String?>(null) }
        var loginError by rememberSaveable { mutableStateOf<String?>(null) }

        when (sessionRoleName?.let(RolDemostracion::valueOf)) {
            RolDemostracion.SEGURIDAD -> SecurityModuleScreen()
            RolDemostracion.USUARIO -> PantallaInicioUsuario()
            null -> LoginScreen(
                authenticationError = loginError,
                onLogin = { username, password ->
                    val role = when {
                        username.equals("seguridad.demo", ignoreCase = true) && password == "123456" -> {
                            RolDemostracion.SEGURIDAD
                        }
                        username.equals("usuario.demo", ignoreCase = true) && password == "123456" -> {
                            RolDemostracion.USUARIO
                        }
                        else -> null
                    }

                    if (role != null) {
                        loginError = null
                        sessionRoleName = role.name
                    } else {
                        loginError = "Usuario o contraseña incorrectos"
                    }
                },
            )
        }
    }
}
