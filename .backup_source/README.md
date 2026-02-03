# Shop Project (Buy-01)

**changes**


A Microservices-based E-commerce platform.

**changes**

## Prerequisites

This project relies on a **Traefik Reverse Proxy** to handle routing and SSL.
> **Important**: You must have the proxy running and configured before starting this project.
> Refer to [Traefik Reverse Proxy](https://github.com/nicgen/Traefik-Reverse-Proxy) for setup instructions.

## Quick Start

### Option 1: Using Make (Recommended)

1.  **Start the Project**
    ```bash
    make start
    ```
2.  **Other Commands**
    -   Stop: `make stop`
    -   **Reset (Wipe DB & Uploads)**: `make clean`
    -   View Logs: `make logs`

### Option 2: Using Docker Compose

```bash
docker compose up -d --build
```

### Access
-   **Frontend**: [https://app.local.example.com](https://app.local.example.com)
-   **API**: [https://api.local.example.com](https://api.local.example.com)

## Testing

Import the `insomnia_export.json` file into [Insomnia](https://insomnia.rest/) to test all API endpoints.

## Storage

-   **Database**: Stored in a Docker volume.
-   **Uploaded Images**: Mapped to the `./uploads` directory on your host machine.

## Documentation

Detailed documentation has been generated for you:

-   **[Architecture & Database Guide](ARCHITECTURE.md)**: Explains the microservices, data flow, and why there are multiple MongoDB databases.

## Features
-   User Authentication (JWT)
-   Product Management (CRUD)
-   Media Uploads
-   Shopping Cart
-   Seller Dashboard

## Audit & Code Quality (SonarQube)

To start the SonarQube server:

```bash
make sonarqube
```

-   **Access**: [https://sonarqube.local.example.com](https://sonarqube.local.example.com)
-   **Setup Guide**: See [GUIDE.md](GUIDE.md) for detailed configuration steps.

## CI/CD (Jenkins)

To start the Jenkins server:

1.  **Configure Environment**
    ```bash
    cp .env.template .env
    # Edit .env to set your email and domain
    ```
2.  **Start Jenkins**
    ```bash
    make jenkins
    ```
3.  **Access Jenkins**
    -   Open via [https://jenkins.local.example.com](https://jenkins.local.example.com)
    -   Retrieve the initial admin password:
        ```bash
        docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
        ```
