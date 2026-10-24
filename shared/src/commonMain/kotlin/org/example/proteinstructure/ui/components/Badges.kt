package org.example.proteinstructure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.proteinstructure.domain.ProteinMethod
import org.example.proteinstructure.resources.Res
import org.example.proteinstructure.resources.method_em
import org.example.proteinstructure.resources.method_neutron
import org.example.proteinstructure.resources.method_nmr
import org.example.proteinstructure.resources.method_xray
import org.example.proteinstructure.resources.resolution_format
import org.example.proteinstructure.resources.resolution_na
import org.example.proteinstructure.ui.SemanticColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun MethodBadge(
    method: ProteinMethod,
    modifier: Modifier = Modifier,
) {
    val color = SemanticColors.colorForMethod(method)
    val text = when (method) {
        ProteinMethod.XRAY -> stringResource(Res.string.method_xray)
        ProteinMethod.ELECTRON_MICROSCOPY -> stringResource(Res.string.method_em)
        ProteinMethod.NMR -> stringResource(Res.string.method_nmr)
        ProteinMethod.NEUTRON -> stringResource(Res.string.method_neutron)
        ProteinMethod.OTHER -> method.rawName
    }

    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun ResolutionBadge(
    resolution: Double?,
    modifier: Modifier = Modifier,
) {
    val color = SemanticColors.colorForResolution(resolution)
    val text = if (resolution != null) {
        stringResource(Res.string.resolution_format, resolution.toString())
    } else {
        stringResource(Res.string.resolution_na)
    }

    Box(
        modifier = modifier
            .background(color = color, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
