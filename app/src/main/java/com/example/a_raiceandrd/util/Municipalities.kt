package com.example.a_raiceandrd.util

/**
 * Canonical list of the 12 Bataan municipalities.
 * Use [displayName] in spinners/dropdowns; [slug] when sending to the API.
 * "All municipalities" is not included — callers add it themselves when needed.
 */
data class MunicipalityEntry(val slug: String, val displayName: String)

val BATAAN_MUNICIPALITIES: List<MunicipalityEntry> = listOf(
    MunicipalityEntry("abucay",     "Abucay"),
    MunicipalityEntry("bagac",      "Bagac"),
    MunicipalityEntry("balanga",    "Balanga"),
    MunicipalityEntry("dinalupihan","Dinalupihan"),
    MunicipalityEntry("hermosa",    "Hermosa"),
    MunicipalityEntry("limay",      "Limay"),
    MunicipalityEntry("mariveles",  "Mariveles"),
    MunicipalityEntry("morong",     "Morong"),
    MunicipalityEntry("orani",      "Orani"),
    MunicipalityEntry("orion",      "Orion"),
    MunicipalityEntry("pilar",      "Pilar"),
    MunicipalityEntry("samal",      "Samal"),
)
