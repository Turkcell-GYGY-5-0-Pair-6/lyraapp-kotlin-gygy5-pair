package com.turkcell.lyraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.login.LoginScreen
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferenceRepository: ThemePreferenceRepository

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // İzin sonucu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themePreferenceRepository.isDarkTheme.collectAsState(initial = true)
            LyraAppTheme(darkTheme = isDarkTheme) {
                LyraNavHost(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            themePreferenceRepository.setDarkTheme(!isDarkTheme)
                        }
                    },
                )
            }
        }
    }
}

