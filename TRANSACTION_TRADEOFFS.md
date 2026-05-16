# Architectural Decision Record: MongoDB Transactions vs Logical Rollbacks

## Context

During the implementation of the CSV import feature, we faced a decision on how to handle potential failures when processing large files. The file is read and inserted into MongoDB in chunks (e.g., 1,000 rows at a time) to keep memory usage low. However, if a failure occurs (e.g., malformed row, database connection drop) after some chunks have already been inserted, the database will be left in an inconsistent state (partial import).

## Considered Alternatives

### 1. Using `@Transactional` (Multi-Document Transactions)
Spring Data MongoDB supports the standard `@Transactional` annotation for multi-document ACID transactions. 

**Pros:**
* Clean, declarative code. Spring handles the commit/rollback automatically.

**Cons:**
* **Infrastructure Requirements:** MongoDB strictly requires a **Replica Set** environment to support multi-document transactions. Running a local single-node MongoDB instance for development or testing would require complex replica set initialization.
* **Resource Intensive:** Holding a transaction open while parsing and validating a large CSV file line-by-line locks resources. Huge batch inserts inside a single transaction can cause significant memory pressure on the database server and impact concurrent operations.
* **Size Limitations:** MongoDB has built-in limitations on the size of transaction operations (e.g., the 16MB BSON document size limit applies to the oplog entry representing the transaction in older versions, and long-running transactions can be aborted by the server).

### 2. Manual Logical Rollbacks (Chosen Approach)
Instead of relying on database-level transactions, we manage consistency at the application level.

How it works:
1. Every imported `Transaction` document is tagged with its parent `importJobId`.
2. The file is processed and inserted in chunks.
3. If any fatal exception occurs during the chunk processing loop, the `catch` block performs a **logical rollback**.
4. It executes a single `delete` query matching the current `importJobId`, wiping any previously successful chunks from the failure-struck job.

**Pros:**
* **Works Anywhere:** Fully functional on standalone MongoDB instances, making local development and lightweight deployments trivial.
* **Highly Scalable:** Chunking avoids massive transaction locks and memory overhead, allowing for very large CSV files to be processed safely.
* **Self-Healing:** Because the `importJobId` acts as a unique scope, a rollback only touches the faulty job's data without blocking or affecting other concurrent imports or queries.

**Cons:**
* Requires explicit error handling logic in the service layer.
* Temporary visibility of partial data: During the brief window before a failure triggers the rollback, concurrent queries might see a partial dataset. For asynchronous reporting tasks, this trade-off is highly acceptable.

## Conclusion

We elected to use the **Logical Rollback** approach. The architectural trade-off leans heavily in favor of scalability, lower infrastructure requirements, and reduced memory pressure, making it the robust choice for bulk data import scenarios.
