package com.turkcell.lyraapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.profile.UserProfile
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Profil ekranının durumlu (stateful) giriş noktası.
 */
@Composable
fun ProfileRoute(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ProfileScreen(
        state = uiState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onIntent = viewModel::onIntent,
        onNavigateToPremium = onNavigateToPremium,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Profil ekranının durumsuz (stateless) arayüz tasarımı.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst Başlık ve Ayarlar Çark İkonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { /* Gelecek sürümde eklenecektir */ }) {
                    Icon(
                        imageVector = LyraIcons.Settings,
                        contentDescription = "Ayarlar",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (state.isLoading && state.userProfile == null) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.weight(1f))
            } else {
                state.userProfile?.let { user ->
                    Spacer(modifier = Modifier.height(8.dp))

                    // Profil Fotoğrafı (Avatar) - Rose/Kahve gradyanlı
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8C5D6C), Color(0xFF5D4037))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.initials,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ad Soyad
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Kullanıcı Adı ve Üyelik Tipi
                    val subtitleText = buildString {
                        append("@${user.username}")
                        append(" · ${user.tier}")
                        if (user.tier == "Premium" && user.premiumDaysLeft != null) {
                            append(" · ${user.premiumDaysLeft} gün")
                        }
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // İstatistikler Satırı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(value = user.playlistsCount.toString(), label = "Çalma listesi")
                        StatItem(value = user.followersCount, label = "Takipçi")
                        StatItem(value = user.followingCount, label = "Takip")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Premium Bilgilendirme Kartı
                    PremiumBanner(
                        tier = user.tier,
                        premiumDaysLeft = user.premiumDaysLeft,
                        onClick = onNavigateToPremium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Görünüm Başlığı
                    Text(
                        text = "Görünüm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tema Değiştirici (Segmented Control)
                    ThemeSegmentedControl(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Seçenekler Menü Listesi
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        ProfileOptionItem(
                            icon = LyraIcons.Waveform,
                            title = "Ses kalitesi",
                            trailingText = "Yüksek",
                            onClick = { /* Gelecek sürümde eklenecektir */ }
                        )
                        ProfileOptionItem(
                            icon = LyraIcons.DownloadCircle,
                            title = "Çevrimdışı indirme",
                            trailingText = "Açık",
                            onClick = { /* Gelecek sürümde eklenecektir */ }
                        )
                        ProfileOptionItem(
                            icon = LyraIcons.Notifications,
                            title = "Bildirimler",
                            onClick = { /* Gelecek sürümde eklenecektir */ }
                        )
                        ProfileOptionItem(
                            icon = LyraIcons.Lock,
                            title = "Gizlilik",
                            onClick = { /* Gelecek sürümde eklenecektir */ }
                        )
                        ProfileOptionItem(
                            icon = LyraIcons.HelpCircle,
                            title = "Yardım ve destek",
                            onClick = { /* Gelecek sürümde eklenecektir */ }
                        )
                        ProfileOptionItem(
                            icon = LyraIcons.Logout,
                            title = "Çıkış Yap",
                            onClick = { onIntent(ProfileIntent.Logout) },
                            iconColor = MaterialTheme.colorScheme.error,
                            textColor = MaterialTheme.colorScheme.error,
                            showChevron = false
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeSegmentedControl(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Açık Tema Butonu
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (!isDarkTheme) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (isDarkTheme) onToggleTheme() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = LyraIcons.LightMode,
                    contentDescription = null,
                    tint = if (!isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Açık",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (!isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Koyu Tema Butonu
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkTheme) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (!isDarkTheme) onToggleTheme() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = LyraIcons.DarkMode,
                    contentDescription = null,
                    tint = if (isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Koyu",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = LyraIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumBanner(
    tier: String,
    premiumDaysLeft: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremium = tier == "Premium"
    val gradientColors = if (isPremium) {
        listOf(Color(0xFF8C5D6C), Color(0xFF5D4037))
    } else {
        listOf(Color(0xFF2C2C2C), Color(0xFF1E1E1E))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LyraIcons.PremiumBadge,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isPremium) {
                    if (premiumDaysLeft != null) "Premium · $premiumDaysLeft gün kaldı" else "Premium"
                } else {
                    "Premium'a Geç"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isPremium) {
                    "Yenile ya da aboneliğe geç"
                } else {
                    "Reklamsız ve çevrimdışı müzik keyfi için başla"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Icon(
            imageVector = LyraIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(name = "Profile - Light Theme", showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        ProfileScreen(
            state = ProfileUiState(
                userProfile = UserProfile(
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
            ),
            isDarkTheme = false,
            onToggleTheme = {},
            onIntent = {},
            onNavigateToPremium = {}
        )
    }
}

@Preview(name = "Profile - Dark Theme", showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        ProfileScreen(
            state = ProfileUiState(
                userProfile = UserProfile(
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
            ),
            isDarkTheme = true,
            onToggleTheme = {},
            onIntent = {},
            onNavigateToPremium = {}
        )
    }
}
