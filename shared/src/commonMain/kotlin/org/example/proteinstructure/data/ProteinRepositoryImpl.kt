package org.example.proteinstructure.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.example.proteinstructure.BuildConfig
import org.example.proteinstructure.data.dto.RcsbEntryDto
import org.example.proteinstructure.data.dto.RcsbPolymerEntityResponseDto
import org.example.proteinstructure.data.dto.RcsbSearchResponseDto
import org.example.proteinstructure.data.dto.mapPolymerEntityToProtein
import org.example.proteinstructure.data.dto.mapToProtein
import org.example.proteinstructure.data.network.createHttpClient
import org.example.proteinstructure.domain.Protein
import org.example.proteinstructure.domain.ProteinRepository

private val DEFAULT_ENTRY_IDS = listOf(
    "4HHB", "1HHO", "6LU7", "1EMA", "1TIM",
    "1MBO", "1UBQ", "1CRN", "3NIR", "1ATP",
    "2POR", "5P21", "1AON", "2CP4", "1IGT",
    "1L2Y", "3J3Q", "7KRR", "1F88", "2HAC",
    "1LYZ", "2HHB",
)

private val PDB_ID_REGEX = Regex("^[0-9][a-zA-Z0-9]{3}$")

class ProteinRepositoryImpl(
    private val httpClient: HttpClient = createHttpClient(),
    private val searchApiUrl: String = BuildConfig.SEARCH_API_URL,
    private val dataApiUrl: String = BuildConfig.DATA_API_URL,
    private val mockFallback: List<Protein>? = null,
) : ProteinRepository {

    private val cache = mutableMapOf<String, Protein>()
    private val mutex = Mutex()

    override suspend fun getProteins(
        query: String?,
        page: Int,
        pageSize: Int,
    ): List<Protein> = withContext(Dispatchers.Default) {
        val trimmed = query?.trim()

        if (!trimmed.isNullOrBlank() && PDB_ID_REGEX.matches(trimmed) && page > 0) {
            return@withContext emptyList()
        }

        val jsonQuery = buildSearchQueryJson(trimmed, page, pageSize)

        try {
            val response = httpClient.get(searchApiUrl) {
                parameter("json", jsonQuery)
            }

            if (response.status == HttpStatusCode.NoContent) {
                return@withContext emptyList()
            }

            val searchResult = response.body<RcsbSearchResponseDto>()
            val ids = searchResult.resultSet.map { it.identifier }
            if (ids.isEmpty()) {
                return@withContext emptyList()
            }

            val proteins = coroutineScope {
                ids.map { id ->
                    async {
                        try {
                            getProtein(id)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            if (ids.isNotEmpty() && proteins.isEmpty()) {
                throw IllegalStateException("Failed to load protein details from network")
            }

            proteins
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (mockFallback != null) {
                if (trimmed.isNullOrBlank()) {
                    mockFallback
                } else {
                    val q = trimmed.lowercase()
                    mockFallback.filter {
                        it.id.lowercase().contains(q) ||
                            it.shortName.lowercase().contains(q) ||
                            it.classification.lowercase().contains(q) ||
                            it.organism.lowercase().contains(q)
                    }
                }
            } else {
                throw e
            }
        }
    }

    override suspend fun getProtein(id: String): Protein = withContext(Dispatchers.Default) {
        mutex.withLock { cache[id] }?.let { return@withContext it }

        try {
            val isPolymer = id.contains("_")
            val protein = if (isPolymer) {
                val entryId = id.substringBefore("_")
                val entityId = id.substringAfter("_")
                val parentEntry = try {
                    fetchEntry(entryId)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
                val polymerDto = httpClient.get("$dataApiUrl/polymer_entity/$entryId/$entityId")
                    .body<RcsbPolymerEntityResponseDto>()
                mapPolymerEntityToProtein(polymerDto, parentEntry)
            } else {
                val entryDto = httpClient.get("$dataApiUrl/entry/$id").body<RcsbEntryDto>()
                val polymerEntityIds = entryDto.containerIdentifiers?.polymerEntityIds ?: emptyList()
                val firstEntityId = polymerEntityIds.firstOrNull() ?: "1"
                val polymerDto = try {
                    httpClient.get("$dataApiUrl/polymer_entity/$id/$firstEntityId")
                        .body<RcsbPolymerEntityResponseDto>()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
                val relatedIds = polymerEntityIds.map { "${id}_$it" }
                mapToProtein(entryDto, polymerDto, relatedIds)
            }

            mutex.withLock { cache[id] = protein }
            protein
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (mockFallback != null) {
                mockFallback.firstOrNull { it.id.equals(id, ignoreCase = true) }
                    ?: throw IllegalArgumentException("Protein with PDB ID $id not found")
            } else {
                throw e
            }
        }
    }

    override suspend fun getRelatedProteins(id: String): List<Protein> = withContext(Dispatchers.Default) {
        val entryId = id.substringBefore("_")

        try {
            val jsonQuery = """{"query":{"type":"terminal","service":"text","parameters":{"attribute":"rcsb_entry_container_identifiers.entry_id","operator":"exact_match","value":"$entryId"}},"return_type":"polymer_entity"}"""
            val response = httpClient.get(searchApiUrl) {
                parameter("json", jsonQuery)
            }

            if (response.status == HttpStatusCode.NoContent) {
                return@withContext emptyList()
            }

            val searchResult = response.body<RcsbSearchResponseDto>()
            val entityIds = searchResult.resultSet.map { it.identifier }

            if (entityIds.isEmpty()) {
                val current = getProtein(id)
                return@withContext current.relatedIds.mapNotNull { relId ->
                    try {
                        getProtein(relId)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            coroutineScope {
                entityIds.map { relId ->
                    async {
                        try {
                            getProtein(relId)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (mockFallback != null) {
                val current = getProtein(id)
                current.relatedIds.mapNotNull { relatedId ->
                    mockFallback.firstOrNull { it.id.equals(relatedId, ignoreCase = true) }
                }
            } else {
                throw e
            }
        }
    }

    private suspend fun fetchEntry(entryId: String): Protein? {
        return try {
            val entryDto = httpClient.get("$dataApiUrl/entry/$entryId").body<RcsbEntryDto>()
            mapToProtein(entryDto)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private fun buildSearchQueryJson(query: String?, page: Int, pageSize: Int): String {
        return if (query.isNullOrBlank()) {
            if (page == 0) {
                val idsJson = DEFAULT_ENTRY_IDS.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")
                """{"query":{"type":"terminal","service":"text","parameters":{"attribute":"rcsb_entry_container_identifiers.entry_id","operator":"in","value":$idsJson}},"return_type":"entry"}"""
            } else {
                val start = DEFAULT_ENTRY_IDS.size + (page - 1) * pageSize
                """{"query":{"type":"terminal","service":"text","parameters":{"attribute":"rcsb_entry_info.selected_polymer_entity_types","operator":"exact_match","value":"Protein (only)"}},"return_type":"entry","request_options":{"paginate":{"start":$start,"rows":$pageSize},"sort":[{"sort_by":"rcsb_accession_info.initial_release_date","direction":"desc"}]}}"""
            }
        } else if (PDB_ID_REGEX.matches(query)) {
            """{"query":{"type":"terminal","service":"text","parameters":{"attribute":"rcsb_entry_container_identifiers.entry_id","operator":"exact_match","value":"${query.uppercase()}"}},"return_type":"entry"}"""
        } else {
            val start = page * pageSize
            val escaped = query.replace("\\", "\\\\").replace("\"", "\\\"")
            """{"query":{"type":"terminal","service":"full_text","parameters":{"value":"$escaped"}},"return_type":"entry","request_options":{"paginate":{"start":$start,"rows":$pageSize}}}"""
        }
    }
}
