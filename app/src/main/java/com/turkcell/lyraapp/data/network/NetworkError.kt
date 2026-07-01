package com.turkcell.lyraapp.data.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Throwable nesnelerini kullanıcının anlayabileceği Türkçe hata mesajlarına dönüştürür.
 */
fun Throwable.toUserFriendlyMessage(
    fallbackMessage: String = "Bir hata oluştu. Lütfen tekrar deneyin.",
    unauthorizedMessage: String = "Girdiğiniz bilgiler hatalıdır. Lütfen kontrol edip tekrar deneyin."
): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                401 -> unauthorizedMessage
                400 -> "Geçersiz işlem. Lütfen girdiğiniz bilgileri kontrol edin."
                403 -> "Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır."
                404 -> "İstenen kaynak bulunamadı."
                500, 502, 503, 504 -> "Sunucuyla bağlantı kurulamadı. Lütfen daha sonra tekrar deneyin."
                else -> "Bir ağ hatası oluştu (Hata kodu: ${code()})."
            }
        }
        is IOException -> "İnternet bağlantınız bulunmuyor. Lütfen bağlantınızı kontrol edin."
        else -> message ?: fallbackMessage
    }
}
