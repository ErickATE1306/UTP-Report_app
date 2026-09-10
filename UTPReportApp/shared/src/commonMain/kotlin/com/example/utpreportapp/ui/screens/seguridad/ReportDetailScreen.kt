package com.example.utpreportapp.ui.screens.seguridad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportDetailScreen(
    report: AssignedReport,
    onBack: () -> Unit,
    onConfirmDeparture: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canConfirmDeparture = report.status == SecurityReportStatus.ASSIGNED

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityColors.Background),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(SecurityColors.CoralLight, SecurityColors.Coral),
                        ),
                    )
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text("← Volver")
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "UTP Reporta Seguridad",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Reporte asignado",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderPill("#${report.id}")
                    HeaderPill("Prioridad ${report.priority.label}")
                }
            }
        }

        item {
            ReportInformationCard(
                report = report,
                modifier = Modifier.padding(16.dp),
            )
        }

        item {
            ProgressCard(
                status = report.status,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            Button(
                onClick = { onConfirmDeparture(report.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .height(54.dp),
                enabled = canConfirmDeparture,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecurityColors.Coral,
                    disabledContainerColor = SecurityColors.Green.copy(alpha = 0.75f),
                    disabledContentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (canConfirmDeparture) "Confirmar salida a zona" else "Salida confirmada · ${report.status.label}",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HeaderPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.92f),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            color = SecurityColors.Ink,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReportInformationCard(report: AssignedReport, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = report.incident,
                color = SecurityColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "⌖ ${report.location}",
                modifier = Modifier.padding(top = 8.dp),
                color = SecurityColors.Muted,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SecurityColors.Border)
            Text(report.description, color = SecurityColors.Ink, style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SecurityColors.Border)
            Text("◷ ${report.dateTime}", color = SecurityColors.Muted)
            Text(
                text = "Reportante: ${report.reporter}",
                modifier = Modifier.padding(top = 8.dp),
                color = SecurityColors.Muted,
            )
        }
    }
}

@Composable
private fun ProgressCard(status: SecurityReportStatus, modifier: Modifier = Modifier) {
    val steps = listOf(
        SecurityReportStatus.ASSIGNED to "Reporte recibido y asignado.",
        SecurityReportStatus.EN_ROUTE to "Confirma que te diriges al lugar.",
        SecurityReportStatus.INVESTIGATING to "Recopila información y evidencias.",
        SecurityReportStatus.COMPLETED to "Finaliza y envía el reporte.",
    )
    val currentIndex = SecurityReportStatus.entries.indexOf(status)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Progreso de atención",
                color = SecurityColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            steps.forEachIndexed { index, (step, description) ->
                val reached = index <= currentIndex
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = if (reached) SecurityColors.Purple else SecurityColors.Border,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(if (reached) "✓" else "", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.padding(bottom = 18.dp)) {
                        Text(
                            text = step.label,
                            color = if (index == currentIndex) SecurityColors.Purple else SecurityColors.Ink,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(description, color = SecurityColors.Muted)
                    }
                }
            }
        }
    }
}
