package cd.ucc.proximo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShopRequest(
        @NotBlank(message = "Le nom de la boutique est requis")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String name,

        @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
        String description,

        String imageUrl
) {
}
