package org.example.proteinstructure

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.example.proteinstructure.data.ProteinRepositoryImpl
import org.example.proteinstructure.data.dto.RcsbEntryDto
import org.example.proteinstructure.data.dto.RcsbEntryInfoDto
import org.example.proteinstructure.data.dto.RcsbStructDto
import org.example.proteinstructure.data.dto.mapToProtein
import org.example.proteinstructure.data.mockProteins
import org.example.proteinstructure.ui.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProteinRepositoryTest {

    private val repository = ProteinRepositoryImpl(mockFallback = mockProteins)

    @Test
    fun mockProteinsCountIsAtLeastTwenty() {
        assertTrue(mockProteins.size >= 20, "Требуется не менее 20 записей моков, сейчас: ${mockProteins.size}")
    }

    @Test
    fun mockProteinsHaveValidFields() {
        for (protein in mockProteins) {
            assertTrue(protein.id.isNotBlank(), "PDB ID не должен быть пустым")
            assertTrue(protein.shortName.isNotBlank(), "Имя белка не должно быть пустым")
            assertTrue(protein.organism.isNotBlank(), "Организм не должен быть пустым")
            assertTrue(protein.monomerCount > 0, "Количество остатков должно быть положительным для ${protein.id}")
            assertTrue(protein.molecularWeightKDa > 0.0, "Молекулярная масса должна быть положительной для ${protein.id}")
        }
    }

    @Test
    fun searchFilteringWorks() {
        runBlocking {
            val hemoglobinHits = repository.getProteins("hemoglobin")
            assertTrue(hemoglobinHits.isNotEmpty(), "Поиск гемоглобина должен вернуть результаты")

            val idSearch = repository.getProteins("6LU7")
            assertEquals(1, idSearch.size)
            assertEquals("6LU7", idSearch.first().id)

            val hhbSearch = repository.getProteins("4HHB")
            assertEquals(1, hhbSearch.size)
            assertEquals("4HHB", hhbSearch.first().id)
        }
    }

    @Test
    fun getProteinByIdWorks() {
        runBlocking {
            val protein = repository.getProtein("4HHB")
            assertEquals("4HHB", protein.id)
            assertNotNull(protein.title)
        }
    }

    @Test
    fun jsonParsingIgnoresUnknownKeys() {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val rawJson = """
            {
                "rcsb_id": "4HHB",
                "unknown_field_123": "unexpected_data",
                "nested_unknown": { "foo": "bar" },
                "struct": {
                    "title": "Human Deoxyhemoglobin",
                    "another_unknown": 42
                }
            }
        """.trimIndent()

        val parsed = json.decodeFromString<RcsbEntryDto>(rawJson)
        assertEquals("4HHB", parsed.rcsbId)
        assertEquals("Human Deoxyhemoglobin", parsed.struct?.title)
    }

    @Test
    fun mappersCorrectlyTransformDtoToDomain() {
        val entryDto = RcsbEntryDto(
            rcsbId = "4HHB",
            struct = RcsbStructDto("Human Deoxyhemoglobin"),
            rcsbEntryInfo = RcsbEntryInfoDto(
                molecularWeight = 64.5,
                monomerCount = 574,
                resolutionCombined = listOf(1.74),
            ),
        )
        val protein = mapToProtein(entryDto)
        assertEquals("4HHB", protein.id)
        assertEquals("Human Deoxyhemoglobin", protein.title)
        assertEquals(64.5, protein.molecularWeightKDa)
        assertEquals(574, protein.monomerCount)
        assertEquals(1.74, protein.resolution)
    }

    @Test
    fun cleanHumanNameProducesReadableTitles() {
        val name = org.example.proteinstructure.data.dto.cleanHumanName(
            rawTitle = "STRUCTURE OF A B-DNA DODECAMER. CONFORMATION AND DYNAMICS",
            rawDescription = "DNA (5'-D(*CP*GP*CP*GP*AP*AP*TP*TP*CP*GP*CP*G)-3')",
        )
        assertEquals("B-DNA Dodecamer", name)
    }

    @Test
    fun navigatorBackStackBehavior() {
        val navigator = Navigator()
        assertEquals(listOf(Screen.List), navigator.navStack.value)

        navigator.addToBackStack(Screen.Detail("4HHB"))
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB")), navigator.navStack.value)

        navigator.addToBackStack(Screen.Detail("1HHO"))
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB"), Screen.Detail("1HHO")), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List, Screen.Detail("4HHB")), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List), navigator.navStack.value)

        navigator.back()
        assertEquals(listOf(Screen.List), navigator.navStack.value)
    }
}
