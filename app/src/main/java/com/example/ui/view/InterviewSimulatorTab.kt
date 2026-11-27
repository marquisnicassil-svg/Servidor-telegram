package com.example.ui.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SynapseBottomSheetPicker
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterviewSimulatorTab(
    viewModel: BotViewModel,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val cargo by viewModel.interviewCargo.collectAsStateWithLifecycle()
    val area by viewModel.interviewArea.collectAsStateWithLifecycle()
    val nivel by viewModel.interviewNivel.collectAsStateWithLifecycle()

    val isRunning by viewModel.isInterviewRunning.collectAsStateWithLifecycle()
    val history by viewModel.interviewHistory.collectAsStateWithLifecycle()
    val progress by viewModel.interviewProgress.collectAsStateWithLifecycle()
    val isEvaluating by viewModel.isInterviewEvaluating.collectAsStateWithLifecycle()
    val resultModel by viewModel.interviewResultModel.collectAsStateWithLifecycle()
    val isThinking by viewModel.isRecruiterThinking.collectAsStateWithLifecycle()
    val interviews by viewModel.allInterviews.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    BackHandler(enabled = resultModel != null || isRunning) {
        if (resultModel != null) {
            viewModel.interviewResultModel.value = null
        } else if (isRunning) {
            viewModel.isInterviewRunning.value = false
        }
    }

    LaunchedEffect(history.size, isThinking) {
        if (history.isNotEmpty()) {
            lazyListState.animateScrollToItem(history.size - 1)
        }
    }

    when {
        isEvaluating -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.accent,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Avaliando sua performance...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Analisando clareza, comunicação e profundidade técnica para gerar o relatório de competências.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        resultModel != null -> {
            val r = resultModel!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SynapseSurfaceCard(
                    accentBorder = colors.success,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RELATÓRIO DE ENTREVISTA",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = r.cargo,
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${r.nivel} • ${r.area}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        ScoreCircularGauge(score = r.scoreGeneral, accent = colors.success)
                    }
                }

                SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "DESEMPENHO POR COMPETÊNCIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent
                        )
                        InterviewMetricBar("Comunicação", r.scoreCommunication, colors.accent)
                        InterviewMetricBar("Clareza das Respostas", r.scoreClarity, colors.secondaryAccent)
                        InterviewMetricBar("Conhecimento Técnico", r.scoreTechnical, colors.warning)
                        InterviewMetricBar("Confiança", r.scoreConfidence, Color(0xFFEC4899))
                    }
                }

                FeedbackCard(
                    title = "Pontos Fortes",
                    body = r.strengths,
                    icon = Icons.Rounded.CheckCircleOutline,
                    tint = colors.success
                )
                FeedbackCard(
                    title = "Pontos a Melhorar",
                    body = r.improvements,
                    icon = Icons.Rounded.TrendingUp,
                    tint = colors.danger
                )
                FeedbackCard(
                    title = "Recomendações Personalizadas",
                    body = r.recommendations,
                    icon = Icons.Rounded.Lightbulb,
                    tint = colors.accent
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onHaptic()
                            viewModel.startInterviewMatch()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refazer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onHaptic()
                            val shareText = "Simulador de Entrevistas Synapse IA\n\n" +
                                "Cargo: ${r.cargo} (${r.nivel})\n" +
                                "Nota Geral: ${r.scoreGeneral}/100\n" +
                                "Comunicação: ${r.scoreCommunication} | Clareza: ${r.scoreClarity} | Técnico: ${r.scoreTechnical} | Confiança: ${r.scoreConfidence}\n\n" +
                                "Pontos Fortes: ${r.strengths}\n" +
                                "Recomendações: ${r.recommendations}"
                            try {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("RelatorioEntrevista", shareText))
                                android.widget.Toast.makeText(context, "Relatório copiado!", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {}
                        },
                        border = BorderStroke(1.dp, colors.borderStrong),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartilhar", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }

                TextButton(
                    onClick = {
                        onHaptic()
                        viewModel.interviewResultModel.value = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Painel de Entrevistas", color = colors.textSecondary)
                }
            }
        }

        isRunning -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ENTREVISTA EM ANDAMENTO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.accent
                                )
                                Text(
                                    text = "$cargo • $nivel",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$progress / 5",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (step in 1..5) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(CircleShape)
                                        .background(if (step <= progress) colors.accent else colors.borderSubtle)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(history) { chatItem ->
                        val isCandidate = chatItem.sender == "CANDIDATO"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isCandidate) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = if (isCandidate) "Você" else "Recrutador IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCandidate) colors.accent else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isCandidate) 16.dp else 4.dp,
                                            bottomEnd = if (isCandidate) 4.dp else 16.dp
                                        )
                                    )
                                    .background(if (isCandidate) colors.accent else colors.cardSurface)
                                    .border(
                                        1.dp,
                                        if (isCandidate) Color.Transparent else colors.borderSubtle,
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isCandidate) 16.dp else 4.dp,
                                            bottomEnd = if (isCandidate) 4.dp else 16.dp
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = chatItem.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCandidate) colors.onAccent else colors.textPrimary
                                )
                            }
                        }
                    }

                    if (isThinking) {
                        item {
                            Text(
                                text = "Recrutador analisando resposta...",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardSurface)
                        .border(1.dp, colors.borderStrong, RoundedCornerShape(22.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Escreva sua resposta...", color = colors.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val trimmed = textInput.trim()
                                if (trimmed.isNotEmpty() && !isThinking) {
                                    onHaptic()
                                    viewModel.submitInterviewAnswer(trimmed)
                                    textInput = ""
                                }
                            }
                        ),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            val trimmed = textInput.trim()
                            val dotFree = trimmed.replace(".", "").trim()
                            if (trimmed.isNotEmpty() && dotFree.isNotEmpty() && !isThinking) {
                                onHaptic()
                                viewModel.submitInterviewAnswer(trimmed)
                                textInput = ""
                            }
                        },
                        enabled = !isThinking && textInput.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (!isThinking && textInput.isNotBlank()) colors.accent else colors.cardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Enviar Resposta",
                            tint = if (!isThinking && textInput.isNotBlank()) colors.onAccent else colors.textMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SynapseSurfaceCard(
                    accentBorder = colors.accent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "CARREIRA & TREINAMENTO IA",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simulador de Entrevistas",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pratique entrevistas técnicas e comportamentais em 5 etapas com feedback detalhado por competências.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                SynapseSectionHeader(
                    eyebrow = "CONFIGURAÇÃO",
                    title = "Defina a Vaga"
                )

                val presetJobs = listOf(
                    "Desenvolvedor Mobile Android",
                    "Fullstack Engineer (React / Node)",
                    "Designer UX/UI",
                    "Gerente de Produto (PM)",
                    "Analista de Dados / BI"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetJobs.forEach { job ->
                        val isSelected = cargo == job
                        Surface(
                            onClick = {
                                onHaptic()
                                viewModel.interviewCargo.value = job
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.accent else colors.cardSurface,
                            border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                        ) {
                            Text(
                                text = job,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) colors.onAccent else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                SynapseOutlinedField(
                    value = cargo,
                    onValueChange = { viewModel.interviewCargo.value = it },
                    label = "Cargo específico",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var areaPickerOpen by remember { mutableStateOf(false) }
                    var levelPickerOpen by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = "Área",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Surface(
                            onClick = { onHaptic(); areaPickerOpen = true },
                            shape = RoundedCornerShape(14.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(1.dp, colors.borderStrong),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(area, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (areaPickerOpen) {
                            SynapseBottomSheetPicker(
                                title = "Área Profissional",
                                options = listOf("Tecnologia", "Design", "Vendas", "Finanças", "RH"),
                                selectedOption = area,
                                optionLabel = { it },
                                optionIcon = { a ->
                                    when (a.lowercase()) {
                                        "tecnologia" -> "💻"
                                        "design" -> "🎨"
                                        "vendas" -> "📈"
                                        "finanças" -> "💰"
                                        "rh" -> "👥"
                                        else -> "💼"
                                    }
                                },
                                onDismissRequest = { areaPickerOpen = false },
                                onOptionSelected = { viewModel.interviewArea.value = it }
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Senioridade",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Surface(
                            onClick = { onHaptic(); levelPickerOpen = true },
                            shape = RoundedCornerShape(14.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(1.dp, colors.borderStrong),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nivel, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (levelPickerOpen) {
                            SynapseBottomSheetPicker(
                                title = "Senioridade",
                                options = listOf("Estágio", "Júnior", "Pleno", "Sênior"),
                                selectedOption = nivel,
                                optionLabel = { it },
                                optionIcon = { n ->
                                    when (n.lowercase()) {
                                        "estágio" -> "🌱"
                                        "júnior" -> "🚀"
                                        "pleno" -> "🔥"
                                        "sênior" -> "👑"
                                        else -> "✦"
                                    }
                                },
                                onDismissRequest = { levelPickerOpen = false },
                                onOptionSelected = { viewModel.interviewNivel.value = it }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onHaptic()
                        viewModel.startInterviewMatch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciar Entrevista com IA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (interviews.isNotEmpty()) {
                    SynapseSectionHeader(
                        eyebrow = "HISTÓRICO",
                        title = "Suas Avaliações (${interviews.size})"
                    )

                    val averageScore = interviews.map { it.scoreGeneral }.average().toInt()
                    SynapseSurfaceCard(
                        accentBorder = colors.accent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ScoreCircularGauge(score = averageScore, accent = colors.accent, sizeDp = 56)
                            Column {
                                Text(
                                    text = "Média de Performance",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Média geral de $averageScore/100 em ${interviews.size} simulações realizadas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }

                    interviews.forEach { record ->
                        SynapseSurfaceCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onHaptic()
                                viewModel.interviewResultModel.value = record
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.cargo,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${record.nivel} • ${record.area}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                    Text(
                                        text = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(record.timestamp)),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.success.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, colors.success.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "${record.scoreGeneral}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colors.success,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            onHaptic()
                                            viewModel.deleteInterviewItem(record.id)
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Apagar Registro",
                                            tint = colors.danger,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCircularGauge(
    score: Int,
    accent: Color,
    sizeDp: Int = 76
) {
    val colors = LocalSynapseColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = if (colors.reduceMotion) 0 else 800),
        label = "score_gauge"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(sizeDp.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 6.dp.toPx()
            drawArc(
                color = colors.borderSubtle,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = if (sizeDp >= 70) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            if (sizeDp >= 70) {
                Text(
                    text = "NOTA",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    val colors = LocalSynapseColors.current
    SynapseSurfaceCard(
        accentBorder = tint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tint.copy(alpha = 0.06f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
            }
        }
    }
}

@Composable
fun InterviewMetricBar(label: String, score: Int, color: Color) {
    val colors = LocalSynapseColors.current
    val animatedWidth by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = if (colors.reduceMotion) 0 else 700),
        label = "metric_bar"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Text(
                text = "$score / 100",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(colors.surfaceLow)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedWidth)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
