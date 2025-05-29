package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.ImportTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportTaskRepository extends JpaRepository<ImportTask, Long> {
}
