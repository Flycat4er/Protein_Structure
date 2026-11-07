package org.example.proteinstructure.ui.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proteinstructure.resources.Res
import org.example.proteinstructure.resources.residues_format
import org.example.proteinstructure.resources.weight_format
import org.example.proteinstructure.ui.components.CardSurface
import org.example.proteinstructure.ui.components.MethodBadge
import org.example.proteinstructure.ui.components.ResolutionBadge
import org.example.proteinstructure.ui.model.ProteinCardUi
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProteinCard(
    protein: ProteinCardUi,
    modifier: Modifier = Modifier,
) {
    CardSurface(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = protein.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                ResolutionBadge(resolution = protein.resolution)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = protein.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = protein.organism,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MethodBadge(method = protein.method)
                Text(
                    text = "${stringResource(Res.string.weight_format, protein.molecularWeightKDa.toString())} • ${stringResource(Res.string.residues_format, protein.monomerCount.toString())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
