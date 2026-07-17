package net.tanguydev.paymentservice.Infrastructure.Requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class InitiatePaymentRequest {

    @NotNull(message = "Le wallet emetteur est obligatoire")
    private UUID senderWalletId;

    @NotNull(message = "Le wallet destinataire est obligatoire")
    private UUID receiverWalletId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit etre superieur a zero")
    private BigDecimal amount;

    @NotBlank(message = "La devise est obligatoire")
    @Size(min = 3, max = 3, message = "La devise doit faire exactement 3 caracteres (ex: XOF)")
    private String currency;

    @NotNull(message = "Le type de paiement est obligatoire")
    private PaymentType type;

    @NotBlank(message = "La cle d'idempotence est obligatoire")
    @Size(max = 100)
    private String idempotencyKey;

    @Size(max = 500)
    private String description;

    private UUID merchantId;
}
