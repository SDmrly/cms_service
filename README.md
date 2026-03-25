# Multi-Language CMS Service

Bu proje, Spring Boot tabanlı, PostgreSQL ve Redis kullanan çoklu dil destekli bir İçerik Yönetim Sistemi (CMS) servisidir. Bu servis ile projeler oluşturabilir, bu projelere ait CMS anahtarları ekleyebilir ve çevirileri yönetebilirsiniz.

## Teknolojiler

- **Java 21**
- **Spring Boot 3.3.5**
- **PostgreSQL** (Kalıcı veri yönetimi)
- **Redis** (Geçici veriler ve önbellekleme)
- **Flyway** (Veritabanı versiyonlama - İstendiğinde etkinleştirilebilir)
- **MapStruct & Lombok**
- **Gradle 9.4.1**
- **Docker & Docker Compose**

## Hızlı Başlangıç

### 1. Veritabanlarını Başlatma
Projede PostgreSQL ve Redis kullanıldığı için bunları en kolay şekilde Docker ile başlatabilirsiniz:
```bash
docker-compose up -d
```
Bu komut veritabanlarını arka planda başlatacaktır.

### 2. Uygulamayı Derleme ve Çalıştırma
Veritabanları ayağa kalktıktan sonra Gradle ile uygulamayı doğrudan çalıştırabilirsiniz:
```bash
./gradlew bootRun
```
*Not: Veritabanı tabloları `application.yml`'de bulunan `ddl-auto: update` ayarı ile otomatik olarak oluşturulacaktır.*

## API Endpoints (Özet)

Uygulamanın Swagger arayüzüne (API dokümantasyonuna) çalışırken şu adresten ulaşabilirsiniz:
**http://localhost:8085/swagger-ui.html**

Başlıca endpoint'ler:

### Proje Yönetimi (`/api/v1/projects`)
- `POST /api/v1/projects`: Yeni proje oluşturur.
- `GET /api/v1/projects`: Aktif projeleri listeler.

### CMS Anahtarları (`/api/v1`)
- `POST /api/v1/projects/{projectId}/keys`: Projeye yeni bir anahtar (key) ekler.
- `GET /api/v1/projects/{projectId}/keys`: Projedeki anahtarları listeler.

### Çeviri ve Import Yönetimi (`/api/v1`)
- `POST /api/v1/keys/{keyId}/translations`: Anahtar için yeni çeviri ekler veya günceller.
- `POST /api/v1/projects/{projectId}/translations/bulk`: Toplu çeviri içeri alır.
- `POST /api/v1/projects/{projectId}/import`: `.properties` dosyasından çevirileri içeri (.import) aktarır.

### Content Lookup (`/api/v1/lookup`)
- `GET /api/v1/lookup/{projectCode}`: Projenin tüm lokalize anahtarlarını JSON formatında getirir.

## Proje Yapısı

- `com.cms.domain.entity`: Veritabanı tablolarını temsil eden sınıflar (`Project`, `CmsKey`, `Translation`).
- `com.cms.repository`: PostgreSQL ile iletişim kuran JPA Repository katmanı.
- `com.cms.service`: İş kurallarının yer aldığı katman.
- `com.cms.controller`: REST API uç noktaları.
- `com.cms.dto`: Veri transfer objeleri.

## Dışa / İçe Aktarım Yapısı (Import)
`PropertiesImportService` sayesinde `.properties` uzantılı (Örn: `messages_en.properties`) dosyaları direkt Multipart Form Data ile `/api/v1/projects/{projectId}/import` adresine yükleyebilirsiniz. Servis otomatik olarak eksik anahtarları oluşturacak ve çevirileri kaydedecektir.
