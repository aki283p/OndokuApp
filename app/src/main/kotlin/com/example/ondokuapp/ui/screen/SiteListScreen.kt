package com.example.ondokuapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ondokuapp.R
import com.example.ondokuapp.model.SupportedNovelSite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteListScreen(
    onSiteClick: (SupportedNovelSite) -> Unit,
    onBack: () -> Unit
) {
    val sites = listOf(
        SupportedNovelSite(stringResource(R.string.site_syosetu), "https://syosetu.com/"),
        SupportedNovelSite(stringResource(R.string.site_kakuyomu), "https://kakuyomu.jp/"),
        SupportedNovelSite(stringResource(R.string.site_aozora), "https://www.aozora.gr.jp/"),
        SupportedNovelSite(stringResource(R.string.site_alphapolis), "https://www.alphapolis.co.jp/novel"),
        SupportedNovelSite(stringResource(R.string.site_hameln), "https://syosetu.org/"),
        SupportedNovelSite(stringResource(R.string.site_novelup), "https://novelup.plus/"),
        SupportedNovelSite(stringResource(R.string.site_estar), "https://estar.jp/")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.supported_sites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.site_notice),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            items(sites) { site ->
                ListItem(
                    headlineContent = { Text(site.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(site.url, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.clickable { onSiteClick(site) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
