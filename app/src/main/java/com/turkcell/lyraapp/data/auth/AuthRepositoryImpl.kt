package com.turkcell.lyraapp.data.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    private var accessToken: String? = null

    override suspend fun requestOtp(phone: String): Result<OtpResponseData> = runCatching {
        authApi.requestOtp(OtpRequest(phone)).data
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<VerifyOtpResponseData> = runCatching {
        val response = authApi.verifyOtp(VerifyOtpRequest(phone, code)).data
        accessToken = response.accessToken
        response
    }

    override suspend fun updateInformation(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<Unit> = runCatching {
        val token = accessToken ?: throw IllegalStateException("Doğrulama token'ı bulunamadı. Lütfen önce giriş yapın.")
        authApi.updateInformation(
            authorization = "Bearer $token",
            request = UpdateInformationRequest(firstName, lastName, birthDate)
        )
        Unit
    }

    override fun getAccessToken(): String? = accessToken
    
    override fun logout() {
        accessToken = null
    }
}
