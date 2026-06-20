package com.turkcell.lyraapp.data.auth

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private val completedProfiles = mutableMapOf<String, Boolean>() // phone -> isCompleted
    private var currentPhone: String? = null
    private var accessToken: String? = null

    override suspend fun requestOtp(phone: String): Result<OtpResponseData> {
        delay(NETWORK_DELAY_MS)
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Telefon numarası boş olamaz."))
        }
        currentPhone = phone
        val isCompleted = completedProfiles[phone] ?: false
        return Result.success(
            OtpResponseData(
                sent = true,
                firstTime = !isCompleted
            )
        )
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<VerifyOtpResponseData> {
        delay(NETWORK_DELAY_MS)
        val validCodes = setOf("280600", "260702", "250506", "101000", "346134", "123456")
        return if (validCodes.contains(code)) {
            accessToken = "fake_jwt_token_for_$phone"
            Result.success(
                VerifyOtpResponseData(
                    accessToken = accessToken!!,
                    refreshToken = "fake_refresh_token_for_$phone",
                    expiresIn = 3600
                )
            )
        } else {
            Result.failure(IllegalArgumentException("Geçersiz doğrulama kodu."))
        }
    }

    override suspend fun updateInformation(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        if (firstName.isBlank() || lastName.isBlank() || birthDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Lütfen tüm alanları doldurun."))
        }
        val phone = currentPhone
        if (phone != null) {
            completedProfiles[phone] = true
        }
        return Result.success(Unit)
    }

    override fun getAccessToken(): String? = accessToken

    private companion object {
        const val NETWORK_DELAY_MS = 1_000L
    }
}