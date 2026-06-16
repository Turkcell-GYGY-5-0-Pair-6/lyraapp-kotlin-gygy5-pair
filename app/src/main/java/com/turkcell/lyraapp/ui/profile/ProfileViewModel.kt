package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Profil ekranının MVI ViewModel sınıfı.
 * Tek giriş noktası [onIntent]'tir.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        onIntent(ProfileIntent.LoadProfile)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.Logout -> logout()
        }
    }

    private fun loadProfile() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = profileRepository.getProfileInfo()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { profile ->
                    _uiState.update { it.copy(userProfile = profile) }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Profil bilgileri yüklenemedi."
                    _uiState.update { it.copy(error = errorMsg) }
                    _effect.send(ProfileEffect.ShowError(errorMsg))
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _effect.send(ProfileEffect.NavigateToLogin)
        }
    }
}
