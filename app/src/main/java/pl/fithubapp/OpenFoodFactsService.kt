package pl.fithubapp

import pl.fithubapp.data.*
import retrofit2.http.*

interface OpenFoodFactsService {
    // Endpoint dla barcode
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsProductResponse

    // Endpoint dla wyszukiwania po nazwie
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") limit: Int
    ): OpenFoodFactsSearchResponse
}