package org.example.proteinstructure.data.dto

import org.example.proteinstructure.domain.Protein
import org.example.proteinstructure.domain.ProteinMethod

val CANONICAL_SHORT_NAMES = mapOf(
    "4HHB" to "Deoxyhemoglobin",
    "1HHO" to "Oxyhemoglobin",
    "6LU7" to "SARS-CoV-2 Main Protease",
    "1EMA" to "Green Fluorescent Protein (GFP)",
    "1TIM" to "Triosephosphate Isomerase",
    "1MBO" to "Myoglobin",
    "1UBQ" to "Ubiquitin",
    "1CRN" to "Crambin",
    "3NIR" to "Ultra-high Resolution Crambin",
    "1ATP" to "Protein Kinase A",
    "2POR" to "Porin Membrane Channel",
    "5P21" to "H-Ras p21 Oncogene Protein",
    "1AON" to "GroEL Chaperonin Complex",
    "2CP4" to "Cytochrome P450cam",
    "1IGT" to "Monoclonal Antibody IgG2a",
    "1L2Y" to "Trp-cage Miniprotein",
    "3J3Q" to "Bacterial 70S Ribosome",
    "7KRR" to "SARS-CoV-2 Spike Glycoprotein",
    "1F88" to "Bovine Rhodopsin",
    "2HAC" to "Influenza Hemagglutinin",
    "1LYZ" to "Egg-White Lysozyme",
    "2HHB" to "Deoxyhemoglobin T-State",
    "1BNA" to "B-DNA Dodecamer",
)

fun cleanHumanName(rawTitle: String?, rawDescription: String?): String {
    // 1. Проверяем описание полимерной сущности
    if (!rawDescription.isNullOrBlank()) {
        val desc = rawDescription.trim()
        val isChemicalFormula = desc.contains("5'-") || desc.contains("3'-") || desc.contains("(*") ||
            desc.contains(")-") || desc.startsWith("DNA (") || desc.startsWith("RNA (")
        if (!isChemicalFormula && desc.length in 3..60) {
            return desc
        }
    }

    // 2. Очищаем название структуры
    if (!rawTitle.isNullOrBlank()) {
        var clean = rawTitle.trim()

        if (clean.endsWith(".")) clean = clean.dropLast(1).trim()

        // Убираем технические префиксы вида "The crystal structure of..."
        val prefixRegex = Regex(
            "^(the\\s+)?(crystal\\s+structure|structure|cryo-em\\s+structure|high-resolution\\s+structure|atomic\\s+structure|solution\\s+structure|three-dimensional\\s+structure|x-ray\\s+structure)\\s+of(\\s+the|\\s+a)?\\s+",
            RegexOption.IGNORE_CASE,
        )
        clean = prefixRegex.replace(clean, "")

        // Убираем суффиксы с разрешением, например "at 1.74 A resolution"
        val suffixRegex = Regex(
            "(\\s+at\\s+[\\d\\.]+\\s*(a|å|angstroms?).*|\\s+determined\\s+by.*|\\s*\\.\\s*conformation\\s+and\\s+dynamics.*)$",
            RegexOption.IGNORE_CASE,
        )
        clean = suffixRegex.replace(clean, "")

        // Убираем детали "in complex with..."
        if (clean.length > 45 && clean.contains(" in complex with ", ignoreCase = true)) {
            clean = clean.substringBefore(" in complex with ")
        }

        // Если строка полностью в верхнем регистре, приводим к Title Case
        if (clean.all { it.isUpperCase() || !it.isLetter() }) {
            clean = toTitleCase(clean)
        }

        if (clean.isNotBlank()) {
            return if (clean.length > 55) clean.take(52) + "..." else clean
        }
    }

    return "Protein Structure"
}

private val KNOWN_ACRONYMS = setOf("DNA", "RNA", "NMR", "GFP", "HIV", "SARS", "COVID", "PDB", "ATP", "ADP", "GDP", "GTP")

private fun toTitleCase(str: String): String {
    val minorWords = setOf("of", "and", "in", "the", "a", "an", "at", "to", "for", "from", "with", "by")
    return str.split(" ").joinToString(" ") { token ->
        if (token.contains("-")) {
            token.split("-").joinToString("-") { part ->
                formatWord(part, minorWords)
            }
        } else {
            formatWord(token, minorWords)
        }
    }.replaceFirstChar { it.uppercase() }
}

private fun formatWord(word: String, minorWords: Set<String>): String {
    val upper = word.uppercase()
    if (upper in KNOWN_ACRONYMS) return upper
    val lower = word.lowercase()
    if (lower in minorWords) return lower
    return lower.replaceFirstChar { it.uppercase() }
}

