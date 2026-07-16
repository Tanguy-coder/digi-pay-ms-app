package net.tanguydev.walletservice.Infrastructure.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class WalletRequest {

    @NotNull(message = "Le customerId est obligatoire")
    private Long customerId;

    @NotBlank(message = "La devise est obligatoire")
    @Size(min = 3, max = 3, message = "La devise doit faire exactement 3 caracteres (ex: XOF)")
    private String currency;
}
