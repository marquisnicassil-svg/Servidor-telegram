package com.example.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.ConsoleLogItem
import com.example.ui.viewmodel.LogType

@Composable
fun LogsTab(
    logs: List<ConsoleLogItem>,
    onClearLogs: () -> Unit,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "SUCCESS" -> item.type == LogType.SUCCESS
                "WARNING" -> item.type == LogType.WARNING
                "ERROR" -> item.type == LogType.ERROR
                "TELEGRAM" -> item.type == LogType.TG_IN || item.type == LogType.TG_OUT
                "AI" -> item.type == LogType.AI_SYS
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                item.message.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.scrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SynapseSectionHeader(
            eyebrow = "MONITORAMENTO",
            title = "Terminal de Comandos",
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            onHaptic()
                            if (filteredLogs.isNotEmpty()) {
                                val text = filteredLogs.joinToString("\n") {
                                    "[${it.formattedTime}] [${it.type.name}] ${it.message}"
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                android.widget.Toast.makeText(context, "Logs copiados!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardSurface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copiar Logs",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            onHaptic()
                            onClearLogs()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.danger.copy(alpha = 0.1f))
                            .border(1.dp, colors.danger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Limpar Terminal",
                            tint = colors.danger,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        )

        SynapseOutlinedField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Filtrar logs por palavra-chave...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Limpar busca",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        val filterChips = listOf(
            "ALL" to "Todos (${logs.size})",
            "TELEGRAM" to "Telegram",
            "AI" to "IA",
            "SUCCESS" to "Sucesso",
            "WARNING" to "Avisos",
            "ERROR" to "Erros"
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterChips) { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.accent.copy(alpha = 0.16f) else colors.cardSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) colors.accent else colors.borderSubtle
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .clickable {
                            onHaptic()
                            selectedFilter = key
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) colors.accent else colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF03050A))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = colors.textMuted.copy(alpha = 0.45f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (logs.isEmpty())
                            "Nenhum evento registrado ainda.\nA telemetria do servidor aparecerá aqui em tempo real."
                        else
                            "Nenhum log corresponde ao filtro selecionado.",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs, key = { it.id }) { logItem ->
                        val (tagLabel, logColor) = when (logItem.type) {
                            LogType.INFO -> "INFO" to Color(0xFF94A3B8)
                            LogType.SUCCESS -> "OK" to colors.success
                            LogType.WARNING -> "WARN" to colors.warning
                            LogType.ERROR -> "ERR" to colors.danger
                            LogType.TG_IN -> "TG_IN" to colors.accent
                            LogType.TG_OUT -> "TG_OUT" to Color(0xFFC084FC)
                            LogType.AI_SYS -> "AI" to Color(0xFFF472B6)
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = logItem.formattedTime,
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(logColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = tagLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = logColor
                                )
                            }
                            Text(
                                text = logItem.message,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = logColor.copy(alpha = 0.95f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
