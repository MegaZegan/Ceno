# Ceno

Pastel kırtasiye estetiğinde, tablet öncelikli kişisel günlük, çizim arkadaşı ve minik pet uygulaması.

## Teknoloji

- Kotlin 2.2.21
- Jetpack Compose 1.10 / Material 3 altyapısı
- Room 2.8.4
- minSdk 26, targetSdk 35, compileSdk 36
- Tamamen lokal ve offline veri

## Çalıştırma

Android Studio ile kök klasörü açın veya:

```powershell
.\gradlew.bat :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Yapı

- `ui/theme`: merkezi pastel renk ve tipografi sistemi
- `ui/components`: CuteCard, PaperNote, Mascot, Tape ve tekrar kullanılabilir parçalar
- `ui/screens`: Ana Sayfa, Günlük, Minik Dostum, Referanslar, MOA Köşesi, Mektuplar, Merch Çantam ve Ayarlar
- `data/database`: Room diary veritabanı
- `data/repository`: lokal ayarlar ve veri erişimi
- `model`: ekran, mood ve içerik modelleri

Maskotun değiştirilebilir asset girişleri `res/drawable/mascot_idle`, `mascot_happy`, `mascot_sleep` ve `mascot_birthday` adlarıyla hazırdır. Arayüzdeki mevcut maskot telifsiz, özgün bir Compose Canvas çizimidir.

## Yerel veri

Günlük girişleri Room'da; tema, 03-09 varsayılan doğum günü, MOA tercihleri, Doggy verileri ve referans rafı bilgileri SharedPreferences'ta tutulur. Seçilen görseller kalıcı Android içerik URI izinleriyle cihazda kalır. Ayarlardaki JSON yedekleme/geri yükleme günlük girdilerini ve temel ayarları kapsar. Ana sayfadaki fotoğraf kartı, kullanıcının seçtiği galeri havuzundan rastgele bir anı gösterir.

Doggy'nin `idle`, `happy`, `surprised` ve `angry` WebM animasyonları `res/raw` altında tutulur ve doğrudan döngülü video olarak oynatılır.

03-09 tarihinde açılan doğum günü mektubu, kullanıcı tarafından sağlanan özel TXT çizimini ve Türkçe kutlama mesajını scrapbook kağıt düzeninde gösterir.

Ana sayfadaki “birlikte geçen zaman” kartı, Android'in ilk kurulum zamanını kullanır. Uygulama güncellendiğinde sayaç sıfırlanmaz.

Referans Rafı, kullanıcı görsellerini öne alırken boş klasörlerde özgün çizimli başlangıç ilhamları gösterir. MOA Köşesi; çalışan favoriler, şarkılar, üyeler, sürüklenebilir dönem sırası ve koleksiyon sekmelerini yoğun tablet yerleşiminde bir araya getirir. Üye portreleri ve albüm/şarkı kapakları kullanıcı tarafından sağlanan yerel görsellerden yüklenir; tüm MOA seçimleri cihazda kalıcı saklanır.

“Merch çantam” ekranı, verilen PNG çantayı değiştirmeden gösterir ve görselin kendi kutularına hizalanmış 4×4 interaktif katman içerir. Photo Picker ile seçilen görseller uygulamanın özel depolama alanına kopyalanır; her görsel 1–4 sütun ve 1–4 satır kaplayacak biçimde ayarlanabilir ve bu düzen kalıcı saklanır.
