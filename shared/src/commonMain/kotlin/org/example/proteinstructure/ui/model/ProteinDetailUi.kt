package org.example.proteinstructure.ui.model

import org.example.proteinstructure.domain.Protein
import org.example.proteinstructure.domain.ProteinMethod

data class ProteinDetailUi(
    val id: String,
    val name: String,
    val title: String,
    val organism: String,
    val classification: String,
    val method: ProteinMethod,
    val resolution: Double?,
    val polymerComposition: String,
    val monomerCount: Int,
    val molecularWeightKDa: Double,
    val releaseDate: String,
    val authors: List<String>,
    val citationTitle: String?,
    val related: List<ProteinCardUi>,
)

fun Protein.toDetailUi(related: List<Protein>): ProteinDetailUi = ProteinDetailUi(
    id = id,
    name = shortName,
    title = title,
    organism = organism,
    classification = classification,
    method = method,
    resolution = resolution,
    polymerComposition = polymerComposition,
    monomerCount = monomerCount,
    molecularWeightKDa = molecularWeightKDa,
    releaseDate = releaseDate,
    authors = authors,
    citationTitle = citationTitle,
    related = related.toCardsUi(),
)
