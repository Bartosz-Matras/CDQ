package com.bartoszmatras.cdq.repository;

import com.bartoszmatras.cdq.model.ImportJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobRepository extends MongoRepository<ImportJob, String> {
}