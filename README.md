# CDQ Application

A Spring Boot application for managing and analyzing bank transactions.

## Prerequisites
- Java 21 or higher
- Docker (recommended for running MongoDB and/or the application)

## Running the Application

There are two primary ways to run the application locally:

### Option 1: Run with Gradle and Docker (Local Development)
This is the recommended approach for development. It runs MongoDB in a Docker container and the Spring Boot application on your host machine.

1. Start the MongoDB database:
   ```bash
   cd local_deployment && docker-compose -f docker-compose.yml up -d
   ```
2. Run the Spring Boot application using one of the following methods:

   **Gradle wrapper:**
   ```bash
   cd .. && ./gradlew bootRun
   ```

   **IntelliJ IDEA run configuration:**  
   A shared run configuration is available in `.run/CdqApplication.run.xml`. It is automatically detected by IntelliJ — simply select **CdqApplication** from the Run menu.

By default, the application will be available at `http://localhost:8080` and will connect to the local MongoDB instance at `mongodb://localhost:27017/bank-transactions`.

### Option 2: Run entirely with Docker Compose
If you want to run the entire stack (both the Java application and MongoDB) in Docker containers without needing Java installed on your host machine:

1. Navigate to the local deployment directory and build and start the containers:
   ```bash
   cd local_deployment && docker-compose -f docker-compose-app.yml up --build -d
   ```

## Configuration

### Spring Profiles

The application uses Spring profiles to manage environment-specific configuration:

| Profile   | Purpose                        | MongoDB URI                                      |
|-----------|--------------------------------|--------------------------------------------------|
| `local`   | Local development (default)    | `mongodb://localhost:27017/bank-transactions`     |
| `dev`     | Shared dev environment         | Requires `MONGODB_URI` env variable              |
| `prod`    | Production                     | Requires `MONGODB_URI` env variable              |

Activate a profile via environment variable:
```bash
SPRING_PROFILES_ACTIVE=dev MONGODB_URI=mongodb://localhost:27017/bank-transactions ./gradlew bootRun
```

Or via command-line argument:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev --spring.mongodb.uri=mongodb://localhost:27017/bank-transactions'
```

### Key Environment Variables

| Variable                | Description                        | Required in        |
|-------------------------|------------------------------------|--------------------|
| `MONGODB_URI`           | MongoDB connection string          | `dev`, `prod`      |
| `SPRING_PROFILES_ACTIVE`| Active Spring profile              | Optional (defaults to `local`) |

## Sample Data
Sample transaction data for testing imports is available in the `local_deployment/sample-data/` directory.

## API Documentation
Once the application is running, you can view the interactive API documentation (Swagger UI) by navigating to:
- http://localhost:8080/swagger-ui.html

## Additional Documentation
You can find more detailed documentation in the `docs/` directory:
- [API cURL Commands](docs/API_CURLS.md) - Sample requests for all API endpoints.
- [Gradle Commands](docs/GRADLE_COMMANDS.md) - Useful Gradle scripts to build, test, and run the project.
- [Architecture & Trade-offs](docs/TRANSACTION_TRADEOFFS.md) - Details regarding data management logic versus MongoDB transactions.
