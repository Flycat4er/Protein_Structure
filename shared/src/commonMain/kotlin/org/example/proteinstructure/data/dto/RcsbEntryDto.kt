package org.example.proteinstructure.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RcsbEntryDto(
    @SerialName("rcsb_id") val rcsbId: String? = null,
    @SerialName("entry") val entry: RcsbEntryIdDto? = null,
    @SerialName("struct") val struct: RcsbStructDto? = null,
    @SerialName("struct_keywords") val structKeywords: RcsbStructKeywordsDto? = null,
    @SerialName("rcsb_entry_info") val rcsbEntryInfo: RcsbEntryInfoDto? = null,
    @SerialName("exptl") val exptl: List<RcsbExptlDto>? = null,
    @SerialName("rcsb_accession_info") val rcsbAccessionInfo: RcsbAccessionInfoDto? = null,
    @SerialName("rcsb_primary_citation") val rcsbPrimaryCitation: RcsbPrimaryCitationDto? = null,
    @SerialName("rcsb_entry_container_identifiers") val containerIdentifiers: RcsbContainerIdentifiersDto? = null,
    @SerialName("audit_author") val auditAuthors: List<RcsbAuditAuthorDto>? = null,
)

@Serializable
data class RcsbEntryIdDto(
    @SerialName("id") val id: String? = null,
)

@Serializable
data class RcsbStructDto(
    @SerialName("title") val title: String? = null,
)

@Serializable
data class RcsbStructKeywordsDto(
    @SerialName("pdbx_keywords") val pdbxKeywords: String? = null,
    @SerialName("text") val text: String? = null,
)

@Serializable
data class RcsbEntryInfoDto(
    @SerialName("molecular_weight") val molecularWeight: Double? = null,
    @SerialName("deposited_polymer_monomer_count") val monomerCount: Int? = null,
    @SerialName("polymer_composition") val polymerComposition: String? = null,
    @SerialName("selected_polymer_entity_types") val selectedPolymerEntityTypes: String? = null,
    @SerialName("experimental_method") val experimentalMethod: String? = null,
    @SerialName("resolution_combined") val resolutionCombined: List<Double>? = null,
)

@Serializable
data class RcsbExptlDto(
    @SerialName("method") val method: String? = null,
)

@Serializable
data class RcsbAccessionInfoDto(
    @SerialName("initial_release_date") val initialReleaseDate: String? = null,
    @SerialName("deposit_date") val depositDate: String? = null,
)

@Serializable
data class RcsbPrimaryCitationDto(
    @SerialName("title") val title: String? = null,
    @SerialName("rcsb_authors") val authors: List<String>? = null,
)

@Serializable
data class RcsbContainerIdentifiersDto(
    @SerialName("entry_id") val entryId: String? = null,
    @SerialName("polymer_entity_ids") val polymerEntityIds: List<String>? = null,
)

@Serializable
data class RcsbAuditAuthorDto(
    @SerialName("name") val name: String? = null,
)
