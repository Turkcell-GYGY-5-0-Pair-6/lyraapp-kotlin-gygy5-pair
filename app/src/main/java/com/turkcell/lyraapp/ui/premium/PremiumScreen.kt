package com.turkcell.lyraapp.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Premium ekranının durumlu (stateful) giriş noktası.
 */
@Composable
fun PremiumRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PremiumEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    PremiumScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateToCheckout = onNavigateToCheckout,
        modifier = modifier,
    )
}

/**
 * Premium ekranının durumsuz (stateless) arayüz tasarımı.
 */
@Composable
fun PremiumScreen(
    state: PremiumUiState,
    onIntent: (PremiumIntent) -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF131011) // Mockup'taki koyu arka plan rengi
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF331D24), // Üstteki hafif pembe/rose ışıltı
                            Color(0xFF131011),
                            Color(0xFF131011)
                        )
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Sol Üst Geri Butonu
            IconButton(
                onClick = { onIntent(PremiumIntent.BackClick) },
                modifier = Modifier.padding(start = 12.dp, top = 12.dp)
            ) {
                Icon(
                    imageVector = LyraIcons.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Premium Logosu (Pembe kart içinde badge)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF9BCCB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LyraIcons.PremiumBadge,
                        contentDescription = null,
                        tint = Color(0xFF8C2E4C),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Başlık
                Text(
                    text = "LyraApp Premium",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Alt Başlık
                Text(
                    text = "Reklamsız, sınırsız ve çevrimdışı müziğin keyfini çıkar.",
                    color = Color(0xFFC0A9AE),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Özellikler Listesi
                val features = listOf(
                    PremiumFeature(LyraIcons.Block, "Reklamsız dinleme", "Kesintisiz, sınırsız müzik"),
                    PremiumFeature(LyraIcons.SkipNext, "Sınırsız atlama", "İstediğin şarkıya geç"),
                    PremiumFeature(LyraIcons.DownloadCircle, "Çevrimdışı indirme", "İnternet olmadan dinle"),
                    PremiumFeature(LyraIcons.Waveform, "Yüksek ses kalitesi", "320 kbps net ses"),
                    PremiumFeature(LyraIcons.Devices, "Tüm cihazlarında", "Telefon, tablet ve masaüstü")
                )

                features.forEach { feature ->
                    FeatureRow(feature = feature)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Plan Seçin Başlığı
                Text(
                    text = "Planını seç",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )

                // Plan 1: Aylık abonelik
                PlanCard(
                    title = "Aylık abonelik",
                    subtext = "İstediğin zaman iptal et",
                    price = "₺59,99 / ay",
                    badgeText = null,
                    isSelected = state.selectedPlanId == "recurring",
                    onClick = {
                        onIntent(PremiumIntent.SelectPlan("recurring"))
                        onNavigateToCheckout("recurring")
                    }
                )

                // Plan 2: Tek seferlik
                PlanCard(
                    title = "Tek seferlik",
                    subtext = "30 gün erişim · otomatik yenilenmez",
                    price = "₺79,99",
                    badgeText = null,
                    isSelected = state.selectedPlanId == "one-time",
                    onClick = {
                        onIntent(PremiumIntent.SelectPlan("one-time"))
                        onNavigateToCheckout("one-time")
                    }
                )

                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(80.dp)
                )
            }
        }
    }
}

private data class PremiumFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun FeatureRow(feature: PremiumFeature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = Color(0xFFFFB2C5),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = feature.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = feature.description,
                color = Color(0xFF9E8E91),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtext: String,
    price: String,
    badgeText: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFB2C5) else Color.White.copy(alpha = 0.12f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Özel Seçim Yuvarlağı (Radio)
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) Color(0xFFFFB2C5) else Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB2C5))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFB2C5))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color(0xFF5D1E31),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                color = Color(0xFF9E8E91),
                fontSize = 12.sp
            )
        }

        Text(
            text = price,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Preview(name = "Premium Screen Preview", showBackground = true, showSystemUi = true)
@Composable
private fun PremiumScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        PremiumScreen(
            state = PremiumUiState(),
            onIntent = {},
            onNavigateToCheckout = {}
        )
    }
}
