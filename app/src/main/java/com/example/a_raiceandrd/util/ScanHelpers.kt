package com.example.a_raiceandrd.util

import com.example.a_raiceandrd.data.repository.api.DiseaseDto

private const val HEALTHY_DISEASE_NAME = "Healthy Rice Plant"

/** Returns true when the scan disease name represents a healthy plant. */
fun isHealthy(name: String?): Boolean =
    name.equals(HEALTHY_DISEASE_NAME, ignoreCase = true)

/** Returns true when no disease was identified (model class not recognised). */
fun isUnrecognised(disease: DiseaseDto?): Boolean = disease == null
