package cd.ucc.proximo.dto;

import cd.ucc.proximo.domain.enums.CancellationReason;
import cd.ucc.proximo.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long buyerId,
        String buyerName,
        Long shopId,
        String shopName,
        Long auditoireId,
        String auditoireCode,
        String auditoireName,
        Integer auditoireLevel,
        Integer auditoirePosition,
        OrderStatus status,
        CancellationReason cancellationReason,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {
}
