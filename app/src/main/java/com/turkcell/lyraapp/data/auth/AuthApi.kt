package com.turkcell.lyraapp.data.auth

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequest): OtpResponse

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("api/v1/me/update-informations")
    suspend fun updateInformation(
        @Header("Authorization") authorization: String,
        @Body request: UpdateInformationRequest
    ): UpdateInformationResponse
}
