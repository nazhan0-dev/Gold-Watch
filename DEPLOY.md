# Deploy ฟรี: Vercel + Render + TiDB Cloud

```
เบราว์เซอร์ ──► Vercel (หน้าเว็บ Next.js) ──► Render (Spring Boot API + ตัวดึงราคา) ──► TiDB Cloud (ฐานข้อมูล MySQL-compatible)
```

| ส่วน | บริการ | ราคา |
| --- | --- | --- |
| หน้าเว็บ `frontend/` | [Vercel](https://vercel.com) แผน Hobby | ฟรี |
| Backend `backend/` | [Render](https://render.com) Free web service (Docker) | ฟรี (หลับหลังไม่มีคนเข้า 15 นาที) |
| ฐานข้อมูล | [TiDB Cloud](https://tidbcloud.com) Starter | ฟรี |
| กัน backend หลับ | [UptimeRobot](https://uptimerobot.com) | ฟรี |

ทำตามลำดับ 1 → 6 ทุกขั้นสมัครด้วยบัญชี GitHub ได้ ไม่ต้องใช้บัตร
**อย่า commit รหัสผ่านลงไฟล์ใด ๆ** ใส่ในหน้า Environment ของแต่ละบริการเท่านั้น

---

## 1. ฐานข้อมูล — TiDB Cloud Starter

1. สมัครที่ <https://tidbcloud.com> → สร้าง cluster แบบ **Starter** (ฟรี)
   เลือก region **AWS – Singapore (ap-southeast-1)** (ใกล้ไทยและใกล้ Render Singapore)
2. เข้า cluster → ปุ่ม **Connect**
   - Connect With: **General** (หรือ Java/JDBC)
   - กด **Generate Password** แล้ว **คัดลอกเก็บไว้** (แสดงครั้งเดียว)
   - จดค่า **HOST** (เช่น `gateway01.ap-southeast-1.prod.aws.tidbcloud.com`), **PORT** `4000`,
     และ **USERNAME** (มีจุดนำหน้า เช่น `xxxxxxxx.root`)
3. เมนู **SQL Editor** ของ cluster รัน:

   ```sql
   CREATE DATABASE goldtracker;
   ```

   ตารางไม่ต้องสร้างเอง — Flyway ใน backend สร้างให้ตอนเริ่มครั้งแรก

4. เตรียมค่า `DB_URL` (แทน `<HOST>` ด้วยของจริง):

   ```text
   jdbc:mysql://<HOST>:4000/goldtracker?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true
   ```

## 2. Backend — Render

1. สมัครที่ <https://render.com> ด้วย GitHub และอนุญาตให้ Render เห็น repo `Gold-Watch`
2. **New → Web Service** → เลือก repo `nazhan0-dev/Gold-Watch`
3. ตั้งค่า:

   | ช่อง | ค่า |
   | --- | --- |
   | Name | `goldwatch-api` (ชื่อนี้จะเป็น URL) |
   | Language | **Docker** |
   | Branch | `main` |
   | Region | **Singapore** |
   | Root Directory | `backend` |
   | Instance Type | **Free** |

4. **Environment Variables**:

   | Key | Value |
   | --- | --- |
   | `DB_URL` | ค่าจากข้อ 1.4 |
   | `DB_USERNAME` | username จาก TiDB (เช่น `xxxxxxxx.root`) |
   | `DB_PASSWORD` | password จาก TiDB |
   | `CORS_ALLOWED_ORIGINS` | ใส่ `http://localhost:3000` ไปก่อน แล้วค่อยแก้ในขั้นที่ 4 |
   | `TZ` | `UTC` |

5. **Advanced → Health Check Path**: `/actuator/health/readiness`
6. กด **Deploy Web Service** — build ครั้งแรกใช้ประมาณ 5–10 นาที
   ดูแท็บ **Logs** จนเห็น `Started GoldTrackerApplication` และ `Stored BAR ...`
7. ทดสอบ: เปิด `https://goldwatch-api.onrender.com/api/prices/latest` (ใช้ URL จริงของคุณ) ต้องเห็น JSON ราคา

## 3. หน้าเว็บ — Vercel

1. สมัครที่ <https://vercel.com> ด้วย GitHub
2. **Add New → Project** → Import repo `Gold-Watch`
3. ตั้งค่า:
   - **Root Directory**: `frontend` (กด Edit แล้วเลือก)
   - Framework Preset: **Next.js** (ขึ้นเอง)
   - **Environment Variables**: `NEXT_PUBLIC_API_BASE_URL` = URL ของ Render
     เช่น `https://goldwatch-api.onrender.com` (**ไม่มี `/` ท้าย**)
4. กด **Deploy** → ได้ URL เช่น `https://gold-watch.vercel.app`

> `NEXT_PUBLIC_API_BASE_URL` ถูกฝังตอน build — ถ้าแก้ค่าภายหลังต้อง **Redeploy** ใน Vercel ด้วย

## 4. อนุญาตให้หน้าเว็บเรียก API (CORS)

กลับไปที่ Render → service `goldwatch-api` → **Environment** → แก้
`CORS_ALLOWED_ORIGINS` = URL ของ Vercel เช่น `https://gold-watch.vercel.app` (ไม่มี `/` ท้าย)
→ **Save Changes** (Render จะ deploy ใหม่ให้เอง)

## 5. กัน backend หลับ — UptimeRobot

Render free จะหลับเมื่อไม่มีคนเข้า 15 นาที ระหว่างหลับตัวดึงราคาจะหยุด กราฟจะขาดช่วง

1. สมัครที่ <https://uptimerobot.com> → **New Monitor**
2. Type **HTTP(s)**, URL `https://goldwatch-api.onrender.com/actuator/health`, Interval **5 minutes**

เปิดตลอดเดือนใช้ประมาณ 720–744 ชั่วโมง อยู่ในโควตาฟรี 750 ชั่วโมงของ Render
(ถ้ามี service ฟรีตัวอื่นใน Render ด้วย โควตาจะไม่พอ)

## 6. ตรวจสอบ

เปิด URL ของ Vercel → ควรเห็นราคาล่าสุด, กราฟ (ข้อมูลเริ่มสะสมหลัง deploy) และตารางรอบประกาศ
แล้วนำลิงก์ไปใส่ใน README ส่วนบนสุด และใน GitHub repo → About → Website

---

## แก้ปัญหา

| อาการ | สาเหตุ / วิธีแก้ |
| --- | --- |
| หน้าเว็บขึ้น "ยังโหลดราคาไม่ได้" นาน ~1 นาทีแล้วหาย | Render เพิ่งตื่นจากหลับ — ตั้ง UptimeRobot (ขั้น 5) |
| Console ของเบราว์เซอร์ขึ้น `CORS` | `CORS_ALLOWED_ORIGINS` ไม่ตรงกับ URL ของ Vercel ทุกตัวอักษร (https, ไม่มี `/` ท้าย) |
| Render log: `Communications link failure` / SSL | ตรวจ `DB_URL` ว่า host/port 4000 ถูก และมี `sslMode=VERIFY_IDENTITY` |
| Render log: `Access denied` | `DB_USERNAME` ต้องมี prefix (`xxxxxxxx.root`) และรหัสถูก |
| Render log: `Unknown database 'goldtracker'` | ยังไม่ได้รัน `CREATE DATABASE goldtracker;` (ขั้น 1.3) |
| Render แจ้ง out of memory | free มี RAM 512 MB — Dockerfile ตั้ง heap 60% ไว้แล้ว ถ้ายังเกิด ลองตั้ง env `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=50` |
| หน้าเว็บยังเรียก `localhost:8080` | ตั้ง `NEXT_PUBLIC_API_BASE_URL` แล้วต้อง Redeploy ใน Vercel |

## ข้อจำกัดของ deploy แบบฟรี

- TiDB เข้ากันได้กับ MySQL แต่ไม่ใช่ MySQL แท้ — test ใน CI ยังรันกับ MySQL 8 จริง
- ถ้า backend หลับหรือ build ใหม่ ช่วงนั้นจะไม่มีการเก็บราคา
- บริการฟรีเปลี่ยนเงื่อนไขได้ ตรวจหน้าราคาของแต่ละเจ้าเป็นระยะ
