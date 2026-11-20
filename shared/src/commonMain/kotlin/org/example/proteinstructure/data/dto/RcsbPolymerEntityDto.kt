package org.example.proteinstructure.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RcsbPolymerEntityResponseDto(
    @SerialName("rcsb_id") val rcsbId: String? = null,
    @SerialName("rcsb_polymer_entity") val polymerEntity: RcsbPolymerEntityDetailsDto? = null,
    @SerialName("entity_poly") val entityPoly: RcsbEntityPolyDto? = null,
    @SerialName("rcsb_entity_source_organism") val sourceOrganisms: List<RcsbEntitySourceOrganismDto>? = null,
    @SerialName("rcsb_polymer_entity_container_identifiers") val containerIdentifiers: RcsbPolymerContainerIdentifiersDto? = null,
)

@Serializable
data class RcsbPolymerEntityDetailsDto(
    @SerialName("pdbx_description") val description: String? = null,
    @SerialName("formula_weight") val formulaWeight: Double? = null,
    @SerialName("rcsb_entity_polymer_type") val polymerType: String? = null,
)

@Serializable
data class RcsbEntityPolyDto(
    @SerialName("rcsb_sample_sequence_length") val sequenceLength: Int? = null,
    @SerialName("type") val type: String? = null,
)

@Serializable
data class RcsbEntitySourceOrganismDto(
    @SerialName("scientific_name") val scientificName: String? = null,
    @SerialName("common_name") val commonName: String? = null,
)

@Serializable
data class RcsbPolymerContainerIdentifiersDto(
    @SerialName("entry_id") val entryId: String? = null,
    @SerialName("entity_id") val entityId: String? = null,
)
