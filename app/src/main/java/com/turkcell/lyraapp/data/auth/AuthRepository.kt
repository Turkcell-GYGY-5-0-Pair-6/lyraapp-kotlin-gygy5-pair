package com.turkcell.lyraapp.data.auth

interface AuthRepository{

    suspend fun login(phoneNumber: String, password: String): Result<Unit>
}