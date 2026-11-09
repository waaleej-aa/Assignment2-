**Baseline:** commit `c419b08`, file `com/uet/legacy/Assignment2/src/main/java/com/uet/legacy/OrderBillingEngine.java` (161 lines). All line numbers below refer to this file at that commit.
## Code smells

| # | Smell | Lines | Why it is a smell here |
|---|-------|-------|------------------------|
| 1 | Long Method / God Method | 22-151 | One 130-line method does validation, pricing, tax, payment, logging and notification. |
| 2 | Magic numbers | 63-78, 86-92, 104, 113, 121 | Rates (0.15, 0.10, 0.08, 0.20, 0.05), thresholds (5000, 10000, 10, 25000), tax rates, and 16/11-digit lengths have no names. |
| 3 | Repeated type-code conditionals | 62-80, 87-93, 102-129, 141-148 | Four separate if/else-if chains on strings. Every new type means editing the same method. |
| 4 | Primitive obsession / untyped data | 22, 29, 36, 49-50 | The order is a Map<String, Object> with unchecked casts. The (double) cast crashes on an Integer price. |
| 5 | Console printing as error handling | 25, 31, 38, 46, 52, 108, 117, 122, 127 | Every failure is println plus return false, so the caller cannot tell why it failed. |
| 6 | Hidden side effects / hardcoded dependencies | 20, 132-137, 142-147 | File I/O and console output are embedded in business logic and cannot be substituted. |
| 7 | Swallowed exception | 135-137 | A failed log write only prints a message and the method still returns true. |
| 8 | Stringly-typed status and bad default | 99-100, 106, 115, 125 | Statuses are raw strings. The default method "CASH" matches no branch, so a missing paymentMethod is always rejected. |

## SOLID violations

| # | Principle | Lines | Violation | Fixed by |
|---|-----------|-------|-----------|----------|
| 1 | SRP | 22-151 | One method has six reasons to change: validation (23-56), discounts (58-80), tax (84-96), payment (98-129), logging (131-137), notification (139-148). | DiscountCalculator, TaxCalculator, PaymentStrategy implementations, FileOrderRepository, NotificationService implementations, plus a slim OrderBillingService (validation stays in OrderBillingService). |
| 2 | OCP | 102-129 (also 62-80, 87-93, 141-148) | Adding a payment method such as JazzCash means editing the if/else-if chain. | PaymentStrategy interface with CreditCardPayment, EasyPaisaPayment, CashOnDeliveryPayment |
| 3 | DIP | 20, 132-137, 142-147 | Business logic depends directly on FileWriter and System.out, so it cannot run without disk and console. | OrderRepository and NotificationService interfaces, injected via the constructor |