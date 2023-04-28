package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Create a property for the base URL
private const val BASE_URL = "https://airlabs.co/api/v9/"
//AirLabs API Key
private const val API_KEY = "602a0ea7-0458-4107-8789-4de50f8a36ef"

// Build the Moshi object with Kotlin adapter factory that Retrofit will be using to parse JSON
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Build a Retrofit object with the Moshi converter
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface PlaneApiService {
    // Declare a suspended function to get the API
    @GET("flights?api_key=${API_KEY}")
    fun getPlane(@Query("bbox") bbox : String) : Call<DataClass>
}

// Create an object that provides a lazy-initialized retrofit service
object PlaneApi {
    val retrofitService: PlaneApiService by lazy {
        retrofit.create(PlaneApiService::class.java)
    }
}
