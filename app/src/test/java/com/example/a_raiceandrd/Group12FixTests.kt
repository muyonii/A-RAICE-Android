package com.example.a_raiceandrd

import com.example.a_raiceandrd.data.repository.api.MapFarmDto
import com.example.a_raiceandrd.data.repository.api.MapFarmScanDto
import com.example.a_raiceandrd.util.BATAAN_MUNICIPALITIES
import com.example.a_raiceandrd.util.isHealthy
import com.example.a_raiceandrd.util.isUnrecognised
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Group 12 — pure JVM unit tests covering the fixed utility functions and DTO helpers.
 * No Android framework or Retrofit needed.
 */
class Group12FixTests {

    private val gson = Gson()

    // -----------------------------------------------------------------------
    // isHealthy / isUnrecognised (ScanHelpers.kt)
    // -----------------------------------------------------------------------

    @Test
    fun isHealthy_returnsTrue_forHealthyRicePlant() {
        assertTrue(isHealthy("Healthy Rice Plant"))
    }

    @Test
    fun isHealthy_returnsTrue_caseInsensitive() {
        // Must contain full phrase "Healthy Rice Plant" regardless of case
        assertTrue(isHealthy("HEALTHY RICE PLANT"))
        assertTrue(isHealthy("healthy rice plant"))
        // Partial words do NOT match
        assertFalse(isHealthy("Healthy"))
        assertFalse(isHealthy("HEALTHY"))
    }

    @Test
    fun isHealthy_returnsFalse_forDiseases() {
        assertFalse(isHealthy("Rice Blast"))
        assertFalse(isHealthy("Bacterial Leaf Blight"))
        assertFalse(isHealthy("Sheath Blight"))
    }

    @Test
    fun isHealthy_returnsFalse_forNull() {
        assertFalse(isHealthy(null))
    }

    @Test
    fun isUnrecognised_returnsTrue_forNullDisease() {
        // A null DiseaseDto means the model returned no recognised class
        assertTrue(isUnrecognised(null))
    }

    @Test
    fun isUnrecognised_returnsFalse_forNamedDisease() {
        val diseaseJson = """{"id":1,"name":"Rice Blast"}"""
        val disease = gson.fromJson(diseaseJson, com.example.a_raiceandrd.data.repository.api.DiseaseDto::class.java)
        assertFalse(isUnrecognised(disease))
    }

    // -----------------------------------------------------------------------
    // BATAAN_MUNICIPALITIES — shared list
    // -----------------------------------------------------------------------

    @Test
    fun bataanMunicipalities_has12Entries() {
        assertEquals(12, BATAAN_MUNICIPALITIES.size)
    }

    @Test
    fun bataanMunicipalities_slugsAreLowercase() {
        BATAAN_MUNICIPALITIES.forEach { m ->
            assertEquals(
                "Slug '${m.slug}' should be lowercase",
                m.slug.lowercase(),
                m.slug
            )
        }
    }

    @Test
    fun bataanMunicipalities_displayNamesAreCapitalized() {
        BATAAN_MUNICIPALITIES.forEach { m ->
            assertTrue(
                "Display name '${m.displayName}' should start with uppercase",
                m.displayName.first().isUpperCase()
            )
        }
    }

    @Test
    fun bataanMunicipalities_containsBalanga() {
        val balanga = BATAAN_MUNICIPALITIES.find { it.slug == "balanga" }
        assertNotNull(balanga)
        assertEquals("Balanga", balanga?.displayName)
    }

    @Test
    fun bataanMunicipalities_noDuplicateSlugs() {
        val slugs = BATAAN_MUNICIPALITIES.map { it.slug }
        assertEquals("Duplicate slugs found", slugs.size, slugs.toSet().size)
    }

    // -----------------------------------------------------------------------
    // MapFarmDto.farmerName / farmerContact — flat-staff vs nested-farmer shape
    // -----------------------------------------------------------------------

    @Test
    fun mapFarmDto_farmerName_returnsName_forFlatShape() {
        // Staff GET /map returns flat: {name, contact_number, ...}
        val json = """
            {
                "id": 1,
                "name": "Pedro Cruz",
                "contact_number": "09171234567",
                "barangay": "Cupang",
                "municipality": "balanga",
                "lat": 14.68,
                "lng": 120.54
            }
        """.trimIndent()
        val farm = gson.fromJson(json, MapFarmDto::class.java)
        assertEquals("Pedro Cruz", farm.farmerName)
        assertEquals("09171234567", farm.farmerContact)
    }

    @Test
    fun mapFarmDto_farmerName_returnsNestedName_forNestedShape() {
        // Nested farmer object
        val json = """
            {
                "id": 2,
                "barangay": "Capitangan",
                "municipality": "abucay",
                "lat": 14.71,
                "lng": 120.52,
                "farmer": {
                    "id": 5,
                    "name": "Maria Santos",
                    "contact_number": "09201234567"
                }
            }
        """.trimIndent()
        val farm = gson.fromJson(json, MapFarmDto::class.java)
        assertEquals("Maria Santos", farm.farmerName)
        assertEquals("09201234567", farm.farmerContact)
    }

    @Test
    fun mapFarmDto_farmerName_returnsNull_whenNoNameAnywhere() {
        val json = """{"id": 3, "barangay": "San Antonio", "municipality": "orani"}"""
        val farm = gson.fromJson(json, MapFarmDto::class.java)
        assertNull(farm.farmerName)
        assertNull(farm.farmerContact)
    }

    // -----------------------------------------------------------------------
    // MapFarmScanDto helper — scan disease and date
    // -----------------------------------------------------------------------

    @Test
    fun mapFarmScanDto_parsesScanFields() {
        val json = """
            {
                "id": 99,
                "disease": "Rice Blast",
                "scan_date": "2026-10-17"
            }
        """.trimIndent()
        val scan = gson.fromJson(json, MapFarmScanDto::class.java)
        assertEquals("Rice Blast", scan.disease)
        assertEquals("2026-10-17", scan.scanDate)
    }

    @Test
    fun mapFarmScanDto_diseaseMayBeNull() {
        val json = """{"id": 100}"""
        val scan = gson.fromJson(json, MapFarmScanDto::class.java)
        assertNull(scan.disease)
    }
}
