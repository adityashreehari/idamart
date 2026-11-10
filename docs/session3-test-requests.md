# Session 3 — test requests (Postman)

Base URL: `http://localhost:8080`
Admin created automatically on startup: `admin@idamart.com` / `Admin@12345`

## 1. Public things still work without logging in
- GET `/api/products` → 200
- GET `/api/categories` → 200

## 2. Admin actions are now locked
POST `/api/categories` with `{ "name": "Posters" }` and NO token → **401** "Please log in to continue"

## 3. Register a customer
POST `/api/auth/register`
```json
{ "fullName": "Test Customer", "email": "customer@test.com", "password": "Test1234" }
```
→ **201**, returns a `token` and `"role": "CUSTOMER"`

## 4. Log in as the customer
POST `/api/auth/login`
```json
{ "email": "customer@test.com", "password": "Test1234" }
```
→ **200**, copy the `token`

## 5. Who am I?
GET `/api/auth/me`
Authorization tab → Type: **Bearer Token** → paste the customer token
→ **200**, shows the customer

## 6. Customer tries an admin action
POST `/api/categories` with `{ "name": "Posters" }` + customer token → **403** "You do not have permission"

## 7. Log in as admin
POST `/api/auth/login`
```json
{ "email": "admin@idamart.com", "password": "Admin@12345" }
```
→ **200**, `"role": "ADMIN"`, copy this token

## 8. Admin creates a category
POST `/api/categories` with `{ "name": "Posters" }` + admin token → **201**

## 9. Error checks
| Request | Expected |
|---|---|
| Register again with `customer@test.com` | 409 already exists |
| Register with password `short` | 400 password rules |
| Register with email `not-an-email` | 400 email must be valid |
| Login with a wrong password | 401 Invalid email or password |
| GET `/api/auth/me` with no token | 401 |
| GET `/api/auth/me` with token `abc.def.ghi` | 401 |

## 10. Check the database
In MySQL Workbench:
```sql
SELECT id, full_name, email, role, password FROM idamart_db.users;
```
Passwords start with `$2a$` — that's a BCrypt hash, not the real password.
