package com.turkcell.lyraapp.data.auth

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import kotlinx.serialization.Serializable

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

    @GET("api/v1/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): ProfileResponse

    @POST("api/v1/memberships/checkout")
    suspend fun checkout(
        @Header("Authorization") authorization: String,
        @Body request: CheckoutRequest
    ): CheckoutResponse
}

@Serializable
data class ProfileResponse(
    val data: UserDto
)

@Serializable
data class CheckoutRequest(
    val plan: String,
    val card: CardDto
)

@Serializable
data class CardDto(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String
)

@Serializable
data class CheckoutResponse(
    val data: CheckoutResponseData
)

@Serializable
data class CheckoutResponseData(
    val payment: PaymentDto,
    val membership: MembershipDto
)

@Serializable
data class PaymentDto(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String
)
