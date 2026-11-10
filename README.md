# 📦 Order Management System

Küçük işletmelerin ürünlerini, stoklarını ve siparişlerini tek bir yerden yönetebilmesi; müşterilerin de farklı şirketlerden alışveriş yapıp siparişlerini adım adım takip edebilmesi için geliştirilmiş bir web uygulaması.

Sistemde üç tür kullanıcı vardır:

- **İşletme sahibi (Owner):** Şirketinin ürünlerini ekler ve düzenler, stoklarını takip eder, gelen siparişleri onaylayıp kargoya kadar ilerletir, müşterilerini ve satış raporlarını görür.
- **Müşteri (Customer):** Farklı şirketlerin ürünlerini inceler, sepetine ekler, satın alır ve siparişini *Onay bekliyor → Hazırlanıyor → Kargoda → Teslim edildi* adımlarıyla takip eder.
- **Yönetici (Admin):** Kullanıcıların rollerini değiştirir, hesapları aktif/pasif yapar.

Her kullanıcı giriş yaptığında rolüne özel bir ana sayfa ve menü görür.

<!-- Ekran görüntüleri: docs/ klasörüne ekleyip aşağıdaki satırları açın -->
<!--
| Owner paneli | Sipariş takibi | Raporlar |
|---|---|---|
| ![Owner paneli](docs/owner-dashboard.png) | ![Sipariş takibi](docs/order-tracking.png) | ![Raporlar](docs/reports.png) |
-->

---

## 🚀 Nasıl Çalışır?

```
Kayıt ol (Owner / Customer)  →  Giriş yap  →  Rolüne özel ana sayfa
                                                 │
            ┌────────────────────────────────────┼─────────────────────────────┐
          Owner                               Customer                        Admin
 • Özet panel                         • Ürünleri şirketlere göre gör   • Kullanıcı listesi
 • Ürün ekle / düzenle                • Sepete ekle, adet değiştir     • Rol değiştir
 • Siparişleri onayla, ilerlet        • Satın al                       • Hesabı aktif/pasif yap
 • Müşteriler                         • Sipariş takibi, iptal,
 • Satış, sipariş ve stok raporları     teslim aldım
                                      • Harcama raporları
```

### Sipariş akışı

```
 Customer satın alır          Owner onaylar           Owner kargoya verir       Customer "teslim aldım" der
        │                          │                          │                          │
        ▼                          ▼                          ▼                          ▼
   ┌─────────┐               ┌───────────┐              ┌──────────┐              ┌───────────┐
   │ PENDING │ ────────────▶ │ PREPARING │ ───────────▶ │ SHIPPED  │ ───────────▶ │ DELIVERED │
   └─────────┘               └───────────┘              └──────────┘              └───────────┘
        │                          │                                                     │
        ▼                          ▼                                                     ▼
   CANCELLED  (customer veya owner iptal eder)                                RETURNED (iade)
```

- Satın alındığında ürünler **stoktan düşülür**; sipariş **iptal** veya **iade** edilirse stok **geri eklenir**.
- Satın alma tek bir transaction içinde çalışır: sepetteki herhangi bir üründe stok yetmezse sipariş oluşturulmaz ve hiçbir stok değişmez.
- Aynı anda gelen siparişler stoğu eksiye düşüremez (satır kilitleme / pessimistic lock).
- Customer sadece **onay bekleyen** siparişini iptal edebilir; owner kargoya verilene kadar iptal edebilir.
- Customer, My Orders sayfasında her siparişin altında **takip çubuğu** görür:
  `Order Placed → Preparing → Shipping → Delivered`.
