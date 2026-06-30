package net.tanguydev.customerservice.Infrastructure.Requests;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SuppressWarnings("unused")
public class CustomerRequest {
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
    private String email;

    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Le numéro de téléphone doit être au format E.164")
    private String phoneNumber;

    @NotBlank(message = "La nationalité est obligatoire")
    @Size(min = 3, max = 3, message = "La nationalité doit faire exactement 3 caractères (ex: TGO)")
    private String nationality;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String addressLine1;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String city;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
    private String country;

    @NotBlank(message = "La devise préférée est obligatoire")
    @Size(min = 3, max = 3, message = "La devise doit faire exactement 3 caractères (ex: XOF)")
    private String preferredCurrency;

    private String profilePictureUrl;
    private Map<String, Object> metadata;

}
