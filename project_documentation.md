# LyraApp - Proje Dokümantasyonu

Bu döküman, Turkcell GYGY5 Android Eğitimi kapsamında geliştirilen LyraApp (Online/Offline Müzik Çalar Uygulaması) projesinin teknik mimarisini, teknoloji yığınını, veri akışını ve dosya yapısını detaylandırmaktadır.

---

## 1. Proje Genel Özeti

LyraApp, hem çevrimiçi hem de çevrimdışı müzik çalma özelliklerine sahip, modern Android geliştirme pratikleriyle inşa edilmiş bir mobil uygulamadır. Uygulama, uzaktaki bir RESTful API ile haberleşerek kullanıcı doğrulama, dinamik içerik önerileri, çalma geçmişi kaydı ve profil yönetimi gibi özellikleri gerçekleştirir. Müzik oynatma yetenekleri için arka planda çalışan servisler ve medya kontrolcüleri entegre edilmiştir.

---

## 2. Teknoloji Yığını ve Bağımlılıklar

Projenin temel kütüphane ve araç seti [libs.versions.toml](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/gradle/libs.versions.toml) ve [build.gradle.kts](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/build.gradle.kts) dosyalarında tanımlanmıştır. Detaylar aşağıda listelenmiştir:

*   **Dil:** Kotlin 2.0.21
*   **KSP Sürümü:** 2.0.21-1.0.28 (kapt yerine Kotlin Symbol Processing kullanılmıştır).
*   **Arayüz Teknolojisi:** Jetpack Compose (Compose BOM 2024.09.00 tabanlı Material 3 kütüphanesi).
*   **Mimari Yapı:** Model-View-Intent (MVI) mimarisi.
*   **Bağımlılık Enjeksiyonu (DI):** Dagger Hilt 2.51.1 (`androidx.hilt:hilt-navigation-compose:1.2.0` dahil).
*   **Asenkron Programlama:** Kotlin Coroutines ve Flow (`kotlinx-coroutines-android:1.9.0`).
*   **Ağ Katmanı:** Retrofit 2.11.0, OkHttp 4.12.0 ve `okhttp-logging-interceptor`.
*   **Serileştirme:** Kotlinx Serialization JSON 1.8.1.
*   **Medya Oynatma:** Jetpack Media3 (ExoPlayer ve Media3 Session 1.5.1).
*   **Yerel Depolama:** Preferences DataStore 1.1.0 (Tema tercihleri ve oturum yönetimi için).
*   **Navigasyon:** Jetpack Compose Navigation 2.8.5.

---

## 3. Mimari Yapı ve Temel Kararlar

Projenin genel mimarisine dair kararlar [decisions.md](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/docs/decisions.md) dökümanı çerçevesinde şekillendirilmiştir:

### 3.1. MVI (Model-View-Intent) Deseni
Uygulamadaki her ekran MVI prensiplerine göre tasarlanmıştır. Veri akışı tek yönlüdür (Unidirectional Data Flow - UDF):
1.  **State (UiState):** Ekranın o anki durumunu gösteren, immutable bir veri yapısıdır (`data class`).
2.  **Intent (UiIntent):** Kullanıcının ekranda gerçekleştirdiği eylemleri veya niyetleri temsil eder (`sealed interface`).
3.  **Effect (UiEffect):** Navigasyon, toast gösterimi gibi tek seferlik (one-shot) olayları yönetir (`sealed interface`).

MVI mimarisi hakkında detaylı bilgi için [mvi-overview.md](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/docs/architecture/mvi-overview.md), sözleşme kuralları için [mvi-contracts.md](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/docs/architecture/mvi-contracts.md) ve ViewModel entegrasyonu için [mvi-viewmodel-rules.md](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/docs/architecture/mvi-viewmodel-rules.md) dosyaları incelenebilir.

