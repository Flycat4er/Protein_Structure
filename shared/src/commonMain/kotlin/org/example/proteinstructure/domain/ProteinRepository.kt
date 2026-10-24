package org.example.proteinstructure.domain

interface ProteinRepository {
    suspend fun getProteins(query: String? = null): List<Protein>
    suspend fun getProtein(id: String): Protein
    suspend fun getRelatedProteins(id: String): List<Protein>
}
