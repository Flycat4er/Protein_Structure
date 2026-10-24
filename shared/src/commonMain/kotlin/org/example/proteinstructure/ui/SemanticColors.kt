package org.example.proteinstructure.ui

import androidx.compose.ui.graphics.Color
import org.example.proteinstructure.domain.ProteinMethod

object SemanticColors {
    val methodXray = Color(0xFF1976D2)
    val methodCryoEm = Color(0xFF00897B)
    val methodNmr = Color(0xFFFB8C00)
    val methodNeutron = Color(0xFF8E24AA)
    val methodOther = Color(0xFF607D8B)

    val resolutionHigh = Color(0xFF2E7D32)
    val resolutionMedium = Color(0xFFF57F17)
    val resolutionLow = Color(0xFFC62828)
    val resolutionNmr = Color(0xFF546E7A)

    fun colorForMethod(method: ProteinMethod): Color = when (method) {
        ProteinMethod.XRAY -> methodXray
        ProteinMethod.ELECTRON_MICROSCOPY -> methodCryoEm
        ProteinMethod.NMR -> methodNmr
        ProteinMethod.NEUTRON -> methodNeutron
        ProteinMethod.OTHER -> methodOther
    }

    fun colorForResolution(resolution: Double?): Color = when {
        resolution == null -> resolutionNmr
        resolution < 1.8 -> resolutionHigh
        resolution <= 2.5 -> resolutionMedium
        else -> resolutionLow
    }
}
