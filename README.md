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
2. Navigate back to the root directory and run the Spring Boot application using the Gradle wrapper:
   ```bash
   cd .. && ./gradlew bootRun
   ```

By default, the application will be available at `http://localhost:8080` and will connect to the local MongoDB instance at `mongodb://localhost:27017/bank-transactions`.

### Option 2: Run entirely with Docker Compose
If you want to run the entire stack (both the Java application and MongoDB) in Docker containers without needing Java installed on your host machine:

1. Navigate to the local deployment directory and build and start the containers:
   ```bash
   cd local_deployment && docker-compose -f docker-compose-app.yml up --build -d
   ```

## Configuration

The application is configured to use environment variables for key settings. The most important one is the MongoDB connection URI.

- **`MONGODB_URI`**: The connection string for MongoDB. 
  - Default: `mongodb://localhost:27017/bank-transactions` (when running via Gradle) or `mongodb://mongo:27017/bank-transactions` (when running via `docker-compose-app.yml`).

To override it locally, you can pass the environment variable before the run command:
```bash
MONGODB_URI="mongodb://your-custom-host:27017/your-db" ./gradlew bootRun
```

## Sample Data
Sample transaction data for testing imports is available in the `local_deployment/sample-data/` directory.

## API Documentation
Once the application is running, you can view the interactive API documentation (Swagger UI) by navigating to:
- http://localhost:8080/swagger-ui.html
