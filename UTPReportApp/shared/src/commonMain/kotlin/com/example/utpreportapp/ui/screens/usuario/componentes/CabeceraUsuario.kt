package com.example.utpreportapp.ui.screens.usuario.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utpreportapp.ui.screens.usuario.modelo.TipoIconoCabecera
import com.example.utpreportapp.ui.screens.usuario.tema.PaletaUsuario

@Composable
internal fun CabeceraUsuario() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(258.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(PaletaUsuario.CoralClaro, PaletaUsuario.Coral),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarUsuarioGenerico()
                    Text(
                        text = "U22215657",
                        modifier = Modifier.padding(start = 9.dp),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                AccionCabeceraUsuario(TipoIconoCabecera.CARNET)
                AccionCabeceraUsuario(TipoIconoCabecera.CORREO)
                AccionCabeceraUsuario(
                    tipo = TipoIconoCabecera.NOTIFICACION,
                    mostrarNotificacion = true,
                )
                AccionCabeceraUsuario(
                    tipo = TipoIconoCabecera.REPORTAR,
                    etiqueta = "Reportar\nahora",
                )
            }

            Text(
                text = "¡Hola!",
                modifier = Modifier.padding(top = 28.dp),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            TarjetaPendientesUsuario(modifier = Modifier.padding(top = 18.dp))
        }
    }
}

@Composable
private fun AvatarUsuarioGenerico() {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White,
    ) {
        Canvas(modifier = Modifier.padding(8.dp)) {
            drawCircle(
                color = Color(0xFFD5DAE3),
                radius = size.minDimension * 0.19f,
                center = Offset(size.width / 2f, size.height * 0.34f),
            )
            drawArc(
                color = Color(0xFFAEB7C7),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(size.width * 0.15f, size.height * 0.49f),
                size = Size(size.width * 0.7f, size.height * 0.58f),
            )
        }
    }
}

@Composable
private fun AccionCabeceraUsuario(
    tipo: TipoIconoCabecera,
    etiqueta: String? = null,
    mostrarNotificacion: Boolean = false,
) {
    Column(
        modifier = Modifier
            .width(if (etiqueta == null) 42.dp else 50.dp)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(31.dp)) {
            DibujoIconoCabeceraUsuario(
                tipo = tipo,
                modifier = Modifier.fillMaxSize(),
            )
            if (mostrarNotificacion) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .background(Color(0xFFFFD439), CircleShape),
                )
            }
        }
        if (etiqueta != null) {
            Text(
                text = etiqueta,
                color = Color.White,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DibujoIconoCabeceraUsuario(
    tipo: TipoIconoCabecera,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val grosor = 2.dp.toPx()
        val trazoBlanco = Stroke(grosor, cap = StrokeCap.Round)

        when (tipo) {
            TipoIconoCabecera.CARNET -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(size.width * 0.08f, size.height * 0.18f),
                    size = Size(size.width * 0.84f, size.height * 0.64f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = trazoBlanco,
                )
                drawCircle(
                    Color.White,
                    size.minDimension * 0.09f,
                    Offset(size.width * 0.3f, size.height * 0.42f),
                )
                drawLine(
                    Color.White,
                    Offset(size.width * 0.2f, size.height * 0.66f),
                    Offset(size.width * 0.4f, size.height * 0.66f),
                    grosor,
                )
                drawLine(
                    Color.White,
                    Offset(size.width * 0.55f, size.height * 0.38f),
                    Offset(size.width * 0.78f, size.height * 0.38f),
                    grosor,
                )
                drawLine(
                    Color.White,
                    Offset(size.width * 0.55f, size.height * 0.58f),
                    Offset(size.width * 0.78f, size.height * 0.58f),
                    grosor,
                )
            }

            TipoIconoCabecera.CORREO -> {
                drawRoundRect(
                    Color.White,
                    Offset(size.width * 0.08f, size.height * 0.2f),
                    Size(size.width * 0.84f, size.height * 0.6f),
                    CornerRadius(3.dp.toPx()),
                    style = trazoBlanco,
                )
                drawLine(Color.White, Offset(size.width * 0.1f, size.height * 0.25f), center, grosor)
                drawLine(Color.White, Offset(size.width * 0.9f, size.height * 0.25f), center, grosor)
            }

            TipoIconoCabecera.NOTIFICACION -> {
                val campana = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.67f)
                    quadraticTo(size.width * 0.31f, size.height * 0.55f, size.width * 0.31f, size.height * 0.38f)
                    quadraticTo(size.width * 0.31f, size.height * 0.16f, size.width * 0.5f, size.height * 0.16f)
                    quadraticTo(size.width * 0.69f, size.height * 0.16f, size.width * 0.69f, size.height * 0.38f)
                    quadraticTo(size.width * 0.69f, size.height * 0.55f, size.width * 0.78f, size.height * 0.67f)
                    close()
                }
                drawPath(campana, Color.White, style = trazoBlanco)
                drawCircle(
                    Color.White,
                    size.minDimension * 0.055f,
                    Offset(size.width * 0.5f, size.height * 0.79f),
                )
            }

            TipoIconoCabecera.REPORTAR -> {
                val escudo = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.08f)
                    lineTo(size.width * 0.84f, size.height * 0.22f)
                    lineTo(size.width * 0.77f, size.height * 0.68f)
                    lineTo(size.width * 0.5f, size.height * 0.9f)
                    lineTo(size.width * 0.23f, size.height * 0.68f)
                    lineTo(size.width * 0.16f, size.height * 0.22f)
                    close()
                }
                drawPath(escudo, Color.White, style = trazoBlanco)
                drawLine(
                    Color.White,
                    Offset(size.width * 0.5f, size.height * 0.3f),
                    Offset(size.width * 0.5f, size.height * 0.58f),
                    grosor,
                )
                drawCircle(
                    Color.White,
                    size.minDimension * 0.045f,
                    Offset(size.width * 0.5f, size.height * 0.7f),
                )
            }
        }
    }
}

@Composable
private fun TarjetaPendientesUsuario(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(7.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("◷", fontSize = 25.sp, color = PaletaUsuario.Texto)
            Text(
                text = "No hay más pendientes por hoy",
                modifier = Modifier.padding(start = 9.dp),
                color = PaletaUsuario.Texto,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(PaletaUsuario.AzulCalendario)
                .clickable { },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("▣", fontSize = 24.sp, color = PaletaUsuario.Texto)
            Text(
                text = "Ver mi calendario",
                modifier = Modifier.padding(start = 9.dp),
                color = PaletaUsuario.Texto,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
