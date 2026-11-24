package cd.ucc.proximo.controller;

import cd.ucc.proximo.domain.entity.Order;
import cd.ucc.proximo.domain.entity.OrderItem;
import cd.ucc.proximo.domain.entity.Shop;
import cd.ucc.proximo.domain.enums.CancellationReason;
import cd.ucc.proximo.domain.enums.OrderStatus;
import cd.ucc.proximo.dto.CreateOrderRequest;
import cd.ucc.proximo.dto.OrderItemResponse;
import cd.ucc.proximo.dto.OrderResponse;
import cd.ucc.proximo.exception.ResourceNotFoundException;
import cd.ucc.proximo.repository.OrderRepository;
import cd.ucc.proximo.repository.ShopRepository;
import cd.ucc.proximo.security.UserPrincipal;
import cd.ucc.proximo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository, ShopRepository shopRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody CreateOrderRequest req) {
        Order order = orderService.createOrder(principal.getId(), req.shopId(), req.auditoireId(), req.items());
        return ResponseEntity.ok(mapToResponse(order));
    }

    @GetMapping("/my-orders")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(principal.getId());
        return ResponseEntity.ok(orders.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/vendor/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getActiveOrdersForVendor(@AuthenticationPrincipal UserPrincipal principal) {
        Shop shop = shopRepository.findByOwnerId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vous n'avez pas de boutique enregistrée."));

        List<Order> orders = orderRepository.findActiveOrdersForVendorGroupedByAuditoire(shop.getId(), OrderStatus.EN_ATTENTE);
        return ResponseEntity.ok(orders.stream().map(this::mapToResponse).toList());
    }

    @PostMapping("/{id}/confirm-paid")
    public ResponseEntity<OrderResponse> confirmPayment(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id) {
        Order order = orderService.confirmPaymentAndDelivery(id, principal.getId());
        return ResponseEntity.ok(mapToResponse(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable Long id,
                                                     @RequestParam(required = false) CancellationReason reason) {
        Order order = orderService.cancelOrder(id, principal.getId(), reason);
        return ResponseEntity.ok(mapToResponse(order));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItemToResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getBuyer().getId(),
                order.getBuyer().getFullName(),
                order.getShop().getId(),
                order.getShop().getName(),
                order.getDeliveryAuditoire().getId(),
                order.getDeliveryAuditoire().getCode(),
                order.getDeliveryAuditoire().getName(),
                order.getDeliveryAuditoire().getLevel(),
                order.getDeliveryAuditoire().getPositionOrder(),
                order.getStatus(),
                order.getCancellationReason(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPhotoUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
