package io.moxd.mocohands_on.model.peripherals.uwbeesp32

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class LedState(val state: Boolean)

interface UWBeEsp32Service {
    @GET("info")
    suspend fun getDeviceInfo(): DeviceInfo

    @POST("led")
    suspend fun setLedState(@Body body: LedState)

    @POST("led/toggle")
    suspend fun toggleLed()

    companion object {
        fun createApiService(url: String): UWBeEsp32Service {
            val moshi = Moshi.Builder()
                .add(DeviceTypeAdapter())
                .add(DeviceIdAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(UWBeEsp32Service::class.java)
        }
    }
}
