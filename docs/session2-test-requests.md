# Session 2 — test requests (copy into Postman)

Base URL: `http://localhost:8080`
For every POST / PUT: Body → raw → JSON.

## 1. Create a category
POST `/api/categories`
```json
{ "name": "Figures", "description": "Collectible anime figures" }
```

## 2. List categories
GET `/api/categories`

## 3. Create a product (use the category id from step 1, usually 1)
POST `/api/products`
```json
{
  "name": "Test Ninja Figure",
  "description": "A test product to check the API",
  "animeSeries": "Naruto",
  "price": 49.99,
  "stock": 10,
  "imageUrl": "",
  "categoryId": 1
}
```

## 4. List / search products
- GET `/api/products`
- GET `/api/products?keyword=ninja`
- GET `/api/products?anime=Naruto&sort=price_asc`
- GET `/api/products?page=0&size=5`

## 5. One product
GET `/api/products/1`

## 6. Update a product
PUT `/api/products/1` (same body as step 3, change the price to 39.99)

## 7. Delete (hide) a product
DELETE `/api/products/1` → 204 No Content.
Then GET `/api/products/1` → 404.

## 8. Error checks (these SHOULD fail, with clean messages)
| Request | Expected |
|---|---|
| POST `/api/categories` with `{ "name": "" }` | 400, fieldErrors.name |
| POST `/api/categories` with `{ "name": "Figures" }` again | 409 already exists |
| POST `/api/products` with `"price": -5` | 400, fieldErrors.price |
| GET `/api/products/999` | 404 not found |
| GET `/api/products/abc` | 400 invalid value |
| DELETE `/api/categories/1` while it has products | 400 still has products |
