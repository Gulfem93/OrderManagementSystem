# 📦 Order Management System

Küçük işletmelerin ürünlerini ve siparişlerini tek bir yerden yönetebilmesi için geliştirilmiş bir web uygulaması.

Sistemde iki tür kullanıcı vardır:

- **İşletme sahibi (Owner):** Şirketinin ürünlerini ekler, stoklarını takip eder ve gelen siparişleri görür.
- **Müşteri (Customer):** Farklı şirketlerin ürünlerini inceler, sepetine ekler ve siparişlerini takip eder.

Her kullanıcı giriş yaptığında, rolüne göre kendisine özel bir ana sayfa ve menü görür.

---

## 🚀 Nasıl Çalışır?

```
Kayıt ol (Owner / Customer)  →  Giriş yap  →  Rolüne özel ana sayfa
                                                 │
                 ┌───────────────────────────────┴───────────────────────────────┐
              Owner                                                          Customer
   • Özet panel (sipariş, bekleyen sipariş,                    • Ürünleri şirketlere göre listele
     düşük stok, müşteri sayısı)                               • Sepete ekle
   • Ürün ekle / kendi ürünlerini listele                      • Sepette adet artır, azalt, çıkar
   • Tüm siparişleri görüntüle                                 • Kendi siparişlerini görüntüle
```

1. Kullanıcı `/signup` sayfasından rolünü seçerek kayıt olur. Owner olarak kayıt olanlar şirket adını da girer.
2. `/login` sayfasından e-posta ve şifreyle giriş yapılır.
3. **Owner**, ürün eklerken ürünü otomatik olarak kendi şirketine bağlar. Stoğu 5 ve altına düşen ürünler uyarı olarak gösterilir.
4. **Customer**, ürünler sayfasında beğendiği ürünü sepete ekler; sepetini *Siparişlerim* sayfasından yönetir.

---

## ✨ Özellikler

| | Owner | Customer |
|---|:---:|:---:|
| Kayıt / giriş / çıkış | ✅ | ✅ |
| Rolüne özel ana sayfa | ✅ | ✅ |
| Özet istatistik paneli | ✅ | — |
| Ürün ekleme (doğrulamalı form) | ✅ | — |
| Düşük stok uyarısı | ✅ | — |
| Ürünleri listeleme | Kendi şirketi | Tüm şirketler (gruplu) |
| Sepet yönetimi | — | ✅ |
| Siparişleri görüntüleme | Tüm siparişler | Kendi siparişleri |

**Güvenlik:** Spring Security ile form tabanlı giriş, şifrelerin hash'lenerek saklanması ve sayfa/işlem bazında rol kontrolü (ör. ürün ekleme yalnızca Owner'a açık).

---

## 🛠️ Teknolojiler

- **Java 17**, **Spring Boot 4**
- **Spring Web MVC** + **Thymeleaf** — sunucu tarafında oluşturulan sayfalar
- **Spring Security** — kimlik doğrulama ve yetkilendirme
- **Spring Data JPA / Hibernate** — veritabanı erişimi
- **MySQL** — veritabanı
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

Tablolar uygulama ilk açıldığında otomatik oluşturulur.

**3. Bağlantı bilgilerini girin** — `src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ordermanagementsystem
    username: <kullanici_adi>
    password: <sifre>
```

**4. Uygulamayı çalıştırın**

```bash
mvn spring-boot:run
```

Tarayıcıda **http://localhost:8080** adresine gidin, bir hesap oluşturun ve giriş yapın.

---

## 🗂️ Proje Yapısı

Proje katmanlı mimariyle düzenlenmiştir: istek **controller**'a gelir, iş mantığı **service**'te çalışır, veritabanı işlemleri **repository** üzerinden yapılır.

```
src/main/java/com/ordermanagementsystem/dev
├── config/       → Güvenlik ve şifreleme ayarları
├── controller/   → Sayfa istekleri (Home, Login, Signup, Product, Orders, Cart)
├── service/      → İş mantığı
├── repository/   → Veritabanı erişimi
├── model/        → Veritabanı tabloları (Entity)
└── data/         → Formlardan gelen veriler (DTO)

src/main/resources
├── templates/    → HTML sayfaları (Thymeleaf)
└── static/css/   → Stil dosyaları
```

**Veritabanı tabloları:** `users`, `customers`, `products`, `orders`, `orderitem`, `cart_item`

---

## 🧭 Yol Haritası

Projenin geliştirilmesi devam ediyor. Planlanan özellikler:

- [ ] Sepetteki ürünlerden sipariş oluşturma
- [ ] Owner için sipariş durumunu güncelleme (Bekliyor → Hazırlanıyor → Teslim edildi)
- [ ] Müşteriler sayfası (`/customers`)
- [ ] Raporlar sayfası (`/reports`)
- [ ] Profil / hesap bilgileri sayfası (`/profile`)
- [ ] Ürün düzenleme ve silme
