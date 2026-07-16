package net.tanguydev.walletservice.Domain.Validations;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Validations.Exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DomainWalletValidator implements Validator<DomainWallet> {

    @Override
    public void validate(DomainWallet wallet) {
        Map<String, String> errors = new HashMap<>();

        if (wallet == null) {
            errors.put("wallet", "Le wallet ne peut pas etre null");
            throw new DomainValidationException(errors);
        }

        if (wallet.getCustomerId() == null) {
            errors.put("customerId", "Le customerId est obligatoire");
        }

        if (wallet.getCurrency() == null || wallet.getCurrency().trim().isEmpty()) {
            errors.put("currency", "La devise est obligatoire");
        } else if (wallet.getCurrency().trim().length() != 3) {
            errors.put("currency", "La devise doit comporter exactement 3 caracteres (ex: XOF)");
        }

        if (wallet.getBalance() == null) {
            errors.put("balance", "Le solde est obligatoire");
        } else if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("balance", "Le solde ne peut pas etre negatif");
        }

        if (wallet.getFrozenAmount() == null) {
            errors.put("frozenAmount", "Le montant gele est obligatoire");
        } else if (wallet.getFrozenAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("frozenAmount", "Le montant gele ne peut pas etre negatif");
        }

        if (wallet.getBalance() != null && wallet.getFrozenAmount() != null) {
            if (wallet.getFrozenAmount().compareTo(wallet.getBalance()) > 0) {
                errors.put("frozenAmount", "Le montant gele ne peut pas depasser le solde");
            }
        }

        if (wallet.getStatus() == null) {
            errors.put("status", "Le statut du wallet est obligatoire");
        }

        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
    }
}
