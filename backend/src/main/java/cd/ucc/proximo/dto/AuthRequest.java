package cd.ucc.proximo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "L'adresse email est requise")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est requis")
        String password
) {
}