fun cleanOrganismName(
    polymerDto: RcsbPolymerEntityResponseDto?,
    entryDto: RcsbEntryDto,
): String {
    val sourceScientific = polymerDto?.sourceOrganisms?.firstOrNull()?.scientificName
    if (!sourceScientific.isNullOrBlank() && !sourceScientific.equals("unspecified", ignoreCase = true)) {
        return sourceScientific
    }
    val sourceCommon = polymerDto?.sourceOrganisms?.firstOrNull()?.commonName
    if (!sourceCommon.isNullOrBlank()) {
        return sourceCommon
    }
    val polymerType = polymerDto?.polymerEntity?.polymerType
    if (polymerType != null && (polymerType.contains("DNA", ignoreCase = true) || polymerType.contains("RNA", ignoreCase = true))) {
        return "Synthetic construct"
    }
    return "Homo sapiens"
}

fun mapToProtein(
    entryDto: RcsbEntryDto,
    polymerEntityDto: RcsbPolymerEntityResponseDto? = null,
    relatedIds: List<String> = emptyList(),
): Protein {
    val id = entryDto.rcsbId ?: entryDto.entry?.id ?: "UNKNOWN"
    val title = entryDto.struct?.title ?: id
    val classification = entryDto.structKeywords?.pdbxKeywords
        ?: entryDto.structKeywords?.text
        ?: "STRUCTURE"
    val organism = cleanOrganismName(polymerEntityDto, entryDto)
    val shortName = CANONICAL_SHORT_NAMES[id.uppercase()]
        ?: cleanHumanName(title, polymerEntityDto?.polymerEntity?.description)

    val methodString = entryDto.exptl?.firstOrNull()?.method
        ?: entryDto.rcsbEntryInfo?.experimentalMethod
        ?: "OTHER"
    val method = ProteinMethod.fromRaw(methodString)

    val resolution = entryDto.rcsbEntryInfo?.resolutionCombined?.firstOrNull()

    val monomerCount = polymerEntityDto?.entityPoly?.sequenceLength
        ?: entryDto.rcsbEntryInfo?.monomerCount
        ?: 0

    val molecularWeight = polymerEntityDto?.polymerEntity?.formulaWeight
        ?: entryDto.rcsbEntryInfo?.molecularWeight
        ?: 0.0

    val releaseDateRaw = entryDto.rcsbAccessionInfo?.initialReleaseDate
        ?: entryDto.rcsbAccessionInfo?.depositDate
        ?: ""
    val releaseDate = if (releaseDateRaw.length >= 10) releaseDateRaw.substring(0, 10) else releaseDateRaw

    val authors = entryDto.rcsbPrimaryCitation?.authors
        ?: entryDto.auditAuthors?.mapNotNull { it.name }
        ?: emptyList()

    val citationTitle = entryDto.rcsbPrimaryCitation?.title

    val polymerComposition = entryDto.rcsbEntryInfo?.polymerComposition
        ?: polymerEntityDto?.polymerEntity?.polymerType
        ?: "protein"

    return Protein(
        id = id,
        shortName = shortName,
        title = title,
        organism = organism,
        classification = classification,
        method = method,
        resolution = resolution,
        polymerComposition = polymerComposition,
        monomerCount = monomerCount,
        molecularWeightKDa = molecularWeight,
        releaseDate = releaseDate,
        authors = authors,
        citationTitle = citationTitle,
        relatedIds = relatedIds,
    )
}

fun mapPolymerEntityToProtein(
    polymerDto: RcsbPolymerEntityResponseDto,
    parentEntry: Protein? = null,
): Protein {
    val id = polymerDto.rcsbId ?: "UNKNOWN"
    val desc = polymerDto.polymerEntity?.description
    val shortName = cleanHumanName(parentEntry?.title, desc)
    val organism = polymerDto.sourceOrganisms?.firstOrNull()?.scientificName
        ?: polymerDto.sourceOrganisms?.firstOrNull()?.commonName
        ?: parentEntry?.organism
        ?: "Unknown"
    val classification = parentEntry?.classification ?: "POLYMER ENTITY"
    val method = parentEntry?.method ?: ProteinMethod.OTHER
    val resolution = parentEntry?.resolution
    val polymerComposition = polymerDto.polymerEntity?.polymerType ?: "protein"
    val monomerCount = polymerDto.entityPoly?.sequenceLength ?: 0
    val molecularWeight = polymerDto.polymerEntity?.formulaWeight ?: 0.0
    val releaseDate = parentEntry?.releaseDate ?: ""
    val authors = parentEntry?.authors ?: emptyList()
    val citationTitle = parentEntry?.citationTitle
    val title = parentEntry?.title ?: shortName

    return Protein(
        id = id,
        shortName = shortName,
        title = title,
        organism = organism,
        classification = classification,
        method = method,
        resolution = resolution,
        polymerComposition = polymerComposition,
        monomerCount = monomerCount,
        molecularWeightKDa = molecularWeight,
        releaseDate = releaseDate,
        authors = authors,
        citationTitle = citationTitle,
        relatedIds = parentEntry?.relatedIds ?: emptyList(),
    )
}
