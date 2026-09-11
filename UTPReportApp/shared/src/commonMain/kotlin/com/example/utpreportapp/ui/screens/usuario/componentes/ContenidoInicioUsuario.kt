package com.example.utpreportapp.ui.screens.usuario.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utpreportapp.ui.screens.usuario.modelo.ElementoInteres
import com.example.utpreportapp.ui.screens.usuario.modelo.ElementoReserva
import com.example.utpreportapp.ui.screens.usuario.tema.PaletaUsuario

@Composable
internal fun TarjetaModalidadUsuario() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, Color(0xFF8595C2), RoundedCornerShape(9.dp))
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = PaletaUsuario.MoradoClaro) {
            Text(
                "⚑",
                modifier = Modifier.padding(8.dp),
                color = PaletaUsuario.Morado,
                fontSize = 18.sp,
            )
        }
        Text(
            text = "Estás inscrito en: ",
            modifier = Modifier.padding(start = 10.dp),
            color = PaletaUsuario.Texto,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Modalidad presencial",
            color = PaletaUsuario.Texto,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun TituloSeccionUsuario(
    texto: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        modifier = modifier,
        color = PaletaUsuario.Texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
    )
}

@Composable
internal fun CarruselAnunciosUsuario() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(
                modifier = Modifier
                    .width(304.dp)
                    .height(166.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                    ) {
                        Text(
                            "¡Tu opinión es clave para mejorar!",
                            color = PaletaUsuario.Texto,
                            fontSize = 20.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "Ayúdanos compartiendo tu experiencia",
                            modifier = Modifier.padding(top = 7.dp),
                            color = PaletaUsuario.Texto,
                            fontSize = 14.sp,
                        )
                        Text(
                            "Completar encuestas pendientes ↗",
                            modifier = Modifier.padding(top = 10.dp),
                            color = PaletaUsuario.Morado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                    IlustracionEncuesta(
                        modifier = Modifier
                            .width(95.dp)
                            .fillMaxSize(),
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .height(166.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "¡Ahora paga tus cuotas en línea!",
                        color = PaletaUsuario.Texto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Revisa tus próximos vencimientos desde la sección Pagos.",
                        modifier = Modifier.padding(top = 12.dp),
                        color = PaletaUsuario.TextoSecundario,
                    )
                }
            }
        }
    }
}

@Composable
private fun IlustracionEncuesta(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            PaletaUsuario.Morado.copy(alpha = 0.12f),
            size.minDimension * 0.12f,
            Offset(size.width * 0.36f, size.height * 0.22f),
        )
        drawRoundRect(
            PaletaUsuario.Coral,
            Offset(size.width * 0.22f, size.height * 0.43f),
            Size(size.width * 0.7f, size.height * 0.5f),
            CornerRadius(8.dp.toPx()),
            style = Stroke(6.dp.toPx()),
        )
        drawRoundRect(
            PaletaUsuario.CoralClaro,
            Offset(size.width * 0.35f, size.height * 0.35f),
            Size(size.width * 0.4f, size.height * 0.12f),
            CornerRadius(5.dp.toPx()),
        )
        repeat(3) { indice ->
            val posicionY = size.height * (0.56f + indice * 0.11f)
            drawLine(
                PaletaUsuario.CoralClaro,
                Offset(size.width * 0.37f, posicionY),
                Offset(size.width * 0.58f, posicionY),
                3.dp.toPx(),
                StrokeCap.Round,
            )
            drawLine(
                PaletaUsuario.Morado,
                Offset(size.width * 0.67f, posicionY),
                Offset(size.width * 0.72f, posicionY + 4.dp.toPx()),
                2.dp.toPx(),
                StrokeCap.Round,
            )
            drawLine(
                PaletaUsuario.Morado,
                Offset(size.width * 0.72f, posicionY + 4.dp.toPx()),
                Offset(size.width * 0.8f, posicionY - 5.dp.toPx()),
                2.dp.toPx(),
                StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun CarruselInteresesUsuario() {
    val elementos = listOf(
        ElementoInteres("UTP Reporta", "!", PaletaUsuario.Coral),
        ElementoInteres("Ruta de nuevos\nalumnos", "↝", Color(0xFFFF6D83)),
        ElementoInteres("Bolsa de trabajo", "✦", Color(0xFFFFB13B)),
        ElementoInteres("Beneficios", "◆", Color(0xFFFF7187)),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(elementos) { elemento -> TarjetaInteresUsuario(elemento) }
    }
}

@Composable
private fun TarjetaInteresUsuario(elemento: ElementoInteres) {
    Card(
        modifier = Modifier
            .width(145.dp)
            .height(155.dp)
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = PaletaUsuario.AzulOscuro),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1D263B)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = elemento.colorAcento.copy(alpha = 0.16f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            elemento.simbolo,
                            color = elemento.colorAcento,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = elemento.titulo,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("→", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}

@Composable
internal fun CarruselReservasUsuario() {
    val elementos = listOf(
        ElementoReserva("Refuerzo\nAcadémico", "✎"),
        ElementoReserva("Eventos", "▢"),
        ElementoReserva("Citas y\nAsesorías", "▣"),
        ElementoReserva("Recursos", "≡"),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(elementos) { elemento ->
            Card(
                modifier = Modifier
                    .width(128.dp)
                    .height(118.dp)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(elemento.simbolo, color = PaletaUsuario.Texto, fontSize = 31.sp)
                    Text(
                        elemento.titulo,
                        modifier = Modifier.padding(top = 7.dp),
                        color = PaletaUsuario.Texto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
