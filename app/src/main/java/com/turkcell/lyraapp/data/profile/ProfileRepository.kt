package com.turkcell.lyraapp.data.profile

interface ProfileRepository {
    suspend fun getProfileInfo(): Result<UserProfile>

    suspend fun checkout(
        plan: String,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String,
        holderName: String
    ): Result<Unit>
}

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val username: String,
    val tier: String,
    val playlistsCount: Int,
    val followersCount: String,
    val followingCount: String,
    val initials: String,
    val premiumDaysLeft: Int? = null,
    val membershipType: String? = null
)
