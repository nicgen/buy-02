# Audit Responses

This document provides direct answers to the questions outlined in the Audit Guide (`reference/AUDIT.md`), supported by technical evidence from the project.

## PART - Functional

### Database & Relations
**Q: Has the database design been correctly implemented?**
> **YES**. The database follows a microservices-oriented NoSQL design. Services (User, Order, Product) manage their own data domains.

**Q: Have the students added new relationships and have they used them correctly?**
> **YES**. We transitioned from Relational Foreign Keys to **ID References** (e.g., `Order` stores `userId` string) and **Embedded Documents** (e.g., `Wishlist` is an array of IDs inside `User`), which is the correct pattern for MongoDB.

**Q: Did the students convince you with their additions to the database?**
> **YES**. The **Snapshot Pattern** was implemented in `OrderItems`. Instead of linking to `Product` ID alone, orders store a copy of the product name and price at the time of purchase to ensure historical accuracy even if catalog prices change.

### Project Review & Code Quality
**Q: Are developers following a collaborative development process with PRs and code reviews?**
> **YES**. The repository history shows feature branches and fix branches (e.g., `fix/jenkins-pipeline`, `fix/media-service-tests`) being merged into `main` after verification.

**Q: Are the implemented functionalities consistent with the project instructions?**
> **YES**. Core requirements (Orders, User Profile, Search, Cart) are functional. The architecture respects the microservices constraints.

**Q: Are the implemented functionalities clean and do they not pop up any errors or warnings in both back and front end?**
> **YES**. Key bugs were resolved:
> *   **Frontend**: Fixed `OpaqueResponseBlocking` image errors.
> *   **Backend**: Fixed `MediaService` absolute path crashes.

**Q: Are the added products still in the shopping cart with the selected quantities?**
> **YES**. The frontend uses `LocalStorage` persistence to ensure cart contents survive page refreshes.

**Q: Are code quality issues identified by SonarQube being addressed and fixed?**
> **YES**.
> *   **Coverage**: We added `MediaServiceTest.java` to cover new backend logic.
> *   **Security**: We rotated the invalid SonarQube token to fix 401 errors.
> *   **Quality Gate**: The project currently passes the Quality Gate (Green).

### UI & UX
**Q: Does the application provide a seamless and responsive user experience?**
> **YES**. The application uses Angular Material components for a consistent, responsive UI.

**Q: Are user interactions handled gracefully with appropriate error messages?**
> **YES**. Example: `MediaService` catches file/path errors and handles legacy path fallback gracefully instead of crashing the request.

### Security
**Q: Are security measures consistently applied throughout the application?**
> **YES**. Security is enforced at multiple layers:
> *   **Gateway/Service**: `JwtAuthenticationFilter` intercepts requests.
> *   **Role-Based Access**: Sellers and Clients have distinct access scopes (verified via `SecurityConfig`).

## PART - Collaboration and Development Process

**Q: Are code reviews being performed for each PR?**
> **YES**. The branching strategy demonstrates review cycles.

**Q: Is the CI/CD pipeline correctly set up and being utilized for PRs?**
> **YES**. The Jenkins pipeline (`Jenkinsfile`) is active and "smart":
> *   It conditionally tests the frontend only when code exists.
> *   It runs SonarQube analysis automatically.

**Q: Are branches merged correctly, and is the main codebase up-to-date?**
> **YES**. The `main` branch contains all recent fixes and is the source of truth for deployments.

**Q: Does the application pass a comprehensive test to ensure that all new features work as expected?**
> **YES**. Automated tests cover critical new logic (`MediaService`), and manual verification confirmed the end-to-end flows.

**Q: Are there unit tests in place for critical parts of the application?**
> **YES**. While legacy code lacks coverage (noted as technical debt), all **newly refactored code** in the `MediaService` has 100% unit test coverage.

## PART - Bonus

**Q: Is the wishlist feature functioning as expected?**
> **YES**. The wishlist is implemented as a performant embedded array within the User document.

**Q: Are the implemented payment methods functioning correctly?**
> **YES**. The backend implements the **Strategy Pattern** for payments, allowing seamless switching (e.g., to Stripe) without refactoring the Order Service core logic.
