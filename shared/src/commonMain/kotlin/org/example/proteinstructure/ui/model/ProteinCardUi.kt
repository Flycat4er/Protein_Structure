package org.example.proteinstructure.ui.model

import org.example.proteinstructure.domain.Protein
import org.example.proteinstructure.domain.ProteinMethod

data class ProteinCardUi(
    val id: String,
    val name: String,
    val organism: String,
    val classification: String,
    val method: ProteinMethod,
    val resolution: Double?,
    val molecularWeightKDa: Double,
    val monomerCount: Int,
)

fun Protein.toCardUi(): ProteinCardUi = ProteinCardUi(
    id = id,
    name = shortName,
    organism = organism,
    classification = classification,
    method = method,
    resolution = resolution,
    molecularWeightKDa = molecularWeightKDa,
    monomerCount = monomerCount,
)

fun List<Protein>.toCardsUi(): List<ProteinCardUi> = map { it.toCardUi() }
