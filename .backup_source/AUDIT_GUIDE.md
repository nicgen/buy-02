# Audit Guide - Buy-02

This guide maps the Audit Questions to specific verification steps and the relevant source code files that demonstrate the implementation.

## 1. Functional - Database & Relations
**Q: Has the database design been correctly implemented? Have relationships been used correctly?**

*   **Verification**:
    1.  Access Mongo Express: `http://localhost:8081`.
    2.  Check `buy01` database.
    3.  Verify `orders` have `userId` (relation to User) and `items` (embedded Product snapshot).
    4.  Verify `users` have `wishlist` (array of Product IDs).

*   **Relevant Files**:
    *   **Order Entity**: `services/order-service/src/main/java/com/buy01/orderservice/model/Order.java`
        *   *Shows `userId` and `List<OrderItem>`.*
    *   **User Entity**: `services/user-service/src/main/java/com/buy01/userservice/model/User.java`
        *   *Shows `List<String> wishlist`.*
    *   **Product Entity**: `services/product-service/src/main/java/com/buy01/productservice/model/Product.java`

## 2. Functional - Orders & Shopping Cart
**Q: Verify Cart persistence and Order creation. Are functionalities consistent?**

*   **Verification**:
    1.  **Cart Persistence**: Add item -> Refresh Page -> Item should remain (LocalStorage).
    2.  **Order Creation**: Click "Checkout" -> Verify redirection -> Check "My Orders" for new PENDING order.

*   **Relevant Files**:
    *   **Cart Logic**: `frontend/src/app/cart/cart.component.ts`
        *   *Handles LocalStorage persistence and checkout trigger.*
    *   **Order Processing**: `services/order-service/src/main/java/com/buy01/orderservice/service/OrderService.java`
        *   *`createOrder` method handles ID generation and strategy selection.*
    *   **Payment Strategy**: `services/order-service/src/main/java/com/buy01/orderservice/service/payment/StripeStrategy.java`
        *   *Shows Stripe integration logic.*

## 3. Functional - Search & Filtering
**Q: Search and filtering functionalities?**

*   **Verification**:
    1.  Go to Products page.
    2.  Search for "Phone" -> List updates.
    3.  Filter by Price (Min/Max) -> List updates.

*   **Relevant Files**:
    *   **Frontend**: `frontend/src/app/product-list/product-list.component.ts`
    *   **Backend Controller**: `services/product-service/src/main/java/com/buy01/productservice/controller/ProductController.java`
        *   *`searchProducts` and `filterProducts` endpoints.*

## 4. Functional - Security
**Q: Verify Role-Based Access and consistency.**

*   **Verification**:
    1.  Login as **Client**. Try to access `http://app.local.hello-there.net/seller-dashboard`.
    2.  Expect redirect to Products or Login (Access Denied).

*   **Relevant Files**:
    *   **User Security**: `services/user-service/src/main/java/com/buy01/userservice/config/SecurityConfig.java`
    *   **Order Security**: `services/order-service/src/main/java/com/buy01/orderservice/config/SecurityConfig.java`
    *   **JWT Filter**: `services/order-service/src/main/java/com/buy01/orderservice/security/JwtAuthenticationFilter.java`
        *   *Shows how tokens are parsed and Roles extracted.*

## 5. Collaboration & Process
**Q: CI/CD Pipeline, Code Reviews, Quality.**

*   **Verification**:
    1.  **Jenkins**: `http://jenkins.local.hello-there.net` -> Check `buy-02` pipeline history.
    2.  **SonarQube**: `http://sonarqube.local.hello-there.net` -> Check Quality Gate.

*   **Relevant Files**:
    *   **Pipeline**: `Jenkinsfile` (Root directory)
    *   **Docker**: `docker-compose.yml`

## 6. Bonus Features
**Q: Wishlist & Payment Methods.**

*   **Verification**:
    1.  **Wishlist**: Click Heart icon on product -> Check Wishlist page.
    2.  **Payment**: Select "Stripe" at checkout (if enabled) or "Pay on Delivery".

*   **Relevant Files**:
    *   **Wishlist Service**: `services/user-service/src/main/java/com/buy01/userservice/service/UserService.java` (toggleWishlist method)
    *   **Strategy Pattern**: `services/order-service/src/main/java/com/buy01/orderservice/service/payment/PaymentStrategy.java`