### 3.2. Medya Oynatma ve Playback Servisi
*   Müzik çalma işlemlerinin arka planda sorunsuz devam edebilmesi için [PlaybackService.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/player/PlaybackService.kt) adında bir Media3 Session servisi kurulmuştur.
*   [DefaultPlayerRepository.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/player/DefaultPlayerRepository.kt) üzerinden ExoPlayer kontrol edilmekte, çalınan şarkıların stream adresleri `/api/v1/songs/{id}/stream-url` uç noktasından dinamik olarak talep edilmektedir.
*   Şarkı oynatımı başladığında asenkron olarak `POST /api/v1/me/plays` endpoint'ine dinleme geçmişi gönderilerek sunucudaki "Son Çalınanlar" ve "Öneriler" algoritmaları beslenmektedir.

### 3.3. Ağ Entegrasyonu ve API'lar
*   [NetworkModule.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/network/NetworkModule.kt) dosyasında base url olarak `https://streaming-api.halitkalayci.com/` tanımlanmıştır.
*   **Doğrulama (Auth) Akışı:** Şifresiz OTP (SMS doğrulama) akışı kullanılmaktadır. `/api/v1/auth/otp/request` ile kod talep edilir, `/api/v1/auth/otp/verify` ile kod doğrulanarak JWT access token alınır. Token, [AuthRepositoryImpl.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/auth/AuthRepositoryImpl.kt) bünyesinde bellekte singleton düzeyde saklanır.
*   **Kişiselleştirilmiş Veriler:**
    *   Önerilen Şarkılar: `/api/v1/me/recommendations`
    *   Son Çalınanlar: `/api/v1/me/recently-played` (Ana sayfaya geri dönüldüğünde otomatik güncellenir, boş olması halinde UI üzerinde gizlenir).
    *   Kişiye Özel Müzikler ("Senin İçin"): `/api/v1/me/for-you`
    *   Kullanıcı Profili: `/api/v1/me` (Dönen bilgiler [DefaultProfileRepository.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/profile/DefaultProfileRepository.kt) ile işlenerek arayüze aktarılır).

### 3.4. Tema Yönetimi
*   [02-theme-system.md](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/docs/design/02-theme-system.md) dosyasında belgelendiği üzere, kullanıcı tercihleri `PreferencesDataStore` içinde saklanır.
*   Kullanıcının Dark veya Light tema tercihi uygulama yeniden başlatılsa dahi korunur ve `MainActivity` düzeyinde toplanarak Compose ağacına yayılır.

---

## 4. Proje Klasör ve Dosya Yapısı Dökümü

Proje klasör yapısı bileşenlerin ve sorumlulukların ayrışmasını sağlayacak şekilde düzenlenmiştir:

### 4.1. Sunum (UI) Katmanı Yapısı
Her ekran kendi içinde `Contract`, `ViewModel` ve `Screen` dosyalarını barındırır:
*   [ui/auth/login/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/auth/login) - Kullanıcı giriş ekranı (Referans implementasyondur).
*   [ui/auth/register/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/auth/register) - Kullanıcı kayıt ekranı.
*   [ui/home/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/home) - Ana sayfa besleme ekranı (Öneriler, son çalınanlar, senin için müzikler listelenir).
*   [ui/player/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/player) - Oynatıcı ekranı (`NowPlayingScreen`), mini oynatıcı çubuğu (`MiniPlayerBar`) ve bunlara ait MVI sözleşmeleri.
*   [ui/profile/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/profile) - Kullanıcı profil detayları ve ayarları.
*   [ui/library/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/library) - Kitaplık ekranı ve yeni çalma listesi oluşturma ([ui/library/create/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/library/create)).
*   [ui/playlist/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/playlist) - Çalma listesi detay ekranı.
*   [ui/search/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/search) - Şarkı arama ve listeleme ekranı.
*   [ui/favorites/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/favorites) - Beğenilen şarkılar ve favori çalma listeleri ekranı.
*   [ui/navigation/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/navigation) - BottomBar bileşeni ve uygulamadaki rotaları yöneten `LyraNavHost`.
*   [ui/theme/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/theme) - Renk paletleri, tipografi ayarları ve Material 3 temalandırması (`Theme.kt`).
*   [ui/icons/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/ui/icons) - Uygulamada kullanılan tüm özelleştirilmiş vektör ikonların toplandığı `LyraIcons.kt` dosyası.

