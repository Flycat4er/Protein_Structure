package org.example.proteinstructure.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RcsbSearchResponseDto(
    @SerialName("query_id") val queryId: String? = null,
    @SerialName("result_type") val resultType: String? = null,
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("result_set") val resultSet: List<RcsbSearchResultItemDto> = emptyList(),
)

@Serializable
data class RcsbSearchResultItemDto(
    @SerialName("identifier") val identifier: String,
    @SerialName("score") val score: Double? = null,
)
