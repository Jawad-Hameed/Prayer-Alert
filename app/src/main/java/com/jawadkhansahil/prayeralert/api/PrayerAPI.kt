package com.jawadkhansahil.prayeralert.api

import com.jawadkhansahil.prayeralert.models.PrayersModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerAPI {

    @GET("/v1/timings/{date}")
    suspend fun getData(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ) : Response<PrayersModel>
}