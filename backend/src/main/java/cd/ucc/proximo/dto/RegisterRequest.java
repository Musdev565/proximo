package cd.ucc.proximo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom complet est requis")
        String fullName,

        @NotBlank(message = "L'adresse email est requise")
        @Email(message = "Format d'email invalide")
        String email,

        String phone,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String password,

        Long defaultAuditoireId
) {
}
