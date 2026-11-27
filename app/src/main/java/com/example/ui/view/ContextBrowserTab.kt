package com.example.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SynapseOutlinedField
import com.example.ui.components.SynapseSectionHeader
import com.example.ui.components.SynapseSurfaceCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalSynapseColors
import com.example.ui.viewmodel.BotViewModel

@Composable
fun ContextBrowserTab(
    viewModel: BotViewModel,
    onHaptic: () -> Unit = {}
) {
    val colors = LocalSynapseColors.current
    val contextUrl by viewModel.contextUrl.collectAsStateWithLifecycle()
    val extractedStats by viewModel.extractedStats.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isContextProcessing.collectAsStateWithLifecycle()

    var urlInput by remember { mutableStateOf("") }
    var showAddSourceForm by remember { mutableStateOf(false) }

    val defaultResources = remember {
        listOf(
            NewsResource("G1 Tecnologia", "https://g1.globo.com/tecnologia", "Tecnologia"),
            NewsResource("TecMundo", "https://www.tecmundo.com.br", "Tecnologia"),
            NewsResource("TechCrunch", "https://techcrunch.com", "Tecnologia"),
            NewsResource("Globo Esporte", "https://ge.globo.com", "Futebol"),
            NewsResource("Lance!", "https://www.lance.com.br", "Futebol"),
            NewsResource("ESPN Brasil", "https://www.espn.com.br", "Futebol")
        )
    }

    var customResources by remember { mutableStateOf(listOf<NewsResource>()) }
    val combinedResources = defaultResources + customResources

    var selectedIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<SimulatedNews>()) }

    var newResourceName by remember { mutableStateOf("") }
    var newResourceUrl by remember { mutableStateOf("") }
    var newResourceCategory by remember { mutableStateOf("Tecnologia") }

    val performNewsSearchSimulated = { resource: NewsResource, query: String ->
        val terms = query.lowercase().trim()
        val domain = resource.url
        val category = resource.category
        val results = mutableListOf<SimulatedNews>()

        if (terms.isNotEmpty()) {
            when (category) {
                "Futebol" -> {
                    val teamName = when {
                        terms.contains("palmeiras") -> "Palmeiras"
                        terms.contains("corinthians") -> "Corinthians"
                        terms.contains("santos") -> "Santos"
                        terms.contains("sao paulo") || terms.contains("são paulo") -> "São Paulo"
                        else -> "Flamengo"
                    }
                    results.add(
                        SimulatedNews(
                            title = "$teamName apresenta novo sistema de análise com tecnologia AI e dados integrados",
                            desc = "Comissão técnica confirmou a adoção de monitoramento fisiológico avançado e rastreamento biomecânico.",
                            url = "$domain/futebol/time/tactical-data-science-${teamName.lowercase()}"
                        )
                    )
                    results.add(
                        SimulatedNews(
                            title = "Gols da Rodada: $teamName conquista vitória emocionante com placar de 2 a 1",
                            desc = "Destaques em campo mostram transição ofensiva rápida e precisão tática no Brasileirão.",
                            url = "$domain/futebol/jogos/brasileirao-gols-${teamName.lowercase()}"
                        )
                    )
                }
                "Tecnologia" -> {
                    val keyword = when {
                        terms.contains("apple") -> "Apple NPU"
                        terms.contains("google") || terms.contains("gemini") -> "Google Gemini"
                        terms.contains("openai") || terms.contains("gpt") -> "OpenAI GPT-5"
                        terms.contains("supabase") || terms.contains("banco") -> "Supabase DB"
                        else -> "Chips Neurais"
                    }
                    results.add(
                        SimulatedNews(
                            title = "Novo processamento local para execução offline de $keyword",
                            desc = "Arquitetura NPU integrada entrega inferência de alta fidelidade com privacidade local no dispositivo.",
                            url = "$domain/tech/news/offline-local-inference-npu"
                        )
                    )
                    results.add(
                        SimulatedNews(
                            title = "Suporte unificado a frameworks móveis de $keyword",
                            desc = "Melhoria na interoperabilidade de hardware móvel focado em inteligência de baixíssima latência.",
                            url = "$domain/hardware/smartphones/unified-os-support"
                        )
                    )
                }
                else -> {
                    results.add(
                        SimulatedNews(
                            title = "Análise aprofundada aponta tendências de mercado para \"$query\"",
                            desc = "Pesquisas revelam transformações operacionais influenciadas por automação e dados analíticos.",
                            url = "$domain/tendencias/mercado/analise-${query.replace(" ", "-")}"
                        )
                    )
                }
            }
        }
        results
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("context_browser_container")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SynapseSectionHeader(
                eyebrow = "WEB INTELLIGENCE",
                title = "Navegador de Contexto",
                subtitle = "Extraia conteúdo limpo de URLs ou pesquise notícias em tempo real para alimentar a IA."
            )
        }

        // URL Extraction Card
        item {
            SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Extrair Página Web",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Remove menus e anúncios automaticamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    SynapseOutlinedField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = "https://exemplo.com/artigo",
                        trailingIcon = if (urlInput.isNotEmpty()) {
                            {
                                IconButton(onClick = { urlInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Limpar URL",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("context_url_input")
                    )

                    Button(
                        onClick = {
                            onHaptic()
                            viewModel.processContextUrl(urlInput, chatId = 999999L)
                        },
                        enabled = urlInput.isNotBlank() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent,
                            disabledContainerColor = colors.cardElevated,
                            disabledContentColor = colors.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("analyze_url_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.accent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Extraindo...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extrair e Enviar para a IA",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // News Search & Sources Card
        item {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FONTES & BUSCA AO VIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accent
                            )
                            Text(
                                text = "Pesquisa de Notícias",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(
                            onClick = {
                                onHaptic()
                                showAddSourceForm = !showAddSourceForm
                            }
                        ) {
                            Icon(
                                imageVector = if (showAddSourceForm) Icons.Rounded.ExpandLess else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAddSourceForm) "Fechar" else "Nova Fonte",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.accent
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAddSourceForm) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceLow)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SynapseOutlinedField(
                                    value = newResourceName,
                                    onValueChange = { newResourceName = it },
                                    placeholder = "Nome (ex: GE)",
                                    modifier = Modifier.weight(1f)
                                )
                                SynapseOutlinedField(
                                    value = newResourceUrl,
                                    onValueChange = { newResourceUrl = it },
                                    placeholder = "https://site.com",
                                    modifier = Modifier.weight(1.4f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Tecnologia", "Futebol").forEach { cat ->
                                        val isSel = newResourceCategory == cat
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSel) colors.accent.copy(alpha = 0.18f) else colors.cardSurface,
                                            border = BorderStroke(1.dp, if (isSel) colors.accent else colors.borderSubtle),
                                            modifier = Modifier.clickable { newResourceCategory = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSel) colors.accent else colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (newResourceName.isNotBlank() && newResourceUrl.isNotBlank()) {
                                            onHaptic()
                                            val normalizedUrl = if (!newResourceUrl.startsWith("http://") && !newResourceUrl.startsWith("https://")) {
                                                "https://$newResourceUrl"
                                            } else newResourceUrl
                                            customResources = customResources + NewsResource(
                                                name = newResourceName.trim(),
                                                url = normalizedUrl.trim(),
                                                category = newResourceCategory
                                            )
                                            selectedIndex = defaultResources.size + customResources.size - 1
                                            newResourceName = ""
                                            newResourceUrl = ""
                                            showAddSourceForm = false
                                        }
                                    },
                                    enabled = newResourceName.isNotBlank() && newResourceUrl.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.success, contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Adicionar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(combinedResources.size) { index ->
                            val res = combinedResources[index]
                            val isSelected = index == selectedIndex
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) colors.accent else colors.surfaceLow,
                                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderSubtle),
                                modifier = Modifier.clickable {
                                    onHaptic()
                                    selectedIndex = index
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (res.category == "Futebol") Icons.Rounded.SportsSoccer else Icons.Rounded.Memory,
                                        contentDescription = null,
                                        tint = if (isSelected) colors.onAccent else colors.accent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = res.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) colors.onAccent else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    SynapseOutlinedField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Pesquisar assunto (ex: Gemini, Palmeiras...)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_search_query_input"),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onHaptic()
                                    val activeRes = combinedResources.getOrNull(selectedIndex) ?: combinedResources[0]
                                    searchResults = performNewsSearchSimulated(activeRes, searchQuery)
                                    viewModel.searchAndSummarizeNews(activeRes.name, activeRes.url, activeRes.category, searchQuery, chatId = 999999L)
                                },
                                enabled = searchQuery.isNotBlank()
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = "Buscar", tint = colors.accent)
                            }
                        }
                    )

                    Button(
                        onClick = {
                            onHaptic()
                            val activeRes = combinedResources.getOrNull(selectedIndex) ?: combinedResources[0]
                            searchResults = performNewsSearchSimulated(activeRes, searchQuery)
                            viewModel.searchAndSummarizeNews(activeRes.name, activeRes.url, activeRes.category, searchQuery, chatId = 999999L)
                        },
                        enabled = searchQuery.isNotBlank() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryAccent,
                            contentColor = Color.White,
                            disabledContainerColor = colors.cardElevated,
                            disabledContentColor = colors.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("news_search_query_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isProcessing) "Pesquisando e resumindo..." else "Extrair e Resumir no Chat",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (searchResults.isNotEmpty()) {
                        HorizontalDivider(color = colors.borderSubtle)
                        Text(
                            text = "RESULTADOS (${searchResults.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        searchResults.forEach { item ->
                            val uriHandler = LocalUriHandler.current
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.surfaceLow,
                                border = BorderStroke(1.dp, colors.borderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.accent
                                    )
                                    Text(
                                        text = item.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = item.url,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onHaptic()
                                                urlInput = item.url
                                                viewModel.processContextUrl(item.url, chatId = 999999L)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                        ) {
                                            Text("Analisar", style = MaterialTheme.typography.labelLarge)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                try { uriHandler.openUri(item.url) } catch (_: Exception) {}
                                            },
                                            border = BorderStroke(1.dp, colors.borderStrong),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                        ) {
                                            Text("Abrir Original", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Extraction Status Card
        item {
            SynapseSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ÚLTIMA EXTRAÇÃO",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("URL processada", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        Text(
                            text = contextUrl.ifBlank { "Nenhuma" },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Volume limpo", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        Text(
                            text = extractedStats,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.success
                        )
                    }
                    if (contextUrl.isNotBlank()) {
                        val uriHandler = LocalUriHandler.current
                        OutlinedButton(
                            onClick = {
                                try { uriHandler.openUri(contextUrl) } catch (_: Exception) {}
                            },
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Página Original", color = colors.accent, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