- Bir siparişte birden fazla şirketin ürünü olabilir; her owner sipariş listesinde yalnızca **kendi ürünlerini ve tutarını** görür (bkz. [Bilinen Kısıtlar](#️-bilinen-kısıtlar)).
- Durum geçiş kuralları tek bir yerde, `OrderStatus` enum'unda tanımlıdır.

---

## ✨ Özellikler

| | Owner | Customer | Admin |
|---|:---:|:---:|:---:|
| Kayıt / giriş / çıkış | ✅ | ✅ | ✅ |
| Rolüne özel ana sayfa | ✅ | ✅ | ✅ |
| Özet istatistik paneli (şirkete özel) | ✅ | — | — |
| Ürün ekleme ve düzenleme | ✅ | — | — |
| Kategori seçimi (hiyerarşik, enum) | ✅ | — | — |
| Düşük stok uyarısı | ✅ | — | — |
| Ürünleri listeleme | Kendi şirketi | Tüm şirketler (gruplu) | — |
| Sepet (ekle, artır, azalt, çıkar, satın al) | — | ✅ | — |
| Siparişler | Şirketinin siparişleri, durum filtresi | Kendi siparişleri | — |
| Sipariş durumunu güncelleme | ✅ | Teslim aldım | — |
| Sipariş iptali (stok geri eklenir) | ✅ | Onay beklerken | — |
| Sipariş detayı | ✅ | ✅ | — |
| Sipariş takip çubuğu | — | ✅ | — |
| Müşteriler sayfası | ✅ | — | — |
| Raporlar | Satış, sipariş, stok | Harcama | — |
| Kullanıcı rol / aktiflik yönetimi | — | — | ✅ |

### 📊 Raporlar (`/reports`)

**Owner — Satış raporu**
- Toplam satış, sipariş sayısı, ortalama sipariş tutarı, satılan ürün adedi
- Onay bekleyen siparişler, açık sipariş tutarı, teslim edilmiş satış, iptal/iade oranı
- Son 6 ayın satış grafiği, duruma ve kategoriye göre dağılım
- En çok satan ürünler, en çok alışveriş yapan müşteriler

**Owner — Sipariş istatistikleri**
- Bugün / son 7 gün / son 30 gün (önceki dönemle karşılaştırmalı ▲▼)
- Son 14 günün günlük sipariş grafiği, haftanın günlerine göre dağılım
- En yoğun gün ve saat, sipariş başına ürün, en büyük sipariş, tekrar eden ve yeni müşteriler

**Owner — Stok raporu**
- Ürün sayısı, toplam stok, stok değeri, stokta olmayan ürünler
- Düşük stoklu ürünler ve hiç satılmamış ürünler

**Customer — Harcama raporu**
- Toplam harcama, sipariş sayısı, ortalama sipariş, alınan ürün adedi
- Son 6 ayın harcama grafiği, sipariş durumları, şirketlere göre harcama, en çok alınan ürünler

> Tutarlara iptal ve iade edilen siparişler dahil edilmez.

### 🔒 Güvenlik
- Spring Security ile form tabanlı giriş, CSRF koruması
- Şifreler BCrypt ile hash'lenerek saklanır
- Sayfa ve işlem bazında rol kontrolü (ör. ürün ekleme/düzenleme ve sipariş durumu yalnızca Owner'a, `/admin` yalnızca Admin'e açık)
- Kayıt formundan yalnızca Owner veya Customer rolü seçilebilir; Admin rolü sadece admin panelinden verilir
- Owner yalnızca kendi şirketinin ürünlerini ve siparişlerini görebilir ve değiştirebilir; customer yalnızca kendi sepetine ve siparişlerine işlem yapabilir
- Pasif yapılan hesaplar giriş yapamaz
- Veritabanı şifresi ve admin bilgileri kodda değil, yerel ayar dosyasında veya ortam değişkenlerinde tutulur

---

## 🛠️ Teknolojiler

- **Java 17**, **Spring Boot 4.1**
- **Spring Web MVC** + **Thymeleaf**: sunucu tarafında oluşturulan sayfalar
- **Spring Security**: form tabanlı giriş, rol bazlı yetkilendirme, BCrypt
- **Spring Data JPA / Hibernate**: veritabanı erişimi, satır kilitleme (pessimistic lock)
- **MySQL 8**: veritabanı
- **Bean Validation**, **Lombok**, **Maven**

---

## ⚙️ Kurulum

**Gereksinimler:** JDK 17+, Maven, MySQL 8+

**1. Projeyi klonlayın**

```bash
git clone https://github.com/Gulfem93/OrderManagementSystem.git
cd OrderManagementSystem
```

**2. Veritabanını oluşturun**

```sql
CREATE DATABASE ordermanagementsystem;
```

Tablolar uygulama ilk açıldığında otomatik oluşturulur (`ddl-auto: update`).

**3. Bağlantı bilgilerini girin**

Örnek ayar dosyasını kopyalayın ve kendi MySQL bilgilerinizi yazın. `application-dev.yml` şifre içerdiği için `.gitignore`'dadır ve GitHub'a gönderilmez.

```bash
cp src/main/resources/application-dev.example.yml src/main/resources/application-dev.yml
```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ordermanagementsystem
    username: <kullanici_adi>
    password: <sifre>
  jpa:
    hibernate:
      ddl-auto: update

app:
  seed:
    enabled: false
```

**4. Uygulamayı çalıştırın**

```bash
mvn spring-boot:run
```

Tarayıcıda **http://localhost:8080** adresine gidin, bir hesap oluşturun ve giriş yapın.

### 👤 Admin hesabı

İki yol vardır:

**a) Açılışta admin hesabı oluşturma.** `app.seed.enabled: true` iken uygulama açılışta bir admin hesabı oluşturur (hesap zaten varsa dokunmaz). E-posta ve şifre ortam değişkenlerinden okunur:

```bash
ADMIN_SEED_EMAIL=admin@ornek.com ADMIN_SEED_PASSWORD=guclu-bir-sifre mvn spring-boot:run
```

Canlı ortamda `app.seed.enabled` değerini `false` yapın.

**b) Mevcut bir kullanıcıyı admin yapma.** Kayıtlı bir kullanıcının e-postasını ortam değişkeniyle verin (birden fazla adres virgülle ayrılır):

```bash
ADMIN_EMAILS=ornek@mail.com mvn spring-boot:run
```

---

## 🗂️ Proje Yapısı

Proje katmanlı mimariyle düzenlenmiştir: istek **controller**'a gelir, iş mantığı **service**'te çalışır, veritabanı işlemleri **repository** üzerinden yapılır.

```
src/main/java/com/ordermanagementsystem
├── dev
│   ├── config/       → Güvenlik, şifreleme, admin hesabı oluşturma
│   ├── controller/   → Sayfa istekleri (Home, Login, Signup, Product, Cart,
│   │                   Orders, Customers, Reports, Admin)
│   ├── service/      → İş mantığı (sepet, sipariş, stok, raporlar...)
│   ├── repository/   → Veritabanı erişimi
│   ├── model/        → Veritabanı tabloları (Entity)
│   └── data/         → Formlardan gelen ve sayfalara giden veriler (DTO)
└── enums
    ├── OrderStatus   → Sipariş durumları ve izin verilen geçişler
    ├── CategoryType  → Hiyerarşik ürün kategorileri
    └── UserRole      → Kullanıcı rolleri

src/main/resources
├── templates/        → HTML sayfaları (Thymeleaf)
├── static/css/       → Stil dosyaları
├── application.yml
└── application-dev.example.yml → Yerel ayar şablonu
```

**Veritabanı tabloları:** `users`, `customers`, `products`, `orders`, `orderitem`, `cart_item`

### Sayfalar

| Adres | Kim görür | Açıklama |
|---|---|---|
| `/signup`, `/login` | Herkes | Kayıt ve giriş |
| `/` | Giriş yapan herkes | Rolüne özel ana sayfa |
| `/products` | Owner / Customer | Ürün yönetimi / alışveriş |
| `/orders` | Owner / Customer | Siparişler / sepet ve sipariş geçmişi |
| `/customers` | Owner | Şirketten alışveriş yapan müşteriler |
| `/reports` | Owner / Customer | Satış, sipariş ve stok raporu / harcama raporu |
| `/admin` | Admin | Kullanıcı yönetimi |

---

## ⚠️ Bilinen Kısıtlar

- Bir siparişte birden fazla şirketin ürünü olabilir, ancak siparişin durumu tektir: bir owner durumu değiştirdiğinde veya siparişi iptal ettiğinde siparişin tamamı etkilenir. Şirket başına ayrı durum takibi yol haritasındadır.
- Şirketler ayrı bir tablo değil, owner kaydındaki şirket adıyla eşleştirilir; aynı adla iki şirket açılmamalıdır.
- Otomatik testler henüz kapsamlı değildir.

---

## 🧭 Yol Haritası

- [x] Sepetteki ürünlerden sipariş oluşturma, stoktan düşme
- [x] Sipariş durumunu güncelleme ve müşteri tarafında takip
- [x] Sipariş iptali ve iade (stok geri ekleme)
- [x] Müşteriler sayfası (`/customers`)
- [x] Raporlar sayfası (`/reports`)
- [x] Ürün düzenleme ve kategori seçimi
- [ ] Çok şirketli siparişlerde şirket başına ayrı durum takibi
- [ ] Servis katmanı için birim testleri (sepet, sipariş, stok)
- [ ] Şirketleri ayrı bir tabloya taşıma
- [ ] Ürün silme
- [ ] Profil / hesap bilgileri sayfası (`/profile`)
- [ ] Ürün arama ve kategoriye göre filtreleme