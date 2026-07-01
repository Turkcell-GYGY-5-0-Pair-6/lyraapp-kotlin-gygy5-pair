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
        UserProfile(
            firstName = first,
            lastName = last,
            username = user.phone ?: "",
            tier = if (isPremium) "Premium" else "Free",
            playlistsCount = 0,
            followersCount = "0",
            followingCount = "0",
            initials = if (initials.isEmpty()) "U" else initials
        )
    }
}
