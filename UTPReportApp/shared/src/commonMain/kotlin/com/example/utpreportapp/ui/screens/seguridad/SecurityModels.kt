package com.example.utpreportapp.ui.screens.seguridad

enum class ReportPriority(val label: String) {
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja"),
}

enum class SecurityReportStatus(val label: String) {
    ASSIGNED("Asignado"),
    EN_ROUTE("En ruta"),
    INVESTIGATING("Investigando"),
    COMPLETED("Completado"),
}

data class AssignedReport(
    val id: String,
    val priority: ReportPriority,
    val incident: String,
    val location: String,
    val description: String,
    val dateTime: String,
    val reporter: String,
    val status: SecurityReportStatus,
)

internal fun sampleSecurityReports(): List<AssignedReport> = listOf(
    AssignedReport(
        id = "R-1038",
        priority = ReportPriority.HIGH,
        incident = "Robo de pertenencias",
        location = "Biblioteca",
        description = "El estudiante reporta la pérdida de una mochila cerca de la sala de lectura.",
        dateTime = "Hoy, 12:45 p. m.",
        reporter = "Anónimo",
        status = SecurityReportStatus.ASSIGNED,
    ),
    AssignedReport(
        id = "R-1035",
        priority = ReportPriority.MEDIUM,
        incident = "Accidente",
        location = "Canchas",
        description = "Se reportó una caída durante una actividad deportiva.",
        dateTime = "Hoy, 11:20 a. m.",
        reporter = "María López",
        status = SecurityReportStatus.INVESTIGATING,
    ),
    AssignedReport(
        id = "R-1032",
        priority = ReportPriority.LOW,
        incident = "Objeto extraviado",
        location = "Pabellón A",
        description = "Se encontró una billetera en el pasillo del primer piso.",
        dateTime = "Hoy, 9:10 a. m.",
        reporter = "Carlos Pérez",
        status = SecurityReportStatus.EN_ROUTE,
    ),
)

enum class SecurityDestination(val label: String, val symbol: String) {
    HOME("Inicio", "⌂"),
    ASSIGNED("Asignados", "▤"),
    ZONES("Zonas", "⌖"),
    PROFILE("Perfil", "○"),
}
