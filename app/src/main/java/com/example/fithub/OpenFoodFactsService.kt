package com.example.fithub

import com.example.fithub.data.*
import retrofit2.http.*


interface OpenFoodFactsService {
    @GET("product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsProductResponse

    @GET("cgi/search.pl") // Zmieniona ścieżka na tę ze starego API
    suspend fun searchProducts(
        @Query("search_terms") query: String, // Prawidłowy parametr dla tej wersji API
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") limit: Int
    ): OpenFoodFactsSearchResponse
}