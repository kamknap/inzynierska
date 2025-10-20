package com.example.fithub

import com.example.fithub.data.*
import retrofit2.http.*


interface OpenFoodFactsService {
    @GET("product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsProductResponse

    @GET("search")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("fields") fields: String = "product_name,brands,nutriments,code",
        @Query("page_size") limit: Int = 10,
        @Query("json") json: Int = 1
    ): OpenFoodFactsSearchResponse
}