# API Endpoints & cURL Commands

This document contains sample `curl` requests for all available API methods in the CDQ Application. By default, the API runs locally on `http://localhost:8080`.

## 1. Import Operations

### Upload a CSV file
Uploads a CSV file containing bank transactions for asynchronous processing.
*(Note: Run this command from the root directory of the project, or provide an absolute path to the file)*
```bash
curl -X POST http://localhost:8080/api/v1/imports \
  -H "Content-Type: multipart/form-data" \
  -F "file=@./local_deployment/sample-data/transactions.csv"
```

### Check Import Job Status
Retrieves the real-time processing status of a specific import job using its `jobId`.
```bash
# Replace {jobId} with the ID returned by the upload endpoint
curl -X GET http://localhost:8080/api/v1/imports/{jobId} \
  -H "Accept: application/json"
```

### List All Import Jobs
Fetches a list of all import jobs and their statuses.
```bash
curl -X GET http://localhost:8080/api/v1/imports \
  -H "Accept: application/json"
```

---

## 2. Statistics Operations

### Get Statistics by Category
Retrieves transaction total values grouped by category for a specific month.
```bash
# Replace month query parameter (format: yyyy-MM)
curl -X GET "http://localhost:8080/api/v1/statistics/by-category?month=2026-04" \
  -H "Accept: application/json"
```

### Get Statistics by IBAN
Retrieves incoming and outgoing limits (total deposits, withdrawals, and counts) per each IBAN for a given month.
```bash
# Replace month query parameter (format: yyyy-MM)
curl -X GET "http://localhost:8080/api/v1/statistics/by-iban?month=2026-04" \
  -H "Accept: application/json"
```

### Get Statistics by Month Range
Fetches summarized transaction data across multiple months within a specified range.
```bash
# Replace from and to query parameters (format: yyyy-MM)
curl -X GET "http://localhost:8080/api/v1/statistics/by-month?from=2026-01&to=2026-04" \
  -H "Accept: application/json"
```
