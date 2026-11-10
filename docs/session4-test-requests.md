# Session 4 — test requests (Postman)

Base URL: `http://localhost:8080`
Customer: `customer@test.com` / `Test1234`   Admin: `admin@idamart.com` / `Admin@12345`

Every request below needs **Authorization → Bearer Token**.
Find your product ids first: open `http://localhost:8080/api/products` in Chrome.

## A. Cart (customer token)

| # | Method + URL | Body | Expected |
|---|---|---|---|
| 1 | GET `/api/cart` | – | 200, `items: []`, total 0 |
| 2 | POST `/api/cart/items` | `{ "productId": 2, "quantity": 2 }` | 200, 1 line, quantity 2 |
| 3 | POST `/api/cart/items` | same again | 200, quantity becomes 4 (added, not duplicated) |
| 4 | PUT `/api/cart/items/2` | `{ "quantity": 1 }` | 200, quantity 1 |
| 5 | PUT `/api/cart/items/2` | `{ "quantity": 999 }` | 400, at most 10 |
| 6 | POST `/api/cart/items` | `{ "productId": 999, "quantity": 1 }` | 404 product not found |
| 7 | GET `/api/cart` with NO token | – | 401 |

## B. Checkout (customer token)

| # | Method + URL | Body | Expected |
|---|---|---|---|
| 8 | POST `/api/orders` | `{ "shippingAddress": "Flat 12, Marina Tower, Dubai, UAE" }` | 201, status PLACED, total = price x qty |
| 9 | GET `/api/cart` | – | 200, cart is now empty |
| 10 | GET `/api/products/2` (Chrome) | – | stock went DOWN by the quantity |
| 11 | POST `/api/orders` again | same | 400 "Your cart is empty" |
| 12 | GET `/api/orders` | – | 200, your order in the list |
| 13 | GET `/api/orders/1` | – | 200, order with items |

## C. Admin manages orders (admin token)

| # | Method + URL | Body | Expected |
|---|---|---|---|
| 14 | GET `/api/admin/orders` | – | 200, every customer's orders |
| 15 | GET `/api/admin/orders?status=PLACED` | – | only PLACED orders |
| 16 | PATCH `/api/admin/orders/1/status` | `{ "status": "CONFIRMED" }` | 200 |
| 17 | PATCH `/api/admin/orders/1/status` | `{ "status": "DELIVERED" }` | 400 can't jump CONFIRMED → DELIVERED |
| 18 | PATCH `/api/admin/orders/1/status` | `{ "status": "SHIPPED" }` | 200 |
| 19 | GET `/api/admin/orders` with the CUSTOMER token | – | 403 |

## D. Cancelling (customer token)

| # | Step | Expected |
|---|---|---|
| 20 | PATCH `/api/orders/1/cancel` (order 1 is SHIPPED now) | 400 can no longer be cancelled |
| 21 | Add to cart → checkout again → note the new order id | 201 |
| 22 | PATCH `/api/orders/{newId}/cancel` | 200, status CANCELLED |
| 23 | GET `/api/products/2` | stock went back UP |

## E. Stock protection

| # | Step | Expected |
|---|---|---|
| 24 | Admin: PUT `/api/products/2` with `"stock": 1` | 200 |
| 25 | Customer: POST `/api/cart/items` with quantity 2 | 400 "Only 1 left in stock" |
