package com.turkcell.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.home.HomeRepository
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
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var hasCheckedSubscription = false

    init {
        loadFeed()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Retry -> loadFeed()
            is HomeIntent.SongSelected -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToNowPlaying(intent.song.id))
            }
            is HomeIntent.PlaylistClicked -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToPlaylistDetail(intent.playlistId))
                }
            }
            is HomeIntent.DismissSubscriptionWarning -> {
                _uiState.update { it.copy(showSubscriptionWarning = false) }
            }
            is HomeIntent.UpgradePlanClicked -> {
                _uiState.update { it.copy(showSubscriptionWarning = false) }
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToCheckout(intent.planId))
                }
            }
        }
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = homeRepository.getHomeFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            userInitials = feed.userInitials,
                            songs = feed.songs,
                            quickPicks = feed.quickPicks,
                            recentlyPlayed = feed.recentlyPlayed,
                            playlistsForYou = feed.playlistsForYou,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(HomeEffect.ShowError(error.message ?: "Ana sayfa yüklenemedi."))
                }

            checkSubscription()
        }
    }

    private fun checkSubscription() {
        if (hasCheckedSubscription) return
        viewModelScope.launch {
            profileRepository.getProfileInfo()
                .onSuccess { profile ->
                    if (profile.membershipType == "one-time" && profile.premiumDaysLeft != null) {
                        _uiState.update {
                            it.copy(
                                showSubscriptionWarning = true,
                                premiumDaysLeft = profile.premiumDaysLeft
                            )
                        }
                        hasCheckedSubscription = true
                    }
                }
        }
    }

    // java.time yerine Calendar: minSdk 24'te desugaring gerektirmez.
    private fun greetingForNow(): String =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            else -> "İyi akşamlar"
        }
}