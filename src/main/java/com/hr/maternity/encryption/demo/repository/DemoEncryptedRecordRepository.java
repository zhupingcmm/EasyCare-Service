package com.hr.maternity.encryption.demo.repository;

import com.hr.maternity.encryption.demo.entity.DemoEncryptedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DemoEncryptedRecordRepository extends JpaRepository<DemoEncryptedRecord, UUID> {
}
