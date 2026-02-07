# Audit Guide - Buy-02

This guide is designed to assist in auditing the technical architecture, code quality, and functional completeness of the `buy-02` project.

## 1. Technical Architecture & Code Explanation

### Database Evolution (Relation Analysis)
**Context**: Moving from `buy-01` (Monolith) to `buy-02` (Microservices).

*   **Decoupled Relations (NoSQL Style)**:
    *   **Old Way**: Strict Foreign Keys (e.g., `FOREIGN KEY (user_id) REFERENCES users(id)`).
    *   **New Way**: **ID References**. Services are independent. The `Order` service does not "know" the `User` table exists; it only stores the `userId` string.
    *   **Verifiable in Code**: `com.buy01.orderservice.model.Order` -> `private String userId;`.

*   **The Snapshot Pattern (Data Duplication)**:
    *   **Old Way**: JOINing `OrderItems` with `Products` to get the price/name. Risk: If product price changes, old order history changes.
    *   **New Way**: **Snapshotting**. When an order is created, we copy the *current* `name` and `price` into the `OrderItem`.
    *   **Verifiable in Code**: `com.buy01.orderservice.model.OrderItem` contains `name` and `price` fields, duplicating data from Product to ensure historical accuracy.

*   **Simplified Many-to-Many (Wishlist)**:
    *   **Old Way**: A join table `user_wishlist` mapping `user_id` <-> `product_id`.
    *   **New Way**: **Embedded Arrays**. The `User` document contains a `List<String> wishlist`.
    *   **Verifiable in Code**: `com.buy01.userservice.model.User` -> `private List<String> wishlist;`.

### Design Patterns Implemented
*   **Strategy Pattern (Payments)**:
    *   Used to switch between payment methods (`Stripe`, `PayOnDelivery`) without changing core logic.
    *   **Code**: `com.buy01.orderservice.service.payment.PaymentStrategy` interface.
*   **Controller-Service-Repository**:
    *   Standard Spring Boot layered architecture to separate concerns.

---

## 2. Functional Audit Steps

### Database Persistence
*   **Action**: Access Mongo Express (`http://localhost:8081`).
*   **Verify**:
    1.  **Separation**: Although all services share the `buy01` DB container, `orders` and `users` are in distinct collections.
    2.  **Relations**: Check an Order document. It should have a `userId` (String) matching a document in the `users` collection.

### Orders & Shopping Cart
*   **Action**: Add items to cart, refresh page, verify persistence. Place an order.
*   **Verify**:
    1.  **Frontend**: `LocalStorage` is used for cart persistence (`cart.component.ts`).
    2.  **Backend**: `OrderService.createOrder` receives the cart items and generates the `OrderItem` snapshots.

### User Profile & Wishlist
*   **Action**: Update user address. Toggle wishlist on a product.
*   **Verify**:
    1.  **User**: `UpdateUserRequest` DTO handles profile updates.
    2.  **Wishlist**: Clicking the heart icon adds the Product ID to the `users` collection's `wishlist` array.

### Search & Filtering
*   **Action**: Search for "RAM" or filter by price.
*   **Verify**:
    1.  **ProductService**: `ProductRepository` uses `MongoTemplate` or query methods to filter results.

---

## 3. DevOps & Security Audit

### CI/CD Pipeline (Jenkins)
*   **Action**: Check Jenkins History (`http://jenkins.local.hello-there.net`).
*   **Verify**:
    1.  **Conditional Execution**: Pipeline skips "Test Frontend" if `package.json` is missing (smart pipeline).
    2.  **SonarQube Integration**: Analysis runs automatically. Note: `frontend` is excluded if missing.

### Code Quality (SonarQube)
*   **Action**: Check SonarQube Dashboard (`http://sonarqube.local.hello-there.net`).
*   **Verify**:
    1.  **Quality Gate**: Passing (Green).
    2.  **Coverage**: `MediaService` logic is fully covered by unit tests.

### Security
*   **Action**: Attempt to access Seller Dashboard as a Client.
*   **Verify**:
    1.  **Access Denied**: 403 Forbidden or Redirect.
    2.  **Implementation**: `JwtAuthenticationFilter` validates tokens and roles at the gateway/service level.
