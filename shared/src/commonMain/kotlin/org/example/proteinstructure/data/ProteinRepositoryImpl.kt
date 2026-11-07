package org.example.proteinstructure.data

import org.example.proteinstructure.domain.Protein
import org.example.proteinstructure.domain.ProteinRepository

class ProteinRepositoryImpl(
    private val proteins: List<Protein> = mockProteins,
) : ProteinRepository {

    override suspend fun getProteins(query: String?): List<Protein> {
        if (query.isNullOrBlank()) {
            return proteins
        }
        val cleanQuery = query.trim().lowercase()
        return proteins.filter { protein ->
            protein.id.lowercase().contains(cleanQuery) ||
                protein.shortName.lowercase().contains(cleanQuery) ||
                protein.classification.lowercase().contains(cleanQuery) ||
                protein.organism.lowercase().contains(cleanQuery)
        }
    }

    override suspend fun getProtein(id: String): Protein {
        return proteins.firstOrNull { it.id.equals(id, ignoreCase = true) }
            ?: throw IllegalArgumentException("Protein with PDB ID $id not found")
    }

    override suspend fun getRelatedProteins(id: String): List<Protein> {
        val current = getProtein(id)
        return current.relatedIds
            .mapNotNull { relatedId -> proteins.firstOrNull { it.id.equals(relatedId, ignoreCase = true) } }
    }
}
