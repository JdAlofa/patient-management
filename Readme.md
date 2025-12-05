# Patient Management System

A microservices-based application for managing patient data, authentication, billing, and analytics. The system uses an event-driven architecture with Kafka and synchronous communication via gRPC and REST.

## 🏗 Architecture Overview

The system is composed of five main components running in Docker containers:

1.  **API Gateway (Spring Cloud Gateway):** The single entry point for all external requests. It handles routing and validates JWT tokens via the Auth Service before forwarding requests.
2.  **Auth Service:** Manages user authentication and JWT token generation/validation.
3.  **Patient Service:** The core service for managing patient records. It publishes events to Kafka when patients are created and communicates with the Billing Service via gRPC.
4.  **Billing Service:** Handles billing accounts. It receives gRPC calls from the Patient Service to create accounts instantly upon patient registration.
5.  **Analytics Service:** Consumes `PatientEvent` messages from Kafka to perform data analysis and logging.

### Infrastructure

- **Kafka (KRaft mode):** Handles asynchronous messaging between Patient and Analytics services.
- **PostgreSQL:** Dedicated database instances for Patient and Auth services.

## 🛠 Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.x (Web, Data JPA, Validation, Security)
- **Communication:**
  - **REST:** External API access.
  - **gRPC:** Internal synchronous communication (Patient -> Billing).
  - **Kafka:** Internal asynchronous event streaming (Patient -> Analytics).
  - **Protobuf:** Data serialization for gRPC and Kafka events.
- **Database:** PostgreSQL 15
- **Containerization:** Docker & Docker Compose
- **Gateway:** Spring Cloud Gateway with custom JWT Filter.

## 🚀 Setup & Installation

### Prerequisites

- Docker & Docker Compose
- Java 21 (optional, if running locally without Docker)
- Maven (optional, wrapper included)
- Postman (optional, for API testing)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd patient-management
```

### 2. Environment Configuration

Create a `.env` file in the root directory with the following content.

> **Note:** The JWT secret must be at least 32 bytes (256 bits) long to meet security standards.  
> You can generate one with: `openssl rand -hex 32`

```properties
JWT_SECRET=your_very_long_secure_secret_key_here
```

### 3. Build and Run with Docker Compose

```bash
docker compose up --build
```

Wait for the health checks to pass. Kafka and PostgreSQL need time to initialize before dependent services can start.

## 🧪 Testing the Application

### Option 1: Using Postman (Recommended)

A Postman collection is included in the repository for easy API testing.

#### Import the Collection

1. Open Postman.
2. Click **Import** (top-left).
3. Select the file: `Patient-Management.postman_collection.json` from the project root.
4. The collection will appear in your sidebar with two folders:
   - **Auth-Service:** Login and token validation requests.
   - **Patient-Service:** CRUD operations for patients.

#### Collection Variables

The collection uses a variable `{{patient-id}}` for patient-specific operations. After creating a patient, copy the returned ID and set it in the collection variables:

1. Click on the collection name **Patient-Management**.
2. Go to the **Variables** tab.
3. Set the `patient-id` value.

#### Workflow

1. **Login:** Run `Auth-Service > login` to get a JWT token.
2. **Copy Token:** Copy the token from the response.
3. **Set Authorization:** For requests requiring authentication (e.g., `get-patients`), the Bearer token is already configured. Update it if expired.
4. **Create/Update/Delete Patients:** Use the requests in the `Patient-Service` folder.

### Option 2: Manual Testing

All requests should be sent through the **API Gateway** on port `4004`.

#### 1. Login (Get Token)

Authenticate and receive a JWT token.

- **Endpoint:** `POST http://localhost:4004/auth/login`
- **Body:**
  ```json
  {
    "email": "testuser@test.com",
    "password": "password123"
  }
  ```
- **Response:** Copy the `token` value from the JSON response.

#### 2. Create a Patient

This action triggers both a gRPC call to the Billing Service and a Kafka event to the Analytics Service.

- **Endpoint:** `POST http://localhost:4004/api/patients`
- **Headers:**
  ```
  Authorization: Bearer <YOUR_TOKEN>
  Content-Type: application/json
  ```
- **Body:**
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St",
    "dateOfBirth": "1990-01-01",
    "registeredDate": "2024-06-15"
  }
  ```

##### What Happens Internally:

1.  API Gateway validates the JWT token with the Auth Service.
2.  Patient Service saves the patient record to PostgreSQL.
3.  Patient Service calls the Billing Service via **gRPC** to create a billing account.
4.  Patient Service publishes a `PatientEvent` to the **Kafka** `patient` topic.

#### 3. Verify Kafka Consumer (Analytics Service)

Check the logs of the Analytics Service to confirm it received the Kafka message.

```bash
docker compose logs -f analytics-service
```

**Expected Output:**

```
Received patient event : [PatientId=..., Name=John Doe, Email=john.doe@example.com]
```

#### 4. Get All Patients

Retrieve a list of all registered patients.

- **Endpoint:** `GET http://localhost:4004/api/patients`
- **Headers:**
  ```
  Authorization: Bearer <YOUR_TOKEN>
  ```

#### 5. Update a Patient

- **Endpoint:** `PUT http://localhost:4000/patients/{patient-id}`
- **Body:**
  ```json
  {
    "name": "John Updated",
    "email": "john.updated@example.com",
    "address": "456 New St",
    "dateOfBirth": "1990-01-01"
  }
  ```

#### 6. Delete a Patient

- **Endpoint:** `DELETE http://localhost:4000/patients/{patient-id}`

## 📂 Project Structure

```
patient-management/
├── api-gateway/                            # Spring Cloud Gateway & JWT Validation Filter
├── auth-service/                           # User Authentication & JWT Generation
├── patient-service/                        # Patient CRUD, Kafka Producer, gRPC Client
├── billing-service/                        # gRPC Server for Billing Account Creation
├── analytics-service/                      # Kafka Consumer for Patient Events
├── api-requests/                           # .http files for testing endpoints in VS Code
├── Patient-Management.postman_collection.json  # Postman Collection for API Testing
├── docker-compose.yml                      # Container Orchestration
├── .env                                    # Environment Variables (Git Ignored)
└── Readme.md                               # This file
```

## 🐛 Troubleshooting

| Problem                                 | Solution                                                                                                |
| --------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| **Kafka: `INVALID_REPLICATION_FACTOR`** | Ensure `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1` is set in `docker-compose.yml` for single-node setup. |
| **Kafka: `CLUSTER_ID is required`**     | Add `CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk` to the Kafka environment variables.                             |
| **Gateway: `401 Unauthorized`**         | Ensure your JWT token is valid and the header includes the `Bearer ` prefix (with a space).             |
| **Gateway: `404 Not Found`**            | Check that the route URIs in `application.yml` use `http://` not `https://`.                            |
| **gRPC: Connection Refused**            | Verify the Billing Service container is running and port `9001` is exposed.                             |
| **JWT: `WeakKeyException`**             | Your `JWT_SECRET` in `.env` must be at least 32 characters (256 bits).                                  |

## 🛑 Stopping the Application

To stop all running containers:

```bash
docker compose down
```

To stop and remove all data (including database volumes):

```bash
docker compose down -v
```
