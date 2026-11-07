package org.example.proteinstructure

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.detail.ProteinDetailViewModelFactory
import org.example.proteinstructure.domain.ProteinRepository
import org.example.proteinstructure.list.ProteinListViewModelFactory
import org.example.proteinstructure.resources.Res
import org.example.proteinstructure.resources.action_toggle_theme
import org.example.proteinstructure.resources.ic_theme
import org.example.proteinstructure.ui.AppTheme
import org.example.proteinstructure.ui.components.AppScaffold
import org.example.proteinstructure.ui.navigation.AppNavDisplay
import org.example.proteinstructure.ui.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf(false) }
    var currentLanguage by remember {
        val initial = getAppLocale().lowercase()
        mutableStateOf(if (initial.startsWith("en")) "en" else "ru")
    }

    val repository: ProteinRepository = remember { ProteinRepositoryImpl() }
    val navigator = remember { Navigator() }

    val listViewModelFactory = remember { ProteinListViewModelFactory(navigator, repository) }
    val detailViewModelFactory = remember { ProteinDetailViewModelFactory(navigator, repository) }

    val navStack by navigator.navStack.collectAsStateWithLifecycle()
    val backNavigationIsAvailable by remember { derivedStateOf { navStack.size > 1 } }

    AppTheme(darkTheme = darkTheme) {
        key(currentLanguage) {
            AppScaffold(
                onBack = navigator::back.takeIf { backNavigationIsAvailable },
                actions = {
                    TextButton(onClick = {
                        val next = if (currentLanguage == "ru") "en" else "ru"
                        setAppLocale(next)
                        currentLanguage = next
                    }) {
                        Text(
                            text = if (currentLanguage == "ru") "EN" else "RU",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = { darkTheme = !darkTheme }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_theme),
                            contentDescription = stringResource(Res.string.action_toggle_theme),
                        )
                    }
                },
            ) { modifier ->
                AppNavDisplay(
                    modifier = modifier,
                    navigator = navigator,
                    listViewModelFactory = listViewModelFactory,
                    detailViewModelFactory = detailViewModelFactory,
                )
            }
        }
    }
}
