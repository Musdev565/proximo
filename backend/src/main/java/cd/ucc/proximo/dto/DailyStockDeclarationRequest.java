package cd.ucc.proximo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DailyStockDeclarationRequest(
        @NotNull(message = "L'ID du produit est requis")
        Long productId,

        @Min(value = 0, message = "La quantité de stock ne peut pas être négative")
        int stockQuantity
) {
}
