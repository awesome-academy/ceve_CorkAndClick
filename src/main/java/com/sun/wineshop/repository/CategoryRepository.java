package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Category> findByName(String name);
}
