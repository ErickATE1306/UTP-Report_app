package com.example.utpreportapp.ui.screens.usuario.modelo

import androidx.compose.ui.graphics.Color

internal enum class TipoIconoCabecera {
    CARNET,
    CORREO,
    NOTIFICACION,
    REPORTAR,
}

internal data class ElementoInteres(
    val titulo: String,
    val simbolo: String,
    val colorAcento: Color,
)

internal data class ElementoReserva(
    val titulo: String,
    val simbolo: String,
)
