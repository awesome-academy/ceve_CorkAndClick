package com.sun.wineshop.job;

import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductCleanupJob {

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldDeletedProducts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Product> productsToDelete = productRepository.findByDeletedAtBefore(thirtyDaysAgo);

        if (!productsToDelete.isEmpty()) {
            productRepository.deleteAll(productsToDelete);
            System.out.println("Deleted " + productsToDelete.size() + " old soft-deleted products.");
        }
    }
}
