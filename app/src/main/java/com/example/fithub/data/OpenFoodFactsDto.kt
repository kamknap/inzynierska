package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsProductResponse(
    val product: OpenFoodFactsProduct?
)

data class OpenFoodFactsSearchResponse(
    val products: List<OpenFoodFactsProduct>?
)

data class OpenFoodFactsProduct(
    val code: String?,
    val product_name: String?,
    val brands: String?,
    val image_url: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    val energy_kcal_100g: Double?,
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    val energy_100g: Double?,
    val energy_kj_100g: Double?,
    val proteins_100g: Double?,
    val carbohydrates_100g: Double?,
    val fat_100g: Double?,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val sodium_100g: Double?,
    val salt_100g: Double?
)
