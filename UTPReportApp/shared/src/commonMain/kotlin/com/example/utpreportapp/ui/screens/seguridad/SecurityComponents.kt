package com.example.utpreportapp.ui.screens.seguridad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SecurityBottomNavigation(
    selected: SecurityDestination,
    onSelect: (SecurityDestination) -> Unit,
) {
    NavigationBar(containerColor = Color.White) {
        SecurityDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = {
                    Text(
                        text = destination.symbol,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SecurityColors.Coral,
                    selectedTextColor = SecurityColors.Coral,
                    indicatorColor = SecurityColors.PurpleSoft,
                    unselectedIconColor = SecurityColors.Ink,
                    unselectedTextColor = SecurityColors.Ink,
                ),
            )
        }
    }
}

@Composable
internal fun SummaryCard(reports: List<AssignedReport>) {
    val values = listOf(
        Triple("Asignados", reports.count { it.status == SecurityReportStatus.ASSIGNED }, SecurityColors.Purple),
        Triple("En ruta", reports.count { it.status == SecurityReportStatus.EN_ROUTE }, Color(0xFF3D75A6)),
        Triple("Investigando", reports.count { it.status == SecurityReportStatus.INVESTIGATING }, SecurityColors.Orange),
        Triple("Completados", reports.count { it.status == SecurityReportStatus.COMPLETED }, SecurityColors.Green),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            values.forEach { (label, value, color) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = color,
                        content = {},
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = SecurityColors.Muted,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = value.toString(),
                        color = SecurityColors.Ink,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityReportCard(
    report: AssignedReport,
    onViewCase: (AssignedReport) -> Unit,
    modifier: Modifier = Modifier,
) {
    val priorityColor = when (report.priority) {
        ReportPriority.HIGH -> SecurityColors.Coral
        ReportPriority.MEDIUM -> SecurityColors.Orange
        ReportPriority.LOW -> SecurityColors.Green
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = priorityColor.copy(alpha = 0.12f),
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("!", color = priorityColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${report.id}",
                        color = SecurityColors.Purple,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = report.incident,
                        color = SecurityColors.Ink,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = priorityColor.copy(alpha = 0.14f),
                        ) {
                            Text(
                                text = report.priority.label,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                color = priorityColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("⌖ ${report.location}", color = SecurityColors.Ink)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Estado actual: ${report.status.label}",
                    modifier = Modifier
                        .weight(1f)
                        .background(SecurityColors.PurpleSoft, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    color = SecurityColors.Purple,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onViewCase(report) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityColors.Purple),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Ver caso")
                }
            }
        }
    }
}
