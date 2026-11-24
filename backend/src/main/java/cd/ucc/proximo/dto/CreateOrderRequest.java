package cd.ucc.proximo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "L'ID de la boutique est requis")
        Long shopId,

        @NotNull(message = "L'ID de l'auditoire de livraison est requis")
        Long auditoireId,

        @NotEmpty(message = "La commande doit comporter au moins un article")
        @Valid
        List<OrderItemRequest> items
) {
}
