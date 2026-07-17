package net.tanguydev.paymentservice.Domain.Validations;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Validations.Exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DomainPaymentValidator implements Validator<DomainPayment> {

    @Override
    public void validate(DomainPayment payment) {
        Map<String, String> errors = new HashMap<>();

        if (payment == null) {
            errors.put("payment", "Le paiement ne peut pas etre null");
            throw new DomainValidationException(errors);
        }

        if (payment.getSenderWalletId() == null)
            errors.put("senderWalletId", "Le wallet emetteur est obligatoire");

        if (payment.getReceiverWalletId() == null)
            errors.put("receiverWalletId", "Le wallet destinataire est obligatoire");

        if (payment.getSenderWalletId() != null && payment.getReceiverWalletId() != null
                && payment.getSenderWalletId().equals(payment.getReceiverWalletId()))
            errors.put("receiverWalletId", "Le wallet emetteur et destinataire ne peuvent pas etre identiques");

        if (payment.getAmount() == null)
            errors.put("amount", "Le montant est obligatoire");
        else if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Le montant doit etre superieur a zero");

        if (payment.getCurrency() == null || payment.getCurrency().isBlank())
            errors.put("currency", "La devise est obligatoire");
        else if (payment.getCurrency().trim().length() != 3)
            errors.put("currency", "La devise doit comporter exactement 3 caracteres (ex: XOF)");

        if (payment.getIdempotencyKey() == null || payment.getIdempotencyKey().isBlank())
            errors.put("idempotencyKey", "La cle d'idempotence est obligatoire");

        if (payment.getType() == null)
            errors.put("type", "Le type de paiement est obligatoire");

        if (!errors.isEmpty())
            throw new DomainValidationException(errors);
    }
}
