# Emberbound

Emberbound, Java 21 ile geliştirilmiş, terminalde oynanan tamamlanmış bir sıra tabanlı hayatta kalma oyunudur. Mağara, orman ve nehirden yiyecek, odun ve su topla; güvenli eve dönüp işaret ateşini yak ve adadan kurtul.

Oyun kuralları terminal arayüzünden ayrıdır. Proje Java/backend portföyü için; durum yönetimi, doğrulama, hata senaryoları, test edilebilirlik ve tekrarlanabilir derlemeyi gösterir. Henüz web arayüzü veya HTTP API içermez.

## Çalıştırma

JDK 21 veya daha yeni bir JDK kurulu olmalı. `JAVA_HOME` değişkenini JDK klasörüne yönlendir. Maven Wrapper gerekli Maven sürümünü indirir; ilk derlemede internet gerekir.

Windows:

```powershell
.\play.cmd
```

macOS / Linux:

```sh
sh ./play.sh
```

Otomatik tam oyun demosu için komuta `--demo` ekle. Demo kayıt dosyalarını okumaz veya değiştirmez. Oyun arayüzü İngilizcedir.

## Nasıl oynanır?

Samuray, Okçu veya Şövalye seç. İlk olarak mağaraya git. Bir düşmanı yendikten sonra geri çekilip güvenli evde ücretsiz dinlenebilirsin. Samuray ile başlarsan başlangıçtaki 15 altınla hafif zırh almak güvenilir bir yoldur.

Savaşta `1` saldırır, `2` bandaj kullanır, `0` geri çekilir. Bandaj en fazla 10 can iyileştirir fakat düşman karşılık verir. Bölgedeki tüm düşmanları yenince o bölgenin malzemesini kazanırsın. Üç malzemeyle güvenli eve dönmek oyunu kazandırır.

Haritada `7` kaydeder, `0` kaydedip çıkar. Otomatik kayıt yoktur. Varsayılan kayıt `.saves/expedition.properties` dosyasıdır. `--save farklı/dosya.properties` ile ayrı bir kayıt yuvası kullanılabilir. Ölüm önceki kaydı silmez. Kazanılan oyunu sonuç ekranından da kaydedebilirsin.

## Doğrulama ve portföy

```powershell
.\mvnw.cmd verify
```

Bu komut testleri, biçim kontrolünü, JAR üretimini ve en az %80 satır kapsamı şartını çalıştırır. macOS/Linux için `sh ./mvnw verify` kullan. Kapsam raporu `target/site/jacoco/index.html` konumundadır.

- [Oyun kuralları](GAMEPLAY.md)
- [Mimari, teknik kararlar ve gelecekteki frontend](ARCHITECTURE.md)
- [Projeyi kariyer portföyünde sunma notları](PORTFOLIO.md)
- [Ana README](../README.md)
