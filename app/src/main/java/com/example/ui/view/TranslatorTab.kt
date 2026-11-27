package com.example.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SynapseBottomSheetPicker
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel

@Composable
fun TranslatorTab(
    viewModel: BotViewModel,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val translatedText by viewModel.translatedText.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Inglês") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val languages = listOf("Inglês", "Espanhol", "Francês", "Alemão", "Italiano", "Japonês", "Mandarim", "Português")
    val languageFlag: (String) -> String = { lang ->
        when (lang.lowercase()) {
            "inglês" -> "🇺🇸"
            "espanhol" -> "🇪🇸"
            "francês" -> "🇫🇷"
            "alemão" -> "🇩🇪"
            "italiano" -> "🇮🇹"
            "japonês" -> "🇯🇵"
            "mandarim" -> "🇨🇳"
            "português" -> "🇧🇷"
            else -> "🌐"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SynapseSectionHeader(
            eyebrow = "TRADUTOR NEURAL",
            title = "Tradutor Multilíngue",
            subtitle = "Traduções contextuais instantâneas com leitura por voz integrada."
        )

        SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IDIOMA DE DESTINO",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent
                    )
                    if (inputText.isNotEmpty()) {
                        Text(
                            text = "${inputText.length} caracteres",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }

                Surface(
                    onClick = {
                        onHaptic()
                        dropdownExpanded = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceLow,
                    border = BorderStroke(1.dp, colors.borderStrong),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = languageFlag(selectedLanguage), style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = selectedLanguage,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Selecionar idioma",
                            tint = colors.accent
                        )
                    }
                }

                if (dropdownExpanded) {
                    SynapseBottomSheetPicker(
                        title = "Traduzir para...",
                        options = languages,
                        selectedOption = selectedLanguage,
                        optionLabel = { it },
                        optionIcon = languageFlag,
                        onDismissRequest = { dropdownExpanded = false },
                        onOptionSelected = {
                            onHaptic()
                            selectedLanguage = it
                        }
                    )
                }

                SynapseOutlinedField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = "Digite ou cole o texto original aqui...",
                    singleLine = false,
                    maxLines = 6,
                    trailingIcon = if (inputText.isNotEmpty()) {
                        {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Limpar texto",
                                    tint = colors.textMuted
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 132.dp)
                )

                Button(
                    onClick = {
                        onHaptic()
                        viewModel.translateText(inputText, selectedLanguage)
                    },
                    enabled = !isTranslating && inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                        disabledContainerColor = colors.cardElevated,
                        disabledContentColor = colors.textMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.accent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Traduzindo...", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(Icons.Rounded.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Traduzir Agora",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (translatedText.isNotBlank()) {
            SynapseSurfaceCard(
                accentBorder = colors.accent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${languageFlag(selectedLanguage)} TRADUÇÃO ($selectedLanguage)".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onHaptic()
                                    clipboardManager.setText(AnnotatedString(translatedText))
                                    android.widget.Toast.makeText(context, "Tradução copiada!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                border = BorderStroke(1.dp, colors.borderStrong),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copiar", tint = colors.textPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Copiar", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                            }
                            OutlinedButton(
                                onClick = {
                                    onHaptic()
                                    viewModel.speakText(translatedText)
                                },
                                border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                            ) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = "Ouvir", tint = colors.accent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Ouvir", style = MaterialTheme.typography.labelLarge, color = colors.accent)
                            }
                        }
                    }

                    Text(
                        text = translatedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}
