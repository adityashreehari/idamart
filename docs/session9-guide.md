# Session 9 — testing guide

## 1. Selenium UI automation (automation-tests/)
Needs: backend running (Eclipse) + website running (npm run dev) + Chrome installed.

Eclipse: File → Import → Maven → Existing Maven Projects → choose `automation-tests`.
Run all: right-click `src/test/resources/testng.xml` → Run As → TestNG Suite
(or right-click the project → Run As → Maven test).

Options (Run Configurations → Arguments → VM arguments):
- `-Dheadless=true`  run without showing the browser
- `-Dbrowser=edge`   use Microsoft Edge instead of Chrome
- `-DbaseUrl=https://your-live-site`  test the deployed site (Session 10)

Reports: `automation-tests/test-output/index.html` (TestNG) and
screenshots of any failure in `automation-tests/target/screenshots/`.

| Class | Tests |
|---|---|
| BrowseTest | home, search, no results, category filter, sort, out of stock, low stock, 404 |
| AuthTest | sign up, 5 validation cases, duplicate email, admin login, wrong password, redirect, logout, add-to-cart needs login |
| CartTest | add, change quantity, remove, clear, quick view, survives refresh |
| CheckoutTest | full order, checkout validation, cancel, admin confirms → customer sees it |
| AdminTest | dashboard, customer blocked, add → find → remove product |

## 2. Manual testing
`docs/testing/IdaMart-Test-Cases.xlsx`
- Summary: totals and pass rate (update automatically)
- Test Cases: 45 cases. Fill the yellow columns (Actual Result, Status, Tested On, Remarks).
- Bug Report: 2 real bugs found and fixed during development.

## 3. The testing pyramid in this project
| Level | Tool | Where |
|---|---|---|
| Unit | JUnit 5 + Mockito | backend/src/test |
| API | Postman collection with assertions | docs/postman |
| UI / end-to-end | Selenium + TestNG | automation-tests |
| Manual | Test case sheet | docs/testing |
