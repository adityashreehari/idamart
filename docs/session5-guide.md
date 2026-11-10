# Session 5 — tests, demo catalogue, Postman collection

## Run the unit tests (Eclipse)
Right-click `src/test/java` → Run As → JUnit Test.
The JUnit tab should show all green, 0 failures.
No database is needed: repositories are replaced by Mockito mocks.

| Test class | What it proves |
|---|---|
| OrderStatusTest | Orders only move forward one step; DELIVERED/CANCELLED are final |
| ProductServiceImplTest | 404 for missing/hidden products, soft delete, page size capped at 50 |
| CategoryServiceImplTest | No duplicate names, can't delete a category that has products |
| CartServiceImplTest | Same product increases quantity, max 10, can't exceed stock, totals add up |
| OrderServiceImplTest | Checkout reduces stock and empties cart; failure changes nothing; cancel restores stock; can't see others' orders |
| AuthServiceImplTest | Passwords are hashed, role is always CUSTOMER, duplicate email → 409 |
| JwtServiceTest | Tokens round-trip; edited or expired tokens are rejected |

## Fresh start with the demo catalogue (recommended)
In MySQL Workbench:
```sql
DROP DATABASE idamart_db;
```
Run the app again. It recreates the tables, the admin account, 6 categories and 20 products.

## Postman collection
Postman → Import → `docs/postman/IdaMart.postman_collection.json`
Then right-click the collection → Run → Run IdaMart API.
Login requests store the tokens for you; every request has a pass/fail test.
