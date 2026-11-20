package org.example.proteinstructure.domain

enum class ProteinMethod(val rawName: String) {
    XRAY("X-RAY DIFFRACTION"),
    ELECTRON_MICROSCOPY("ELECTRON MICROSCOPY"),
    NMR("SOLUTION NMR"),
    NEUTRON("NEUTRON DIFFRACTION"),
    OTHER("OTHER");

    companion object {
        fun fromRaw(raw: String): ProteinMethod {
            val upper = raw.uppercase()
            return when {
                "X-RAY" in upper -> XRAY
                "MICROSCOPY" in upper || "CRYO" in upper -> ELECTRON_MICROSCOPY
                "NMR" in upper -> NMR
                "NEUTRON" in upper -> NEUTRON
                else -> OTHER
            }
        }
    }
}

data class Protein(
    val id: String,
    val shortName: String,
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
    val relatedIds: List<String> = emptyList(),
)
