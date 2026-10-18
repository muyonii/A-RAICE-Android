package com.example.a_raiceandrd.model

import androidx.annotation.DrawableRes
import com.example.a_raiceandrd.model.Treatment

data class Disease(
    val id: Int,
    val name: String,
    val definition: String,
    val category: String,
    val history: String = "",
    val causes: String = "",
    val symptoms: String = "",
    val prevention: String = "",
    val treatment: List<Treatment> = emptyList(),
    val sources: String = "",
    @DrawableRes val imageRes: Int
)
