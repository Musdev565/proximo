package cd.ucc.proximo.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String photoUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
