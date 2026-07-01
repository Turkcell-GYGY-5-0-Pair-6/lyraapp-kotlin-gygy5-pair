package com.turkcell.lyraapp.data.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeProfileRepository @Inject constructor() : ProfileRepository {
    override suspend fun getProfileInfo(): Result<UserProfile> {
        delay(800L) // Ağ gecikmesi simülasyonu
        return Result.success(
            UserProfile(
                firstName = "Zeynep",
                lastName = "Kaya",
                username = "zeynepk",
                tier = "Premium",
                playlistsCount = 127,
                followersCount = "1.2B",
                followingCount = "348",
                initials = "ZK",
                premiumDaysLeft = 3
            )
        )
    }
}
