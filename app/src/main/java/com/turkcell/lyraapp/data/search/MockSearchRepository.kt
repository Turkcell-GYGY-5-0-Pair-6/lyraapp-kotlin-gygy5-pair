package com.turkcell.lyraapp.data.search

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockSearchRepository @Inject constructor() : SearchRepository {
    override suspend fun getSearchCategories(): Result<List<SearchCategory>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(CATEGORIES)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L

        val CATEGORIES = listOf(
            SearchCategory("sc-1", "Pop", 0xFF2ECC71, 0xFF27AE60),
            SearchCategory("sc-2", "Elektronik", 0xFF8E44AD, 0xFF5B2C6F),
            SearchCategory("sc-3", "Akustik", 0xFFFD9644, 0xFFFC6E20),
            SearchCategory("sc-4", "Lo-fi", 0xFF1ABC9C, 0xFF16A085),
            SearchCategory("sc-5", "Indie", 0xFF3498DB, 0xFF2E86C1),
            SearchCategory("sc-6", "Jazz", 0xFF9B59B6, 0xFF7D3C98),
            SearchCategory("sc-7", "Klasik", 0xFFE74C3C, 0xFFC0392B),
            SearchCategory("sc-8", "Yolculuk", 0xFFF1C40F, 0xFFD4AC0D),
        )
    }
}
