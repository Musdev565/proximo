package cd.ucc.proximo.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long shopId,
        String name,
        BigDecimal price,
        String photoUrl,
        boolean active,
        int availableStock,
        int stockQuantity,
        int reservedQuantity
) {
}
