package com.example.utpreportapp.ui.screens.usuario.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utpreportapp.ui.screens.usuario.tema.PaletaUsuario

@Composable
internal fun BarraNavegacionUsuario(
    seleccionada: String,
    alSeleccionar: (String) -> Unit,
) {
    val secciones = listOf(
        "Inicio" to "⌂",
        "Calendario" to "▣",
        "Cursos" to "▤",
        "Pagos" to "▧",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            secciones.forEach { (etiqueta, simbolo) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { alSeleccionar(etiqueta) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        simbolo,
                        color = if (seleccionada == etiqueta) PaletaUsuario.AzulOscuro else PaletaUsuario.TextoSecundario,
                        fontSize = 25.sp,
                    )
                    Text(
                        etiqueta,
                        color = if (seleccionada == etiqueta) PaletaUsuario.Texto else PaletaUsuario.TextoSecundario,
                        fontSize = 11.sp,
                        fontWeight = if (seleccionada == etiqueta) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(52.dp)
                    .background(PaletaUsuario.CoralClaro, RoundedCornerShape(9.dp))
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}
