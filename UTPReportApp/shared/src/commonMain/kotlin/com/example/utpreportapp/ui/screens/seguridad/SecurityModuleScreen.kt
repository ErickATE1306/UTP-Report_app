package com.example.utpreportapp.ui.screens.seguridad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SecurityModuleScreen(modifier: Modifier = Modifier) {
    var reports by remember { mutableStateOf(sampleSecurityReports()) }
    var isAvailable by rememberSaveable { mutableStateOf(true) }
    var destinationName by rememberSaveable { mutableStateOf(SecurityDestination.HOME.name) }
    var selectedReportId by rememberSaveable { mutableStateOf<String?>(null) }

    val destination = SecurityDestination.valueOf(destinationName)
    val selectedReport = reports.firstOrNull { it.id == selectedReportId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            SecurityBottomNavigation(
                selected = destination,
                onSelect = { selected ->
                    destinationName = selected.name
                    selectedReportId = null
                },
            )
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            when {
                selectedReport != null -> ReportDetailScreen(
                    report = selectedReport,
                    onBack = { selectedReportId = null },
                    onConfirmDeparture = { reportId ->
                        reports = reports.map { report ->
                            if (report.id == reportId && report.status == SecurityReportStatus.ASSIGNED) {
                                report.copy(status = SecurityReportStatus.EN_ROUTE)
                            } else {
                                report
                            }
                        }
                    },
                )

                destination == SecurityDestination.HOME -> SecurityHomeScreen(
                    reports = reports,
                    isAvailable = isAvailable,
                    onAvailabilityChange = { isAvailable = it },
                    onViewCase = { selectedReportId = it.id },
                )

                destination == SecurityDestination.ASSIGNED -> SecurityHomeScreen(
                    reports = reports.filter { it.status != SecurityReportStatus.COMPLETED },
                    isAvailable = isAvailable,
                    onAvailabilityChange = { isAvailable = it },
                    onViewCase = { selectedReportId = it.id },
                    showDashboard = false,
                )

                destination == SecurityDestination.ZONES -> ModulePlaceholder(
                    title = "Estado de zonas",
                    message = "La gestión de zonas se implementará en su historia de usuario.",
                )

                else -> ModulePlaceholder(
                    title = "Perfil de seguridad",
                    message = "Aquí se mostrará la información del personal autenticado.",
                )
            }
        }
    }
}

@Composable
private fun ModulePlaceholder(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecurityColors.Background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = SecurityColors.Ink,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 12.dp),
            color = SecurityColors.Muted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
