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
                premiumDaysLeft = 3,
                membershipType = "recurring"
            )
        )
    }

    override suspend fun checkout(
        plan: String,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String,
        holderName: String
    ): Result<Unit> {
        delay(1500L)
        val cleanCard = cardNumber.replace(" ", "")
        return if (cleanCard == "4242424242424242") {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Ödeme reddedildi. Lütfen geçerli bir test kartı kullanın."))
        }
    }
}
