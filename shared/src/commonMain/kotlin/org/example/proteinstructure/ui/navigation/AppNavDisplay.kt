package org.example.proteinstructure.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.example.proteinstructure.Screen
import org.example.proteinstructure.detail.ProteinDetailViewModel
import org.example.proteinstructure.detail.ProteinDetailViewModelFactory
import org.example.proteinstructure.list.ProteinListViewModel
import org.example.proteinstructure.list.ProteinListViewModelFactory
import org.example.proteinstructure.ui.screens.detail.ProteinDetailScreen
import org.example.proteinstructure.ui.screens.list.ProteinListScreen

private const val TRANSITION_MS = 300

@Composable
fun AppNavDisplay(
    modifier: Modifier,
    navigator: Navigator,
    listViewModelFactory: ProteinListViewModelFactory,
    detailViewModelFactory: ProteinDetailViewModelFactory,
) {
    val backStack by navigator.navStack.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = navigator::back,
        transitionSpec = { slide(SlideDirection.Start) },
        popTransitionSpec = { slide(SlideDirection.End) },
        predictivePopTransitionSpec = { slide(SlideDirection.End) },
        entryProvider = entryProvider {
            entry<Screen.List> {
                val viewModel: ProteinListViewModel = viewModel(factory = listViewModelFactory)
                val state by viewModel.state.collectAsStateWithLifecycle()
                ProteinListScreen(state = state, onIntent = viewModel::onIntent)
            }
            entry<Screen.Detail> { key ->
                val viewModel: ProteinDetailViewModel = viewModel(
                    key = "detail-${key.id}",
                    factory = detailViewModelFactory,
                    extras = ProteinDetailViewModelFactory.extrasFor(key.id),
                )
                val state by viewModel.state.collectAsStateWithLifecycle()
                state?.let { ProteinDetailScreen(protein = it, onIntent = viewModel::onIntent) }
            }
        },
    )
}

private fun AnimatedContentTransitionScope<*>.slide(
    direction: SlideDirection,
): ContentTransform =
    (slideIntoContainer(direction, tween(TRANSITION_MS)) + fadeIn(tween(TRANSITION_MS)))
        .togetherWith(
            slideOutOfContainer(direction, tween(TRANSITION_MS)) + fadeOut(tween(TRANSITION_MS)),
        )