### 4.2. Veri (Data) Katmanı Yapısı
Modüler repository yapıları interface ve implementasyon olarak ayrılmıştır:
*   [data/auth/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/auth) - Giriş, OTP işlemleri ve sahte repository alternatifi.
*   [data/player/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/player) - ExoPlayer entegrasyonu, oynatma durumu (`PlaybackState`) tanımı ve playback kontrol mekanizmaları.
*   [data/home/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/home) - Ana sayfa beslemesini API ve statik mocklar üzerinden orkestre eden yapı.
*   [data/profile/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/profile) - Kullanıcı profilini getiren servis entegrasyonu.
*   [data/library/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/library) - Kitaplık listeleme ve çalma listesi oluşturma veri tabakası.
*   [data/playlist/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/playlist) - Playlist detaylarını ve playlist içi şarkı listesini yöneten repository.
*   [data/songs/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/songs) - API üzerindeki genel şarkı istekleri ve serileştirilebilir DTO modelleri.
*   [data/network/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/data/network) - Retrofit ve OkHttpClient yapılandırması.

### 4.3. Bağımlılık Enjeksiyonu (DI) Modülleri
Hilt modülleri bağımlılıkları bağlamak için [di/](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/di) klasöründe toplanmıştır.
*   [di/AuthModule.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/di/AuthModule.kt)
*   [di/HomeModule.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/di/HomeModule.kt)
*   [di/PlayerModule.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/di/PlayerModule.kt)
*   [di/ProfileModule.kt](file:///c:/Users/Ahmet/Desktop/lyraapp-kotlin-gygy5-pair-main/app/src/main/java/com/turkcell/lyraapp/di/ProfileModule.kt)
*   (Diğer ekranlara ait enjeksiyon modülleri de bu klasörde mevcuttur).

---

## 5. Sık Yapılan Hatalar ve Öneriler

### 5.1. Sık Yapılan Hatalar
1.  **State İçi Mutable Değişkenler:** `UiState` data class'ı içerisinde mutable listeler (`MutableList`) veya değişken `var` alanları tanımlamak immutability ilkesini zedeler. Bu durum Compose tarafında yeniden çizim tetiklenmesi sorunlarına yol açar.
2.  **Effect Yerine State Flag Kullanımı:** Navigasyon tetiklemek için state içine `navigate: Boolean = false` gibi bayraklar koyup işlem sonrası bunu sıfırlamayı unutmak veya ekran döndürüldüğünde (configuration change) navigasyonun tekrarlanması hatasına düşmek.
3.  **UI Katmanında İş Mantığı Çalıştırmak:** Buton tıklaması gibi yerlerde doğrudan repository çağrısı yapmak veya veri formatlamak. Tüm iş mantığı ViewModel ve altındaki repository katmanında çözülmelidir.
4.  **ExoPlayer Stream URL'lerinin Önbelleğe Alınması:** İmzalı stream URL'leri (signed URLs) geçici süreli (TTL ~300sn) geçerliliğe sahiptir. URL'leri listeyle birlikte kaydetmek yerine, oynatılma tetiklendiği an istek atılarak güncel URL alınmalıdır.

### 5.2. Geleceğe Yönelik Öneriler
1.  **Yerel Veritabanı Entegrasyonu (Room):** Çevrimdışı oynatma özelliğinin kararlılığı için çalma listeleri, beğenilen şarkılar ve cihazdaki ses dosyaları Room kütüphanesi yardımıyla yerel veritabanında saklanabilir.
2.  **Çevrimdışı Stream Caching:** ExoPlayer'ın `SimpleCache` mekanizması kullanılarak oynatılan şarkıların yerel belleğe önbelleğe alınması sağlanabilir, böylece aynı şarkı tekrar çalındığında ağ tüketimi önlenir.
3.  **Token Kalıcılığı (DataStore):** `AuthRepositoryImpl` içindeki JWT access token şu anda geçici olarak RAM üzerinde tutulmaktadır. Oturumun uygulama kapatılıp açıldığında devam etmesi için şifrelenmiş bir DataStore preferences alanı kullanılabilir.
