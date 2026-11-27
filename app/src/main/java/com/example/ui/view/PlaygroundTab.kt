package com.example.ui.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.database.BotMessageEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel
import com.example.ui.viewmodel.LogType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlaygroundTab(
    viewModel: BotViewModel,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val messages by viewModel.localTestMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAiThinkingLocal.collectAsStateWithLifecycle()
    val config by viewModel.configState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.addLog("[STEP 1] Tela carregada - UI do Playground carregada de forma reativa", LogType.INFO)
        viewModel.addLog("[STEP 3] Eventos registrados - Escutadores de toque (onClick) e inserção de texto inicializados no botão Enviar.", LogType.INFO)
    }

    DisposableEffect(Unit) {
        viewModel.addLog("[STEP 2] Widgets renderizados - Componentes do container de conversas desenhados", LogType.INFO)
        onDispose {
            viewModel.addLog("[STEP 8] Renderização concluída - Árvore de UI do Playground reciclada na destruição", LogType.INFO)
        }
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val dispatchMessage = { textToSend: String ->
        viewModel.addLog("[STEP 4] Clique recebido - Clique detectado no botão Enviar nativo", LogType.INFO)
        val trimmed = textToSend.trim()
        val dotFree = trimmed.replace(".", "").trim()
        viewModel.addLog("[STEP 5] onPressed executado - Evento onClick processado, com validação de campo concluída. Conteúdo: '$trimmed'", LogType.INFO)

        if (trimmed.isNotEmpty() && dotFree.isNotEmpty()) {
            onHaptic()
            viewModel.addLog("[STEP 6] Mensagem enviada ao controlador - Despachando corpo da mensagem para o ViewModel local", LogType.INFO)
            viewModel.sendLocalMockMessage(trimmed)
            viewModel.addLog("[STEP 7] Atualização da interface - Limpando inputText e atualizando os estados da UI", LogType.INFO)
            inputText = ""
        } else {
            viewModel.addLog("[ALERT] Validação do input falhou no clique do botão", LogType.WARNING)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Compact Model & Session Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.cardSurface,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = config?.aiModel?.ifBlank { "gemini-1.5-flash" } ?: "gemini-1.5-flash",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "• T ${String.format(Locale.US, "%.1f", config?.temperature ?: 0.7f)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }

            if (messages.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onHaptic()
                        viewModel.clearLocalHistory()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Limpar",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.danger
                    )
                }
            }
        }

        // Chat Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surfaceLow)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(22.dp))
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ai_core_orb_1795777695231),
                        contentDescription = "Núcleo Synapse IA",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(2.dp, colors.accent.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sandbox Neural Synapse",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Teste seu prompt de sistema, envie URLs para análise ou experimente comandos rápidos abaixo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val suggestions = listOf(
                        "Quais comandos você suporta?",
                        "Resuma boas práticas de Kotlin",
                        "Crie uma saudação para meu bot"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(suggestions) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.cardSurface,
                                border = BorderStroke(1.dp, colors.borderStrong),
                                modifier = Modifier.clickable {
                                    dispatchMessage(prompt)
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { message ->
                        ChatBubbleItem(
                            message = message,
                            allMessages = messages,
                            onCopy = {
                                onHaptic()
                                clipboardManager.setText(AnnotatedString(message.messageText))
                                android.widget.Toast.makeText(context, "Mensagem copiada", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onSpeak = {
                                onHaptic()
                                viewModel.speakText(message.messageText)
                            }
                        )
                    }

                    if (isThinking) {
                        item {
                            ThinkingIndicatorBubble(reduceMotion = colors.reduceMotion)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Modern Floating Composer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.cardSurface)
                .border(1.dp, colors.borderStrong, RoundedCornerShape(22.dp))
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Mensagem ou URL para analisar...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("playground_msg_input"),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accent
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (!isThinking && inputText.isNotBlank()) {
                            dispatchMessage(inputText)
                        }
                    }
                )
            )

            val canSend = !isThinking && inputText.isNotBlank() && inputText.trim().replace(".", "").trim().isNotEmpty()
            Button(
                onClick = { dispatchMessage(inputText) },
                enabled = canSend,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                    disabledContainerColor = colors.cardElevated,
                    disabledContentColor = colors.textMuted
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("playground_send_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: BotMessageEntity,
    allMessages: List<BotMessageEntity>,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val colors = LocalSynapseColors.current
    val isAI = message.isBotReply
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
    ) {
        if (isAI) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_core_orb_1795777695231),
                contentDescription = "IA",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isAI) 4.dp else 18.dp,
                        bottomEnd = if (isAI) 18.dp else 4.dp
                    )
                )
                .then(
                    if (isAI) {
                        Modifier
                            .background(colors.cardSurface)
                            .border(
                                1.dp,
                                colors.borderSubtle,
                                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                            )
                    } else {
                        Modifier.background(
                            Brush.linearGradient(
                                listOf(colors.accent, colors.accent.copy(alpha = 0.85f))
                            )
                        )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAI) "Synapse IA" else "Você",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAI) colors.accent else colors.onAccent.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeStr,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = if (isAI) colors.textMuted else colors.onAccent.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAI) colors.textPrimary else colors.onAccent
            )

            if (isAI) {
                // Preserve Context Browser "Abrir Página Original" button detection
                val currentIndex = allMessages.indexOf(message)
                val urlToUse = remember(currentIndex, allMessages) {
                    if (currentIndex > 0) {
                        val prevUserMsg = allMessages.take(currentIndex).lastOrNull { !it.isBotReply }
                        if (prevUserMsg != null && prevUserMsg.messageText.contains("Navegador de Contexto")) {
                            val regex = """https?://[^\s)]+""".toRegex()
                            val match = regex.find(prevUserMsg.messageText)?.value
                            if (match != null) match
                            else {
                                val urlIndex = prevUserMsg.messageText.indexOf("de: ")
                                if (urlIndex != -1) prevUserMsg.messageText.substring(urlIndex + 4).trim() else null
                            }
                        } else null
                    } else null
                }

                if (urlToUse != null && urlToUse.startsWith("http")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val uriHandler = LocalUriHandler.current
                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri(urlToUse)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent.copy(alpha = 0.16f),
                            contentColor = colors.accent
                        ),
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_original_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Abrir Página Original",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copiar resposta",
                            tint = colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = "Ouvir resposta",
                            tint = colors.textMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicatorBubble(reduceMotion: Boolean) {
    val colors = LocalSynapseColors.current
    val dot1Alpha: Float
    val dot2Alpha: Float
    val dot3Alpha: Float

    if (!reduceMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
        dot1Alpha = infiniteTransition.animateFloat(
            initialValue = 0.25f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
            label = "d1"
        ).value
        dot2Alpha = infiniteTransition.animateFloat(
            initialValue = 0.25f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(500, delayMillis = 160), RepeatMode.Reverse),
            label = "d2"
        ).value
        dot3Alpha = infiniteTransition.animateFloat(
            initialValue = 0.25f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(500, delayMillis = 320), RepeatMode.Reverse),
            label = "d3"
        ).value
    } else {
        dot1Alpha = 0.8f; dot2Alpha = 0.8f; dot3Alpha = 0.8f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_ai_core_orb_1795777695231),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.cardSurface,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = alpha))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Processando...",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted
                )
            }
        }
    }
}
