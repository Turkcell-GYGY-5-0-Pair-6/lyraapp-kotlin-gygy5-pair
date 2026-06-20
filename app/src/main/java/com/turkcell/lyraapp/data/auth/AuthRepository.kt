package com.turkcell.lyraapp.data.auth

interface AuthRepository {

    suspend fun requestOtp(phone: String): Result<OtpResponseData>

    suspend fun verifyOtp(phone: String, code: String): Result<VerifyOtpResponseData>

    suspend fun updateInformation(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<Unit>

    fun getAccessToken(): String?
}