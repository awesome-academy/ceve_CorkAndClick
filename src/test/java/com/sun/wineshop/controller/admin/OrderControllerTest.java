package com.sun.wineshop.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({
        OrderControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource source) {
            return new MessageUtil(source);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getOrders_shouldReturnPagedOrders() throws Exception {
        OrderDetailResponse order1 = sampleOrder(1L, OrderStatus.PENDING);
        OrderDetailResponse order2 = sampleOrder(2L, OrderStatus.DELIVERED);
        Page<OrderDetailResponse> page = new PageImpl<>(List.of(order1, order2));

        when(orderService.getOrders(null, PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.Order.BASE)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].orderId").value(1L))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage("orders.fetched.success")));
    }

    @Test
    void getOrderById_shouldReturnOrderDetail() throws Exception {
        OrderDetailResponse order = sampleOrder(1L, OrderStatus.DELIVERING);

        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.Order.BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.status").value("DELIVERING"));
    }

    @Test
    void updateOrderStatus_shouldReturnUpdatedOrder() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.REJECTED, "Out of stock");
        OrderDetailResponse updatedOrder = sampleOrder(1L, OrderStatus.REJECTED);

        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class)))
                .thenReturn(updatedOrder);

        mockMvc.perform(MockMvcRequestBuilders.put(AdminApiPaths.Order.BASE + "/1/updateStatus")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.status").value(OrderStatus.REJECTED.name()));
    }

    private OrderDetailResponse sampleOrder(Long id, OrderStatus status) {
        return new OrderDetailResponse(
                id,
                100L,
                "Rio Phan",
                "123 Ly Thuong Kiet St",
                "0123456789",
                status.name(),
                status.name().equals(OrderStatus.REJECTED.name()) ? "Out of stock" : null,
                150000.0,
                LocalDateTime.now(),
                List.of()
        );
    }
}
