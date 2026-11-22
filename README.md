# GoldWatch — Gold Price Tracker

[![CI](https://github.com/<your-github-username>/gold-price-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/<your-github-username>/gold-price-tracker/actions/workflows/ci.yml)

เว็บติดตาม **ราคาทองคำไทย** (ทองคำแท่ง / ทองรูปพรรณ 96.5%) และ **ราคาทองโลก** (XAU/USD) พร้อมค่าเงินบาท
แบบใกล้เรียลไทม์ มี dashboard และกราฟย้อนหลังตั้งแต่ 1 วันถึง 1 ปี เปิดดูได้ทันทีโดยไม่ต้องสมัครสมาชิก

> โปรเจกต์ฝึกงาน — Spring Boot + MySQL + Next.js

## ปัญหาที่แก้

ราคาทองไทยประกาศหลายรอบต่อวัน และกระจายอยู่หลายเว็บ ราคาทองโลกกับค่าเงินบาทก็อยู่คนละที่
GoldWatch เก็บราคาจากแหล่งข้อมูลอัตโนมัติทุก 5 นาที เก็บประวัติลงฐานข้อมูล และแสดงทุกอย่างในหน้าเดียว

## ฟีเจอร์

**ข้อมูล**

- ราคาล่าสุดของทองคำแท่ง ทองรูปพรรณ ทองโลก และ USD/THB พร้อมการเปลี่ยนแปลงเทียบราคาปิดเมื่อวาน
- กราฟย้อนหลัง 1 วัน (รายชั่วโมง), 1 สัปดาห์ / 1 เดือน (รายวัน), 1 ปี (รายสัปดาห์) พร้อมสูงสุด / ต่ำสุด / เปลี่ยนแปลง
- ประวัติรอบประกาศของวัน พร้อมการเปลี่ยนแปลงต่อรอบ
- หน้าเว็บดึงข้อมูลใหม่เองทุก 60 วินาที (ไม่ต้องรีเฟรช) และแสดงวันเวลาที่ได้ข้อมูลล่าสุดบน navbar
- ถ้า API ล่ม ยังแสดงราคาล่าสุดที่มีพร้อมข้อความแจ้งเตือน แล้วลองใหม่อัตโนมัติ

**หน้าตาและการใช้งาน**

- ธีม luxury สองแบบ: **มืด** (ดำ–ทอง) และ **สว่าง** (งาช้าง–ทอง) สลับได้ที่ปุ่ม ☀/☾ บน navbar
  ครั้งแรกจะตามการตั้งค่าของเครื่อง และจำค่าที่ผู้ใช้เลือกไว้
- ภาพประกอบทองบนการ์ดราคา (ชุดภาพแยกสำหรับแต่ละธีม), โลโก้นาฬิกาทรายที่ทรายไหลและพลิกกลับเอง
- ลูกเล่น: ตัวเลขราคานับขึ้นตอนเลื่อนมาเห็น, การ์ดเอียงตามเมาส์พร้อมแสงทอง, ปุ่มแม่เหล็ก,
  ปุ่มกลับขึ้นด้านบนที่มีวงแหวนบอกระยะการอ่าน, แถบ navbar ชี้ส่วนที่กำลังอ่าน
- Responsive ทั้งคอมและมือถือ (ทดสอบตั้งแต่ 320px)
- Accessibility: ขึ้น/ลงแสดงด้วย ▲/▼ และ +/− ไม่ใช่สีอย่างเดียว, มี skip link, ภาพตกแต่งซ่อนจาก screen reader,
  และปิดแอนิเมชันทั้งหมดเมื่อเครื่องตั้งค่า "ลดการเคลื่อนไหว" (prefers-reduced-motion)

## สถาปัตยกรรม

```mermaid
flowchart LR
    subgraph Sources[แหล่งข้อมูลภายนอก]
        GTA[goldtraders.or.th<br/>HTML]
        GAPI[gold-api.com<br/>JSON]
        ER[open.er-api.com<br/>JSON]
    end
    subgraph Backend[Spring Boot]
        SCH[PriceScheduler<br/>ทุก 5 นาที] --> SRC[PriceSource<br/>parsers + validation]
        SRC --> ING[PriceIngestionService<br/>บันทึกเมื่อราคาเปลี่ยน]
        API[REST API<br/>/api/prices/*]
    end
    DB[(MySQL 8<br/>Flyway)]
    WEB[Next.js<br/>GoldWatch dashboard]

    GTA & GAPI & ER --> SRC
    ING --> DB
    DB -->|GROUP BY + DATE_FORMAT| API
    API -->|JSON ทุก 60 วินาที| WEB
```

**Scheduler → MySQL → REST API → Next.js**

### Backend (`backend/src/main/java/com/goldwatch/goldtracker/`)

- `source/` — `PriceSource<T>` interface; แต่ละแหล่งข้อมูลเป็น implementation แยก (สลับ/เพิ่มแหล่งได้ที่จุดเดียว)
- `scheduler/` — ดึงแต่ละแหล่งแยกกัน แหล่งหนึ่งล้มเหลว แหล่งอื่นยังทำงานต่อ และไม่มีข้อมูลผิดลง DB
- `service/` — บันทึกเฉพาะเมื่อรอบประกาศ/ราคาเปลี่ยน, query สำหรับ API
- `repository/` — JPA + SQL รวมข้อมูลเป็นช่วงเวลาใน MySQL (ไม่ดึงทั้งตารางมาประมวลผลในหน่วยความจำ)
- `api/` — REST controllers, DTO, error JSON (RFC 9457)

ราคาเก็บเป็น `DECIMAL` / `BigDecimal` เสมอ, เวลาเก็บเป็น UTC และแปลงเป็นเวลาไทยตอนแสดงผล

### Frontend (`frontend/src/`)

```text
app/
  layout.tsx          ฟอนต์ (next/font) + สคริปต์ตั้งธีมก่อนหน้าเว็บแสดง (ไม่กะพริบ)
  globals.css         design tokens ทั้งหมด: สี/ระยะ/ฟอนต์/motion ของธีมมืด และ override ของธีมสว่าง
  icon.svg            ไอคอนแท็บเบราว์เซอร์ (นาฬิกาทราย)
components/
  Dashboard.tsx       ประกอบทั้งหน้า + ดึงข้อมูลทุก 60 วินาที
  Navbar, ThemeToggle, Hero, LatestPrices + PriceCard, PriceHistoryChart, AnnouncementTable, Footer
  GoldWatchMark       โลโก้ SVG พร้อมแอนิเมชัน
  AnimatedValue       ตัวเลขที่นับขึ้น / เลื่อนเข้าเมื่อค่าเปลี่ยน
  Reveal              ค่อย ๆ แสดงเนื้อหาเมื่อเลื่อนมาถึง (IntersectionObserver)
  Interactions        เอฟเฟกต์ตามเมาส์ทั้งหน้า (การ์ดเอียง, ปุ่มแม่เหล็ก) ด้วย event listener ตัวเดียว
  BackToTop, ChangeIndicator, SectionHeader, Icons
hooks/usePolling.ts   เรียก API ซ้ำตามรอบ เก็บข้อมูลล่าสุดไว้ถ้าครั้งถัดไปล้มเหลว
lib/
  api.ts              type ของ API + ฟังก์ชันเรียก
  format.ts           จัดรูปแบบตัวเลข / วันเวลาไทย (พ.ศ., Asia/Bangkok)
  change.ts           ลบเลขทศนิยมแบบแม่นยำ (BigInt) และทิศทางขึ้น/ลง
  theme.ts            key ใน localStorage + สคริปต์ตั้งธีม
assets/prices/        ภาพบนการ์ดราคา (+ CREDITS.md แหล่งที่มาและสัญญาอนุญาต)
```

- สีทุกสีมาจาก CSS variables ใน `globals.css` — เพิ่ม/แก้ธีมได้ที่ไฟล์เดียว คอมโพเนนต์ไม่ต้องรู้ว่าเป็นธีมไหน
- สไตล์ใช้ CSS Modules ต่อคอมโพเนนต์, ไม่มี UI library เพิ่ม (dependency มีแค่ Next, React, Recharts)

## Tech stack

| ส่วน | เครื่องมือ |
| --- | --- |
| Backend | Java 21, Spring Boot 4 (Web MVC, Data JPA, Cache + Caffeine, Actuator, `@Scheduled`), Maven |
| ดึงข้อมูล | `RestClient` (timeout + User-Agent), Jsoup |
| Database | MySQL 8 + Flyway |
| Frontend | Next.js 16 (App Router, TypeScript), React 19, Recharts, CSS Modules + design tokens |
| ฟอนต์ | Cormorant Garamond + Taviraj (หัวข้อ/ตัวเลข), IBM Plex Sans Thai Looped (เนื้อความ), Cinzel (โลโก้) |
| Test | JUnit 5, Mockito, Testcontainers (MySQL), Jest + React Testing Library |
| Dev / CI | Docker Compose, GitHub Actions, ESLint, Prettier (`frontend/.prettierrc.json`) |

## วิธีรัน

ต้องมี Docker Desktop (หรือ Docker Engine + Compose v2)

```bash
cp .env.example .env      # แก้รหัสผ่านใน .env ก่อนใช้งานจริง
docker compose up --build
```

- เว็บ: <http://localhost:3000>
- API: <http://localhost:8080/api/prices/latest>
- Health: <http://localhost:8080/actuator/health>

ข้อมูลกราฟจะเริ่มสะสมหลังระบบรันไปสักพัก (ดึงราคาทุก 5 นาที)

### รันแยกส่วนสำหรับพัฒนา

ต้องมี Java 21+, Node.js 20.9+ และ MySQL 8

```bash
# Backend (ต้องมี MySQL ที่ localhost:3306 หรือกำหนด DB_URL / DB_USERNAME / DB_PASSWORD)
cd backend && ./mvnw spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```

### Test และตรวจโค้ด

```bash
cd backend && ./mvnw verify          # unit + integration (Testcontainers ต้องมี Docker)

cd frontend
npm test                             # Jest
npm run lint                         # ESLint
npm run typecheck                    # TypeScript
npm run build                        # production build
npx prettier --check "src/**/*.{ts,tsx,css}"
```

ถ้าเครื่องไม่มี Docker, integration test จะถูกข้ามอัตโนมัติ หรือชี้ไปที่ MySQL ที่มีอยู่ได้:

```bash
./mvnw verify "-Dit.mysql.url=jdbc:mysql://127.0.0.1:3306/goldtracker_it?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true" \
  -Dit.mysql.username=root -Dit.mysql.password=...
```

GitHub Actions (`.github/workflows/ci.yml`) รันทั้งหมดนี้ทุก push / pull request และ smoke test ด้วย Docker Compose

### Environment variables

ดู `.env.example` — ค่าที่สำคัญ: `MYSQL_*`, `CORS_ALLOWED_ORIGINS` (ค่าเริ่มต้น `http://localhost:3000`),
`SCHEDULER_INTERVAL` (ISO-8601 เช่น `PT5M`), `NEXT_PUBLIC_API_BASE_URL` (URL ที่ **เบราว์เซอร์** ใช้เรียก API, ฝังตอน build)

## API

ทุก endpoint เป็น `GET`, ตอบ JSON, เวลาเป็น ISO 8601 (UTC), ราคาเป็น **string ทศนิยม** เพื่อไม่เสียความแม่นยำ

| Endpoint | คำอธิบาย |
| --- | --- |
| `GET /api/prices/latest` | ราคาล่าสุดของ `bar`, `ornament`, `spot`, `usdThb` + `previousClose`, `change`, `changePercent` (เทียบราคาสุดท้ายของวันก่อนตามเวลาไทย), `updatedAt` · cache 30 วินาที |
| `GET /api/prices/history?type=bar\|ornament\|spot&range=1D\|1W\|1M\|1Y` | ราคาย้อนหลังรวมเป็นช่วง: 1D รายชั่วโมง, 1W/1M รายวัน, 1Y รายสัปดาห์ (เริ่มวันจันทร์) แต่ละจุดมี `open/high/low/close` (close = ราคาสุดท้ายของช่วง) + `stats` ของทั้งช่วง ทองไทยใช้ราคาขายออก |
| `GET /api/prices/announcements?date=YYYY-MM-DD` | รอบประกาศราคาทองไทยของวันนั้น (ไม่ใส่ = วันนี้ตามเวลาไทย) เรียงล่าสุดก่อน พร้อม `change` ราคาขายทองแท่งเทียบรอบก่อน |

ตัวอย่าง

```http
GET /api/prices/latest
```

```json
{
  "bar": { "buy": "56950.00", "sell": "57150.00", "previousClose": "57000.00",
           "change": "150.00", "changePercent": "0.26", "updatedAt": "2026-11-22T02:09:00Z" },
  "ornament": { "buy": "55803.96", "sell": "57950.00", "...": "..." },
  "spot": { "price": "3539.0000", "unit": "USD", "...": "..." },
  "usdThb": { "price": "33.9184", "unit": "THB", "...": "..." },
  "updatedAt": "2026-11-22T07:17:39Z"
}
```

ค่าพารามิเตอร์ผิดตอบ `400` เป็น problem JSON:

```json
{ "status": 400, "title": "Invalid parameter", "parameter": "range",
  "detail": "Unknown range '5Y'. Allowed values: 1D, 1W, 1M, 1Y" }
```

## แหล่งข้อมูลและข้อจำกัด

| ข้อมูล | แหล่ง | หมายเหตุ |
| --- | --- | --- |
| ทองคำแท่ง / ทองรูปพรรณ 96.5% | [สมาคมค้าทองคำ](https://www.goldtraders.or.th/) | อ่านจาก HTML หน้าแรก (robots.txt อนุญาต `/` และห้าม `/api/` จึงไม่เรียก `/api/`) ดึงไม่เกินทุก 5 นาที |
| XAU/USD | [gold-api.com](https://gold-api.com/) | ฟรี ไม่ใช้ key |
| USD/THB | [Rates By Exchange Rate API](https://www.exchangerate-api.com) (`open.er-api.com`) | ฟรี ไม่ใช้ key **อัปเดตวันละครั้ง** ระบบเรียกไม่เกินชั่วโมงละครั้ง |

- **โครงสร้างเว็บสมาคมอาจเปลี่ยน** parser อาศัยข้อความภาษาไทยที่มองเห็นบนหน้า (`ทองคำแท่ง`, `รับซื้อ`, `ขายออก`, `ประจำวันที่ … (ครั้งที่ N)`)
  ไม่ใช่ CSS class ถ้าหาไม่เจอจะ log error และไม่บันทึกอะไร — ตรวจ log ถ้าราคาไม่อัปเดต
- ทองรูปพรรณ: เว็บสมาคมแสดงช่อง **"ฐานภาษี"** แทนราคารับซื้อ ระบบเก็บค่านี้เป็น `buy` ตามที่สื่อทั่วไปรายงานเป็นราคารับซื้อทองรูปพรรณ
- ราคาอาจช้ากว่าต้นทางได้ราว 1 รอบ scheduler (5 นาที) และรอบประกาศที่เกิดและถูกแทนที่ภายใน 5 นาทีอาจไม่ถูกเก็บ
- ประวัติมีเฉพาะช่วงที่ระบบรันอยู่ (ไม่มีการนำเข้าข้อมูลย้อนหลัง)
- `usd_thb` เป็น `NULL` ได้ถ้ายังไม่เคยดึงอัตราแลกเปลี่ยนสำเร็จ หรืออัตราที่มีเก่ากว่า 48 ชั่วโมง

## เครดิตภาพ

ภาพบนการ์ดราคามาจาก Wikimedia Commons (ปรับสีและตัดขอบภาพ) — รายละเอียดผู้สร้างและสัญญาอนุญาตอยู่ใน
[`frontend/src/assets/prices/CREDITS.md`](frontend/src/assets/prices/CREDITS.md)
ภาพร้านทองโดย Adam Jones ใช้สัญญาอนุญาต [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/) จึงแสดงเครดิตไว้ที่ footer ของเว็บด้วย

## Disclaimer

ข้อมูลบนเว็บไซต์นี้มีไว้เพื่อติดตามราคาเท่านั้น ไม่ใช่คำแนะนำในการลงทุน
ข้อมูลเป็นของเจ้าของแหล่งข้อมูลแต่ละราย โปรเจกต์นี้ไม่มีส่วนเกี่ยวข้องกับสมาคมค้าทองคำหรือผู้ให้บริการข้อมูลข้างต้น
