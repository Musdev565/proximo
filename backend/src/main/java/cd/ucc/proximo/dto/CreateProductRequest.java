package cd.ucc.proximo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Le nom du produit est requis")
        String name,

        @NotNull(message = "Le prix est requis")
        @DecimalMin(value = "100.00", message = "Le prix minimum est de 100 FC")
        BigDecimal price,

        String photoUrl
) {
}
