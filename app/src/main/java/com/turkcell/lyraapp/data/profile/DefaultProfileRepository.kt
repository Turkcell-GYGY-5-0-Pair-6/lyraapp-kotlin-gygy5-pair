package com.turkcell.lyraapp.data.profile

import com.turkcell.lyraapp.data.auth.AuthApi
import com.turkcell.lyraapp.data.auth.AuthRepository
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
                val expires = java.time.Instant.parse(user.membership.expiresAt)
                val now = java.time.Instant.now()
                val days = java.time.Duration.between(now, expires).toDays().toInt()
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
            premiumDaysLeft = premiumDaysLeft
        )
    }
}
