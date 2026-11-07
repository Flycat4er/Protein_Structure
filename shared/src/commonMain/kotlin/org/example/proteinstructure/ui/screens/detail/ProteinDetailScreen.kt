package org.example.proteinstructure.ui.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proteinstructure.detail.ProteinDetailIntent
import org.example.proteinstructure.resources.Res
import org.example.proteinstructure.resources.label_classification
import org.example.proteinstructure.resources.label_organism
import org.example.proteinstructure.resources.label_pdb_id
import org.example.proteinstructure.resources.label_release_date
import org.example.proteinstructure.resources.label_residues
import org.example.proteinstructure.resources.label_resolution
import org.example.proteinstructure.resources.label_weight
import org.example.proteinstructure.resources.residues_format
import org.example.proteinstructure.resources.resolution_format
import org.example.proteinstructure.resources.resolution_na
import org.example.proteinstructure.resources.section_authors
import org.example.proteinstructure.resources.section_general
import org.example.proteinstructure.resources.section_related
import org.example.proteinstructure.resources.section_structure
import org.example.proteinstructure.resources.weight_format
import org.example.proteinstructure.ui.components.FactItem
import org.example.proteinstructure.ui.components.MethodBadge
import org.example.proteinstructure.ui.components.ResolutionBadge
import org.example.proteinstructure.ui.components.SectionHeader
import org.example.proteinstructure.ui.model.ProteinDetailUi
import org.example.proteinstructure.ui.screens.list.ProteinCard
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProteinDetailScreen(
    protein: ProteinDetailUi,
    onIntent: (ProteinDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = protein.id,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            ResolutionBadge(resolution = protein.resolution)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = protein.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = protein.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        MethodBadge(method = protein.method)

        SectionHeader(title = stringResource(Res.string.section_general))
        FactItem(label = stringResource(Res.string.label_pdb_id), value = protein.id)
        FactItem(label = stringResource(Res.string.label_organism), value = protein.organism)
        FactItem(label = stringResource(Res.string.label_classification), value = protein.classification)
        FactItem(label = stringResource(Res.string.label_release_date), value = protein.releaseDate)

        SectionHeader(title = stringResource(Res.string.section_structure))
        FactItem(
            label = stringResource(Res.string.label_resolution),
            value = if (protein.resolution != null) {
                stringResource(Res.string.resolution_format, protein.resolution.toString())
            } else {
                stringResource(Res.string.resolution_na)
            },
        )
        FactItem(
            label = stringResource(Res.string.label_weight),
            value = stringResource(Res.string.weight_format, protein.molecularWeightKDa.toString()),
        )
        FactItem(
            label = stringResource(Res.string.label_residues),
            value = stringResource(Res.string.residues_format, protein.monomerCount.toString()),
        )

        if (protein.authors.isNotEmpty() || protein.citationTitle != null) {
            SectionHeader(title = stringResource(Res.string.section_authors))
            if (protein.authors.isNotEmpty()) {
                Text(
                    text = protein.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (protein.citationTitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "«${protein.citationTitle}»",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        if (protein.related.isNotEmpty()) {
            SectionHeader(title = stringResource(Res.string.section_related))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                protein.related.forEach { relatedItem ->
                    ProteinCard(
                        protein = relatedItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onIntent(ProteinDetailIntent.RelatedProteinClicked(relatedItem.id))
                            },
                    )
                }
            }
        }
    }
}
