# Assignment 02: Audit of OrderBillingEngine.java

## Code smells

| # | Smell | Lines | Why it is a smell here |
|---|-------|-------|------------------------|
| 1 | Long Method / God Method | 21-150 | One 130-line method does validation, pricing, tax, payment, logging and notification. |
| 2 | Magic numbers | 62-77, 85-91, 103, 112, 120 | Rates (0.15, 0.10, 0.08, 0.20, 0.05), thresholds (5000, 10000, 10, 25000), tax rates, and 16/11-digit lengths have no names. |
| 3 | Repeated type-code conditionals | 61-79, 86-92, 101-128, 140-147 | Four separate if/else-if chains on strings. Every new type means editing the same method. |
| 4 | Primitive obsession / untyped data | 21, 28, 35, 48-49 | The order is a Map<String, Object> with unchecked casts. The (double) cast crashes on an Integer price. |
| 5 | Console printing as error handling | 24, 30, 37, 45, 51, 107, 116, 121, 126 | Every failure is println plus return false, so the caller cannot tell why it failed. |
| 6 | Hidden side effects / hardcoded dependencies | 19, 131-136, 141-146 | File I/O and console output are embedded in business logic and cannot be substituted. |
| 7 | Swallowed exception | 134-136 | A failed log write only prints a message and the method still returns true. |
| 8 | Stringly-typed status and bad default | 98-99, 105, 114, 124 | Statuses are raw strings. The default method "CASH" matches no branch, so a missing paymentMethod is always rejected. |
## SOLID violations

| # | Principle | Lines | Violation | Fixed by |
|---|-----------|-------|-----------|----------|
| 1 | SRP | 21-150 | One method has six reasons to change: validation (22-55), discounts (57-79), tax (83-92), payment (97-128), logging (130-136), notification (138-147). | OrderValidator, DiscountCalculator, TaxCalculator, PaymentStrategy implementations, OrderRepository, NotificationService, plus a slim OrderBillingService |
| 2 | OCP | 101-128 (also 61-79, 86-92, 140-147) | Adding a payment method such as JazzCash means editing the if/else-if chain. | PaymentStrategy interface with CreditCardPayment, EasyPaisaPayment, CashOnDeliveryPayment |
| 3 | DIP | 19, 131-136, 141-146 | Business logic depends directly on FileWriter and System.out, so it cannot run without disk and console. | OrderRepository and NotificationService interfaces, injected via the constructor |