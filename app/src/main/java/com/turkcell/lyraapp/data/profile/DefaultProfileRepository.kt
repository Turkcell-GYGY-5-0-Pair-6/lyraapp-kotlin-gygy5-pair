package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.auth.AuthApi
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.CheckoutRequest
import com.turkcell.lyraapp.data.auth.CardDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authRepository: AuthRepository
) : ProfileRepository {

    override suspend fun getProfileInfo(): Result<UserProfile> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
        val user = authApi.getProfile(authorization = "Bearer $token").data
        val first = user.firstName ?: ""
        val last = user.lastName ?: ""
        val initials = ((first.firstOrNull()?.toString() ?: "") + (last.firstOrNull()?.toString() ?: "")).uppercase()
        val isPremium = user.membership != null && user.membership.status == "active"
        val premiumDaysLeft = if (isPremium && !user.membership?.expiresAt.isNullOrEmpty()) {
            runCatching {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val cleanDateStr = if (user.membership.expiresAt.length >= 19) {
                    user.membership.expiresAt.substring(0, 19)
                } else {
                    user.membership.expiresAt
                }
                val expiresDate = sdf.parse(cleanDateStr)
                val expiresMillis = expiresDate?.time ?: 0L
                val nowMillis = System.currentTimeMillis()
                val diffMillis = expiresMillis - nowMillis
                val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                if (days < 0) 0 else days
            }.getOrNull()
        } else {
            null
        }
        UserProfile(
            firstName = first,
            lastName = last,
            username = user.phone ?: "",
            tier = if (isPremium) "Premium" else "Free",
            playlistsCount = 0,
            followersCount = "0",
            followingCount = "0",
            initials = if (initials.isEmpty()) "U" else initials,
            premiumDaysLeft = premiumDaysLeft,
            membershipType = user.membership?.type
        )
    }

    override suspend fun checkout(
        plan: String,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String,
        holderName: String
    ): Result<Unit> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
        val formattedCardNumber = cardNumber.filter { it.isDigit() }.chunked(4).joinToString(" ")
        val request = CheckoutRequest(
            plan = plan,
            card = CardDto(
                number = formattedCardNumber,
                expMonth = expMonth,
                expYear = expYear,
                cvc = cvc,
                holderName = holderName
            )
        )
        authApi.checkout(authorization = "Bearer $token", request = request)
    }
}
