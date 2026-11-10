# Deploying IdaMart (free hosting)

| Part | Service | Free plan (checked Nov 2026) |
|---|---|---|
| Database | **TiDB Cloud Starter** (MySQL-compatible) | 5 GiB, no credit card, TLS required |
| Backend API | **Render** web service (Docker) | 512 MB RAM, sleeps after 15 min idle, ~1 min to wake |
| Website | **Render** static site | Served from a CDN, never sleeps |

Free plans change often. Check each provider's pricing page before relying on these numbers.

---

## 1. Database: TiDB Cloud Starter

1. Sign up at **tidbcloud.com** (GitHub or Google login works).
2. **Create cluster → Starter**. Region: **Frankfurt (eu-central-1)** (pick the same region as Render).
3. Open the cluster → **Connect**:
   - Connect with: **General** / **Java (JDBC)**
   - Click **Generate password** and **copy it now** (it's shown once).
   - Note the **Host** (`gateway01.<region>.prod.aws.tidbcloud.com`), **Port** `4000`, and **User** (`xxxxxxxx.root`).
4. Your three values:

```
DB_URL      = jdbc:mysql://<HOST>:4000/idamart?createDatabaseIfNotExist=true&sslMode=VERIFY_IDENTITY&serverTimezone=UTC
DB_USERNAME = <USER>          (e.g. 2abcDEF3ghij.root)
DB_PASSWORD = <PASSWORD>
```

`sslMode=VERIFY_IDENTITY` = encrypted connection with the server's certificate checked (TiDB requires TLS).

---

## 2. Push the latest code to GitHub

```
git add .
git commit -m "Session 10: deployment config"
git push
```

---

## 3. Render: both services from `render.yaml`

1. Sign up at **render.com** with your **GitHub** account (no card needed).
2. **New → Blueprint** → choose the **idamart** repository → Render reads `render.yaml`.
3. It asks for the `sync: false` values:

| Variable | Service | Value |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | idamart-api | from step 1 |
| `ADMIN_PASSWORD` | idamart-api | a **new strong password** for `admin@idamart.com` (the default is public on GitHub!) |
| `CORS_ORIGINS` | idamart-api | `https://idamart-web.onrender.com` |
| `VITE_API_URL` | idamart-web | `https://idamart-api.onrender.com` |

4. Click **Apply**. The API build takes ~5–10 minutes the first time (Maven downloads everything); the website ~2 minutes.

### If Render gave your services different addresses
If the names were taken, Render adds a suffix (e.g. `idamart-api-x7k2.onrender.com`). Then:
1. **idamart-api → Environment** → set `CORS_ORIGINS` to the website's real address → Save (redeploys).
2. **idamart-web → Environment** → set `VITE_API_URL` to the API's real address → Save → **Manual Deploy → Clear build cache & deploy**
   (the website reads `VITE_API_URL` when it's *built*, so it must be rebuilt).

---

## 4. Check it works

1. Open `https://<api>/api/products`: JSON with 20 products (the seeder fills the new database on first start).
2. Open `https://<web>`: the shop. The first visit may show *"Waking up the IdaMart server…"* for up to a minute.
3. Sign up, add to cart, check out.
4. Log in as `admin@idamart.com` with **your new ADMIN_PASSWORD** → Admin dashboard.

Render logs: **idamart-api → Logs**. Look for `Started IdaMartApplication` and `Demo catalogue: added 20 products`.

---

## 5. Run the Selenium suite against the live site (optional)

Eclipse → Run Configurations → your Maven build → Goals:
```
test -Dbrowser=edge -DbaseUrl=https://<web> -DadminPassword=<your ADMIN_PASSWORD>
```
Visit the site once first so the server is awake.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| Website loads but says it can't reach the server | `VITE_API_URL` wrong or website not rebuilt after changing it (Clear build cache & deploy) |
| Browser console shows a CORS error | `CORS_ORIGINS` on the API must exactly equal the website address (https, no trailing slash) |
| API logs: `Communications link failure` / SSL errors | Check `DB_URL` host/port 4000 and that it contains `sslMode=VERIFY_IDENTITY` |
| API logs: `Access denied` | `DB_USERNAME` must include the prefix (`xxxx.root`); regenerate the password in TiDB if needed |
| API restarts with "out of memory" | The Dockerfile already caps memory for 512 MB; don't add extra JVM options |
| First load is slow | Normal on the free plan: the API sleeps after 15 minutes without visitors |
| Product IDs jump (e.g. 1, 30001) | Normal on TiDB: it hands out auto-increment IDs in blocks across its servers |
