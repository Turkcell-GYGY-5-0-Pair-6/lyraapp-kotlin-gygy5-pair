# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi

- Seçim*: **Hilt**

- Son Güncelleme Tarihi*: 04.06.2026

- Alternatifler: **Koin**

- Sebep: **Opsiyonel**


### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 04.06.2026


### Compose UI / Material

- Seçim: **Compose BOM + Material3**

- Son Güncelleme Tarihi: 10.06.2026

- Kapsam: `androidx.compose:compose-bom`, `androidx.compose.ui:ui`, `androidx.compose.ui:ui-graphics`, `androidx.compose.ui:ui-tooling-preview`, `androidx.compose.material3:material3`

- Sebep: Compose bileşen sürümlerini BOM ile senkronize etmek ve modern Material3 tasarım kütüphanesini kullanmak.


### Tema Yönetimi

- Seçim: **Compose Dark/Light Tema + DataStore Preferences**

- Son Güncelleme Tarihi: 16.06.2026

- Kapsam: `LyraAppTheme`, `ThemePreferenceRepository`, `MainActivity` uygulama başlatma süreci

- Sebep: Kullanıcı temayı kalıcı olarak saklamak, tema seçimini uygulama düzeyinde yönetmek ve Material3 tema paletini tek bir kaynaktan sağlamak.


### Kotlin Coroutines

- Seçim: **kotlinx.coroutines.android**

- Son Güncelleme Tarihi: 10.06.2026

- Sebep: Android ana iş parçacığında asenkron IO ve veri akışı yönetimi için coroutine tabanlı çalışma zamanı.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 10.06.2026

- Sürümler: Hilt **2.51.1**, KSP **2.0.21-1.0.28**.

- Compose'da ViewModel: `androidx.hilt.navigation.compose` **1.2.0** (`hiltViewModel()`).
- Compose Navigation bağımlılığı: `androidx.navigation:navigation-compose` **2.8.5**.

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.0 tabanlı AGP 8 ile uyumludur.


### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: AGP 9 built-in Kotlin ile KSP kullanıldığında `gradle.properties` içinde
  **`android.disallowKotlinSourceSets=false`** gerekebilir.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak
  olmadan derlemeyi kırabilir. Bayrak deneysel (experimental) olarak işaretlidir.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API
  geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.


### Şifresiz OTP Giriş/Kayıt Katmanı Entegrasyonu

- Karar: **AuthRepositoryImpl** ile gerçek API entegrasyonu ve geçici olarak DataStore yerine bellekte token saklama.

- Son Güncelleme Tarihi: 20.06.2026

- Sebep: API spesifikasyonu OTP flow olarak güncellendi ve backend hazır duruma getirildi. SMS doğrulama adımı için sahte SMS altyapısı bulunsa da API tamamen gerçek çalışmaktadır. Kullanıcı bilgileri `/me/update-informations` uç noktasına doğrulanan JWT access token ile gönderilir. Token bellekte singleton repository düzeyinde saklanır.


### Önerilen Şarkılar API Entegrasyonu ve Arayüz Güncellemesi

- Karar: Ana sayfadaki şarkı listesinin `/api/v1/me/recommendations` adresinden çekilmesi ve grid şeklinde listelenmesi.

- Son Güncelleme Tarihi: 20.06.2026

- Sebep: Kullanıcıya özel önerilen şarkıları listelemek ve tasarımı daha zengin, modern bir grid yapısına dönüştürmek.


### Son Çalınanlar API Entegrasyonu

- Karar: Ana sayfadaki son çalınanlar listesinin `/api/v1/me/recently-played` adresinden çekilmesi ve tıklandığında ilgili şarkının oynatılması.

- Son Güncelleme Tarihi: 20.06.2026

- Sebep: Son çalınanları statik liste yerine kullanıcının gerçek geçmişinden dinamik olarak çekmek ve oynatılabilmesini sağlamak.


### Şarkı Dinleme Geçmişinin Kaydedilmesi Entegrasyonu

- Karar: Şarkı oynatma başlatıldığında (`DefaultPlayerRepository.getPlaybackState`) asenkron olarak arka planda `POST /api/v1/me/plays` endpoint'inin çağrılması.

- Son Güncelleme Tarihi: 20.06.2026

- Sebep: Kullanıcının dinleme geçmişini sunucu tarafında kaydetmek ve "Son Çalınanlar" ile "Öneriler" listelerini dinamik olarak besleyebilmek.


### Son Çalınanlar Görünürlüğü ve Otomatik Yenileme Entegrasyonu

- Karar: Ana sayfadaki son çalınanlar listesi boş olduğunda alanın gizlenmesi ve ana sayfaya her geri dönüldüğünde (RESUMED) beslemenin otomatik yenilenmesi.

- Son Güncelleme Tarihi: 20.06.2026

- Sebep: Listelenecek eleman olmadığında arayüzde boş alan kalmamasını sağlamak ve şarkı oynatılıp geri dönüldüğünde dinleme geçmişini anlık yansıtmak.