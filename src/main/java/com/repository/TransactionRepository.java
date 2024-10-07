package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.entity.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer>{

}
