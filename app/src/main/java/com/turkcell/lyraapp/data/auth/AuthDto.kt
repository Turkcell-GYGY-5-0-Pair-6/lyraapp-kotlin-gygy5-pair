package com.turkcell.lyraapp.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    val phone: String
)

@Serializable
data class OtpResponse(
    val data: OtpResponseData
)

@Serializable
data class OtpResponseData(
    val sent: Boolean,
    val firstTime: Boolean
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val code: String
)

@Serializable
data class VerifyOtpResponse(
    val data: VerifyOtpResponseData
)

@Serializable
data class VerifyOtpResponseData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserDto? = null,
    val firstTime: Boolean = false,
    val tokenType: String? = null
)

@Serializable
data class UpdateInformationRequest(
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

@Serializable
data class UpdateInformationResponse(
    val data: UserDto
)

@Serializable
data class UserDto(
    val id: String? = null,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val profileCompleted: Boolean = false
)
