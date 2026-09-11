package com.example.utpreportapp.ui.screens.usuario

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.utpreportapp.ui.screens.usuario.componentes.BarraNavegacionUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.CabeceraUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.CarruselAnunciosUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.CarruselInteresesUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.CarruselReservasUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.TarjetaModalidadUsuario
import com.example.utpreportapp.ui.screens.usuario.componentes.TituloSeccionUsuario
import com.example.utpreportapp.ui.screens.usuario.tema.PaletaUsuario

@Composable
fun PantallaInicioUsuario(modifier: Modifier = Modifier) {
    var seccionSeleccionada by rememberSaveable { mutableStateOf("Inicio") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaletaUsuario.Fondo,
        bottomBar = {
            BarraNavegacionUsuario(
                seleccionada = seccionSeleccionada,
                alSeleccionar = { seccionSeleccionada = it },
            )
        },
    ) { rellenoContenido ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenoContenido),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { CabeceraUsuario() }
            item { TarjetaModalidadUsuario() }
            item {
                TituloSeccionUsuario(
                    texto = "Anuncios",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                )
            }
            item { CarruselAnunciosUsuario() }
            item {
                TituloSeccionUsuario(
                    texto = "Te puede interesar:",
                    modifier = Modifier.padding(start = 16.dp, top = 26.dp, bottom = 14.dp),
                )
            }
            item { CarruselInteresesUsuario() }
            item {
                TituloSeccionUsuario(
                    texto = "Genera una reserva de:",
                    modifier = Modifier.padding(start = 16.dp, top = 26.dp, bottom = 14.dp),
                )
            }
            item { CarruselReservasUsuario() }
        }
    }
}
