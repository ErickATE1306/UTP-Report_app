package com.example.utpreportapp.ui.screens.seguridad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SecurityHomeScreen(
    reports: List<AssignedReport>,
    isAvailable: Boolean,
    onAvailabilityChange: (Boolean) -> Unit,
    onViewCase: (AssignedReport) -> Unit,
    modifier: Modifier = Modifier,
    showDashboard: Boolean = true,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SecurityColors.Background),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(SecurityColors.CoralLight, SecurityColors.Coral),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 26.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "UTP Reporta Seguridad",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Seguridad",
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            color = SecurityColors.Ink,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(26.dp))
                    Text(
                        text = if (showDashboard) "Hola, Carlos" else "Reportes asignados",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(18.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Estado: ",
                                color = SecurityColors.Ink,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = if (isAvailable) "Disponible" else "No disponible",
                                modifier = Modifier.weight(1f),
                                color = if (isAvailable) SecurityColors.Green else SecurityColors.Coral,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Switch(
                                checked = isAvailable,
                                onCheckedChange = onAvailabilityChange,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SecurityColors.Purple,
                                    uncheckedTrackColor = SecurityColors.Muted,
                                ),
                            )
                        }
                    }
                }
            }
        }

        if (showDashboard) {
            item {
                SummaryCard(
                    reports = reports,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                )
            }
        }

        item {
            Text(
                text = "Mis reportes asignados",
                modifier = Modifier.padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = if (showDashboard) 4.dp else 20.dp,
                    bottom = 12.dp,
                ),
                color = SecurityColors.Ink,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        if (reports.isEmpty()) {
            item {
                Text(
                    text = "No tienes reportes asignados.",
                    modifier = Modifier.padding(20.dp),
                    color = SecurityColors.Muted,
                )
            }
        } else {
            items(reports, key = { it.id }) { report ->
                SecurityReportCard(
                    report = report,
                    onViewCase = onViewCase,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                )
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun SummaryCard(
    reports: List<AssignedReport>,
    modifier: Modifier,
) {
    Box(modifier = modifier) {
        com.example.utpreportapp.ui.screens.seguridad.SummaryCard(reports)
    }
}
