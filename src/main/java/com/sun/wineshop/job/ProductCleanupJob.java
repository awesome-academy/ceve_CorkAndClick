package com.sun.wineshop.job;

import com.sun.wineshop.model.entity.CartItem;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CartItemRepository;
import com.sun.wineshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCleanupJob {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Run at 3 am every day
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldDeletedProductsFromCarts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Product> oldDeletedProducts = productRepository.findByDeletedAtBefore(thirtyDaysAgo);

        if (!oldDeletedProducts.isEmpty()) {
            List<Long> productIds = oldDeletedProducts.stream()
                    .map(Product::getId)
                    .toList();

            List<CartItem> itemsToRemove = cartItemRepository.findByProductIdIn(productIds);

            if (!itemsToRemove.isEmpty()) {
                cartItemRepository.deleteAll(itemsToRemove);
                log.info("Removed " + itemsToRemove.size() + " cart items with old soft-deleted products.");
            }
        }
    }
}
