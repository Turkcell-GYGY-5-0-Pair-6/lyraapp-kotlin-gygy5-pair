package com.turkcell.lyraapp.ui.checkout.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
 * Ödeme başarılı ekranının durumlu (stateful) giriş noktası.
 */
@Composable
fun CheckoutSuccessRoute(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutSuccessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Geri tuşu basıldığında da doğrudan ana sayfaya yönlendirilir.
    BackHandler {
        onNavigateToHome()
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutSuccessEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    CheckoutSuccessScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

/**
 * Ödeme başarılı ekranının durumsuz (stateless) arayüz tasarımı.
 */
@Composable
fun CheckoutSuccessScreen(
    state: CheckoutSuccessUiState,
    onIntent: (CheckoutSuccessIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF131011)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A1B20), // Koyu gül kurusu/aubergine üst bölge
                            Color(0xFF131011), // Koyu arka plan geçişi
                            Color(0xFF131011)
                        )
                    )
                )
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Boşluk tutucu (Responsive hizalama için üst bölge)
            Spacer(modifier = Modifier.height(48.dp))

            // Orta içerik (Çember, Başlıklar ve Rozet)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Dış ışıma halkası
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFB2C5).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pembe çember
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFBC4D0),
                                        Color(0xFFFFB2C5)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LyraIcons.Check,
                            contentDescription = "Başarılı",
                            tint = Color(0xFF5D1E31),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Başlık
                Text(
                    text = "Premium aktif! 🎉",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detay Metni
                Text(
                    text = "30 günlük Premium erişimin başladı.\nReklamsız, sınırsız ve çevrimdışı\ndinlemenin keyfini çıkar.",
                    color = Color(0xFFC0A9AE),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Üyelik Rozet Hapı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF23171B))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = LyraIcons.PremiumBadge,
                        contentDescription = null,
                        tint = Color(0xFFFFB2C5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Premium · 30 gün",
                        color = Color(0xFFE4D5D9),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Başlama Butonu
            Button(
                onClick = { onIntent(CheckoutSuccessIntent.StartListeningClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB2C5),
                    contentColor = Color(0xFF5D1E31)
                )
            ) {
                Text(
                    text = "Dinlemeye başla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(name = "Checkout Success Screen Preview", showBackground = true, showSystemUi = true)
@Composable
private fun CheckoutSuccessScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        CheckoutSuccessScreen(
            state = CheckoutSuccessUiState(),
            onIntent = {}
        )
    }
}
