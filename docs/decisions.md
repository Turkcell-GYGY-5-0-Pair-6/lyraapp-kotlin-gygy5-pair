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


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 09.06.2026

- Sürümler: Hilt **2.51.1**, KSP **2.0.21-1.0.28**.

- Compose'da ViewModel: `androidx.hilt.navigation.compose` (`hiltViewModel()`).
  Compose Navigation bağımlılığı `androidx.navigation:navigation-compose` olarak eklenmiştir.

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