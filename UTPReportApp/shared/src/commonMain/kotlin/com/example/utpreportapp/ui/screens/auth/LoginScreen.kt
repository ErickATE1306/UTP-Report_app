package com.example.utpreportapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LoginCoral = Color(0xFFFF3F60)
private val LoginNavy = Color(0xFF0D123F)
private val LoginInk = Color(0xFF30333A)
private val LoginMuted = Color(0xFF626873)
private val LoginBorder = Color(0xFF5F707A)
private val LoginBackground = Color(0xFFF1F6FA)
private val LoginButtonDisabled = Color(0xFFD6E6F1)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    authenticationError: String? = null,
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp),
    ) {
        Spacer(Modifier.height(110.dp))

        UtpAppLogo(
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(52.dp))

        Text(
            text = "Accede",
            color = LoginInk,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "ingresando tus datos institucionales.",
            modifier = Modifier.padding(top = 4.dp),
            color = LoginMuted,
            style = MaterialTheme.typography.bodyLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 28.dp)
                .height(1.dp)
                .background(Color(0xFFBDD2DE)),
        )

        Text(
            text = "Usuario: código o cuenta institucional",
            color = LoginInk,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            enabled = !isLoading,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Ej. 1482720 o U71698935",
                    fontSize = 16.sp,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = usernameError != null,
            supportingText = usernameError?.let { message -> { Text(message) } },
            colors = loginTextFieldColors(),
            shape = RoundedCornerShape(6.dp),
        )

        Row(
            modifier = Modifier.padding(top = 7.dp, bottom = 25.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "ⓘ",
                color = LoginMuted,
                fontSize = 17.sp,
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Ejemplo de usuario: U1533148 (no digitar el @utp.edu.pe)",
                color = LoginMuted,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
            )
        }

        Text(
            text = "Contraseña",
            color = LoginInk,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            enabled = !isLoading,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Ingresa tu contraseña",
                    fontSize = 16.sp,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                androidx.compose.material3.IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !isLoading,
                ) {
                    PasswordVisibilityIcon(
                        visible = passwordVisible,
                        modifier = Modifier.size(27.dp),
                    )
                }
            },
            isError = passwordError != null,
            supportingText = passwordError?.let { message -> { Text(message) } },
            colors = loginTextFieldColors(),
            shape = RoundedCornerShape(6.dp),
        )

        TextButton(
            onClick = {},
            modifier = Modifier.padding(top = 10.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = LoginNavy),
        ) {
            Text("¿Olvidaste tu contraseña?", fontWeight = FontWeight.Bold)
        }

        if (authenticationError != null) {
            Text(
                text = authenticationError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(LoginCoral.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                color = LoginCoral,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = {
                usernameError = if (username.isBlank()) "Ingresa tu usuario o correo" else null
                passwordError = if (password.isBlank()) "Ingresa tu contraseña" else null
                if (usernameError == null && passwordError == null) {
                    onLogin(username.trim(), password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(56.dp),
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LoginNavy,
                disabledContainerColor = LoginButtonDisabled,
                disabledContentColor = Color.White,
            ),
            shape = RoundedCornerShape(6.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Ingresar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LoginNavy,
    unfocusedBorderColor = LoginBorder,
    cursorColor = LoginNavy,
    errorBorderColor = LoginCoral,
    errorLabelColor = LoginCoral,
    errorSupportingTextColor = LoginCoral,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
)

@Composable
private fun UtpAppLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        listOf("U", "T", "P").forEachIndexed { index, letter ->
            UtpLetterTile(letter = letter, modifier = Modifier.size(42.dp))
            if (index < 2) {
                Spacer(Modifier.width(2.dp))
            }
        }
        Box(
            modifier = Modifier
                .width(94.dp)
                .height(48.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterStart),
            ) {
                val bar = 5.dp.toPx()
                drawRect(
                    color = LoginCoral,
                    topLeft = Offset(2.dp.toPx(), (size.height - bar) / 2f),
                    size = Size(size.width - 4.dp.toPx(), bar),
                )
                drawRect(
                    color = LoginCoral,
                    topLeft = Offset((size.width - bar) / 2f, 3.dp.toPx()),
                    size = Size(bar, size.height - 6.dp.toPx()),
                )
            }
            Text(
                text = "app",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp),
                color = Color.Black,
                fontSize = 35.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun UtpLetterTile(
    letter: String,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawRect(Color.Black)

        val unit = size.minDimension / 42f
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)

        val glyph = when (letter) {
            "U" -> Path().apply {
                moveTo(point(8f, 7f).x, point(8f, 7f).y)
                lineTo(point(16f, 7f).x, point(16f, 7f).y)
                lineTo(point(16f, 27f).x, point(16f, 27f).y)
                lineTo(point(19f, 31f).x, point(19f, 31f).y)
                lineTo(point(23f, 31f).x, point(23f, 31f).y)
                lineTo(point(26f, 27f).x, point(26f, 27f).y)
                lineTo(point(26f, 7f).x, point(26f, 7f).y)
                lineTo(point(34f, 7f).x, point(34f, 7f).y)
                lineTo(point(34f, 30f).x, point(34f, 30f).y)
                lineTo(point(27f, 37f).x, point(27f, 37f).y)
                lineTo(point(15f, 37f).x, point(15f, 37f).y)
                lineTo(point(8f, 30f).x, point(8f, 30f).y)
                close()
            }

            "T" -> Path().apply {
                moveTo(point(6f, 7f).x, point(6f, 7f).y)
                lineTo(point(36f, 7f).x, point(36f, 7f).y)
                lineTo(point(36f, 15f).x, point(36f, 15f).y)
                lineTo(point(25f, 15f).x, point(25f, 15f).y)
                lineTo(point(25f, 37f).x, point(25f, 37f).y)
                lineTo(point(17f, 37f).x, point(17f, 37f).y)
                lineTo(point(17f, 15f).x, point(17f, 15f).y)
                lineTo(point(6f, 15f).x, point(6f, 15f).y)
                close()
            }

            else -> Path().apply {
                moveTo(point(8f, 7f).x, point(8f, 7f).y)
                lineTo(point(27f, 7f).x, point(27f, 7f).y)
                lineTo(point(34f, 14f).x, point(34f, 14f).y)
                lineTo(point(34f, 23f).x, point(34f, 23f).y)
                lineTo(point(27f, 30f).x, point(27f, 30f).y)
                lineTo(point(16f, 30f).x, point(16f, 30f).y)
                lineTo(point(16f, 37f).x, point(16f, 37f).y)
                lineTo(point(8f, 37f).x, point(8f, 37f).y)
                close()
            }
        }

        scale(scale = 0.88f, pivot = center) {
            drawPath(glyph, Color.White)

            if (letter == "P") {
                val counter = Path().apply {
                    moveTo(point(16f, 15f).x, point(16f, 15f).y)
                    lineTo(point(25f, 15f).x, point(25f, 15f).y)
                    lineTo(point(28f, 18f).x, point(28f, 18f).y)
                    lineTo(point(25f, 22f).x, point(25f, 22f).y)
                    lineTo(point(16f, 22f).x, point(16f, 22f).y)
                    close()
                }
                drawPath(counter, Color.Black)
            }
        }
    }
}

@Composable
private fun PasswordVisibilityIcon(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val iconColor = LoginBorder
        val strokeWidth = 1.9.dp.toPx()
        val eye = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.5f)
            cubicTo(
                size.width * 0.28f,
                size.height * 0.18f,
                size.width * 0.72f,
                size.height * 0.18f,
                size.width * 0.92f,
                size.height * 0.5f,
            )
            cubicTo(
                size.width * 0.72f,
                size.height * 0.82f,
                size.width * 0.28f,
                size.height * 0.82f,
                size.width * 0.08f,
                size.height * 0.5f,
            )
        }
        drawPath(
            path = eye,
            color = iconColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.12f,
            center = center,
            style = Stroke(width = strokeWidth),
        )
        if (!visible) {
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.12f, size.height * 0.1f),
                end = Offset(size.width * 0.88f, size.height * 0.9f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
