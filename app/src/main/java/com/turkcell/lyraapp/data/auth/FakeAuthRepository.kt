package com.turkcell.lyraapp.data.auth


import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAuthRepository @Inject constructor() : AuthRepository{
    override suspend fun login(phoneNumber: String, password: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return if (password.isNotBlank()){
            Result.success(Unit)
        }
        else{
            Result.failure(IllegalArgumentException("Şifre Boş Olamaz"))
        }
    }

    private companion object{
        const val NETWORK_DELAY_MS = 1_000L
    }

}