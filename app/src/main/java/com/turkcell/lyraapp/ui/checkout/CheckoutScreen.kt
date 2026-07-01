package com.turkcell.lyraapp.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Ödeme ekranının durumlu (stateful) giriş noktası.
 */
@Composable
fun CheckoutRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutEffect.NavigateBack -> onNavigateBack()
                is CheckoutEffect.NavigateToSuccess -> onNavigateToSuccess()
                is CheckoutEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CheckoutScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Ödeme ekranının durumsuz (stateless) arayüz tasarımı.
 */
@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    onIntent: (CheckoutIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val isRecurring = state.planId == "recurring"
    val planPriceText = if (isRecurring) "₺59,99" else "₺79,99"
    val planPriceSubText = if (isRecurring) "$planPriceText / ay" else planPriceText
    val planNameText = if (isRecurring) "Aylık abonelik" else "Tek seferlik"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF131011),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Üst Başlık ve Geri Butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onIntent(CheckoutIntent.BackClick) }) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ödeme",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kredi Kartı Görseli
                CreditCardPreview(state = state)

                Spacer(modifier = Modifier.height(24.dp))

                // Kart Numarası Alanı
                CheckoutTextField(
                    label = "Kart numarası",
                    value = state.cardNumber,
                    onValueChange = { onIntent(CheckoutIntent.CardNumberChanged(it)) },
                    placeholder = "0000 0000 0000 0000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CardNumberFilter()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kart Üzerindeki İsim Alanı
                CheckoutTextField(
                    label = "Kart üzerindeki isim",
                    value = state.cardHolderName,
                    onValueChange = { onIntent(CheckoutIntent.CardHolderNameChanged(it)) },
                    placeholder = "Ad Soyad"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Son Kullanma ve CVC Yan Yana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CheckoutTextField(
                        label = "Son kullanma",
                        value = state.expiryDate,
                        onValueChange = { onIntent(CheckoutIntent.ExpiryDateChanged(it)) },
                        placeholder = "AA/YY",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        visualTransformation = ExpiryDateFilter()
                    )
                    CheckoutTextField(
                        label = "CVC",
                        value = state.cvc,
                        onValueChange = { onIntent(CheckoutIntent.CvcChanged(it)) },
                        placeholder = "123",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sipariş Özeti Kutusu
                OrderSummaryBox(
                    planNameText = planNameText,
                    planPriceText = planPriceText,
                    planPriceSubText = planPriceSubText
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Güvenli Ödeme Butonu
                Button(
                    onClick = { onIntent(CheckoutIntent.SubmitPayment) },
                    enabled = state.isFormValid && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB2C5),
                        contentColor = Color(0xFF5D1E31),
                        disabledContainerColor = Color(0xFFFFB2C5).copy(alpha = 0.3f),
                        disabledContentColor = Color(0xFF5D1E31).copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF5D1E31),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = LyraIcons.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$planPriceSubText öde",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SSL Bilgi Satırı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = LyraIcons.Check,
                        contentDescription = null,
                        tint = Color(0xFF9E8E91),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ödemeniz 256-bit SSL ile güvende",
                        color = Color(0xFF9E8E91),
                        fontSize = 12.sp
                    )
                }

                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun CreditCardPreview(state: CheckoutUiState) {
    val displayCardNumber = if (state.cardNumber.isEmpty()) {
        "•••• •••• •••• ••••"
    } else {
        val raw = state.cardNumber.replace(" ", "")
        val padded = raw.padEnd(16, '•')
        padded.chunked(4).joinToString(" ")
    }

    val displayHolderName = if (state.cardHolderName.trim().isEmpty()) {
        "AD SOYAD"
    } else {
        state.cardHolderName.uppercase()
    }

    val displayExpiry = if (state.expiryDate.isEmpty()) {
        "AA/YY"
    } else {
        val padded = state.expiryDate.padEnd(4, '•')
        "${padded.substring(0, 2)}/${padded.substring(2, 4)}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8B4D5D), Color(0xFFC88276))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        // Dekorasyon Amaçlı Daire (Mockup'taki yarı şeffaf dairesel detaylar)
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 80.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Üst Satır: Chip ve Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Chip
                Box(
                    modifier = Modifier
                        .size(42.dp, 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD4AF37))
                )

                // Kart Ağı Logosu (Overlap Daireler)
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                }
            }

            // Orta Satır: Kart Numarası
            Text(
                text = displayCardNumber,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Alt Satır: Kart Sahibi ve SKT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "KART SAHİBİ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayHolderName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SKT",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayExpiry,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color(0xFFC0A9AE),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = Color(0xFF6E686A), fontSize = 15.sp)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1718),
                unfocusedContainerColor = Color(0xFF1A1718),
                focusedBorderColor = Color(0xFFFFB2C5),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFFB2C5)
            )
        )
    }
}

@Composable
private fun OrderSummaryBox(
    planNameText: String,
    planPriceText: String,
    planPriceSubText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9BCCB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LyraIcons.PremiumBadge,
                    contentDescription = null,
                    tint = Color(0xFF8C2E4C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LyraApp Premium",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = planNameText,
                    color = Color(0xFF9E8E91),
                    fontSize = 13.sp
                )
            }

            Text(
                text = planPriceSubText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bugün ödenecek",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = planPriceText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )


        }

    }
    Spacer(
        modifier = Modifier
            .navigationBarsPadding()
            .height(80.dp)
    )
}

@Preview(name = "Checkout Screen Preview", showBackground = true, showSystemUi = true)
@Composable
private fun CheckoutScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        CheckoutScreen(
            state = CheckoutUiState(),
            onIntent = {}
        )
    }
}

private class CardNumberFilter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i % 4 == 3 && i != 15) {
                    append(" ")
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                return offset + 3
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                return offset - 3
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private class ExpiryDateFilter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1 && raw.length > 2) {
                    append("/")
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                return offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                return (offset - 1).coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
