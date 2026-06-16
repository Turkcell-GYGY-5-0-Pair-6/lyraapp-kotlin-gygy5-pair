# LyraApp - Tema Sistemi

> Bu dosya LyraApp uygulamasının tema yönetimi, tema değişimi ve tercihlerin hafızada
> saklanması için **tek doğruluk kaynağıdır**.

---

## 1. Tema Desteği

- LyraApp iki tema modunu destekler:
  - `Dark` (varsayılan)
  - `Light`
- Tema seçimi uygulama düzeyinde yönetilir ve `PreferencesDataStore` içinde kalıcı hale getirilir.
- Mevcut uygulama mimarisi sistem temasıyla eşleme yerine kullanıcı tercihini saklamayı tercih eder.

---

## 2. Mimarisi

- `ThemePreferenceRepository` tema tercihini okur ve yazar.
- `MainActivity` uygulama açılırken `ThemePreferenceRepository.isDarkTheme` akışını toplar.
- Toplanan değer `LyraAppTheme(darkTheme = isDarkTheme)` üzerinden tüm Compose ağacına aktarılır.
- Tema değiştirme işlemi `ThemePreferenceRepository.setDarkTheme(...)` çağrısıyla yapılır.

---

## 3. Tema Tercihinin Saklanması

### 3.1. `ThemePreferenceRepository`

- `isDarkTheme: Flow<Boolean>`
  - `PreferencesDataStore` içindeki `dark_theme` anahtarından değeri okur.
  - Veri yoksa `true` (Dark tema) olarak başlar.
  - `IOException` durumunda `emptyPreferences()` emilerek uygulama çökmesi engellenir.
- `setDarkTheme(enabled: Boolean)`
  - Tema tercihinin `PreferencesDataStore` içinde `dark_theme` anahtarına kaydedilmesini sağlar.

### 3.2. `PreferencesDataStore`

- Kullanılan anahtar: `dark_theme`
- Depo adı: `theme_preferences`
- Veri tipi: `Boolean`
- Neden `DataStore`?
  - Modern, tip güveliği sağlanmış ve reaktif akış desteği sunar.
  - `SharedPreferences` yerine tercih edilmiştir çünkü daha güvenilir, hata yönetimi kolay ve Compose ile daha uyumludur.

---

## 4. Uygulama Başlatma ve Tema Akışı

- `MainActivity` içinde tema ayarı şu şekilde tüketilir:
  - `val isDarkTheme by themePreferenceRepository.isDarkTheme.collectAsState(initial = true)`
  - `LyraAppTheme(darkTheme = isDarkTheme) { ... }`
- Tema değişikliği uygulandığında Compose tekrar çizilir.
- `onToggleTheme` callback'i, `MainActivity` içinde `lifecycleScope.launch { themePreferenceRepository.setDarkTheme(!isDarkTheme) }` olarak uygulanır.

---

## 5. Tema Uygulama Kuralları

- Tema seçimi `LyraAppTheme` dışında tutulur; ekranlar sadece `MaterialTheme` üzerinden renklerini okur.
- UI bileşenleri `MaterialTheme.colorScheme` ve `MaterialTheme.typography` kullanmalıdır.
- Tema değişikliği sadece global akıştan (`ThemePreferenceRepository.isDarkTheme`) yönetilmelidir.
- Ekran düzeyinde `Context` veya `DataStore` erişimiyle tema yönetimi yapılmamalıdır.

---

## 6. Geliştirici Notları

- Yeni bir ekran tema değişimi tetikleyecekse, `LyraNavHost` veya ilgili screen route üzerinden `onToggleTheme` callback'i iletilir.
- Tema durumu `ViewModel` içinde saklanmamalıdır; ViewModel yalnızca ekran içi UI durumlarına odaklanmalıdır.
- Gelecekte sistem teması ile eşleme istenirse, `ThemePreferenceRepository` bir `ThemeMode` enum`una genişletilebilir ve `LyraAppTheme` `isSystemInDarkTheme()` ile birlikte kullanılabilir.
