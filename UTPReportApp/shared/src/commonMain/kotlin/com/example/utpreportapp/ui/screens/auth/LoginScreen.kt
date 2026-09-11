package com.example.utpreportapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val LoginCoral = Color(0xFFFF5266)
private val LoginCoralLight = Color(0xFFFF8B82)
private val LoginPurple = Color(0xFF5B3DF5)
private val LoginPurpleSoft = Color(0xFFF0EDFF)
private val LoginInk = Color(0xFF12233F)
private val LoginMuted = Color(0xFF667085)
private val LoginBackground = Color(0xFFF7F7F9)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    authenticationError: String? = null,
    demoCredentialsHint: String? = null,
    onLogin: (username: String, password: String) -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        if (username.isNotBlank()) usernameError = null
    }
    LaunchedEffect(password) {
        if (password.isNotBlank()) passwordError = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LoginBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LoginCoralLight, LoginCoral),
                    ),
                )
                .padding(horizontal = 24.dp, vertical = 30.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Seguridad",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = LoginInk,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "UTP Reporta",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Acceso del personal de seguridad",
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-54).dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Bienvenido",
                    color = LoginInk,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Ingresa con tu cuenta institucional",
                    modifier = Modifier.padding(top = 6.dp),
                    color = LoginMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                if (demoCredentialsHint != null) {
                    Text(
                        text = demoCredentialsHint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .background(LoginPurpleSoft, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        color = LoginPurple,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text("Usuario o correo") },
                    placeholder = { Text("seguridad.demo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = usernameError != null,
                    supportingText = usernameError?.let { message -> { Text(message) } },
                    colors = loginTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text("Contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoading,
                            colors = ButtonDefaults.textButtonColors(contentColor = LoginPurple),
                        ) {
                            Text(if (passwordVisible) "Ocultar" else "Mostrar")
                        }
                    },
                    isError = passwordError != null,
                    supportingText = passwordError?.let { message -> { Text(message) } },
                    colors = loginTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                )

                if (authenticationError != null) {
                    Text(
                        text = authenticationError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(LoginCoral.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        color = LoginCoral,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        usernameError = if (username.isBlank()) {
                            "Ingresa tu usuario o correo"
                        } else {
                            null
                        }
                        passwordError = if (password.isBlank()) {
                            "Ingresa tu contraseña"
                        } else {
                            null
                        }

                        if (usernameError == null && passwordError == null) {
                            onLogin(username.trim(), password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(54.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = LoginPurple),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Iniciar sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LoginPurple,
    focusedLabelColor = LoginPurple,
    cursorColor = LoginPurple,
    errorBorderColor = LoginCoral,
    errorLabelColor = LoginCoral,
    errorSupportingTextColor = LoginCoral,
)
