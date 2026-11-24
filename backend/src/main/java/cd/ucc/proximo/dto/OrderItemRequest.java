package cd.ucc.proximo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "L'ID du produit est requis")
        Long productId,

        @Min(value = 1, message = "La quantité doit être supérieure ou égale à 1")
        int quantity
) {
}
