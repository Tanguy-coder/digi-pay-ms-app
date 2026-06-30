package net.tanguydev.customerservice.Domain.Validations;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Validations.Exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DomainCustomerValidator implements Validator<DomainCustomer>{

    @Override
    public void validate(DomainCustomer customer) {
        Map<String, String> errors = new HashMap<>();

        if (customer == null) {
            errors.put("domainCustomer", "domainCustomer cannot be null");
            throw new DomainValidationException(errors);
        }

        validateRequiredString(customer.getFirstName(), "firstName", "firstname is required", errors);
        validateRequiredString(customer.getLastName(), "lastName", "Le nom est obligatoire", errors);
        validateRequiredString(customer.getAddressLine1(), "addressLine1", "L'adresse est obligatoire", errors);
        validateRequiredString(customer.getCity(), "city", "La ville est obligatoire", errors);
        validateRequiredString(customer.getCountry(), "country", "Le pays est obligatoire", errors);

        // Validation Email
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            errors.put("email", "L'adresse email est obligatoire");
        } else if (!customer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "L'adresse email n'est pas valide");
        }

        // Validation Téléphone (Format E.164)
        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().matches("^\\+[1-9]\\d{1,14}$")) {
            errors.put("phoneNumber", "Le numéro de téléphone doit être au format international E.164 (ex: +22890000000)");
        }

        // Validation Nationalité (Exactement 3 caractères ISO)
        if (customer.getNationality() == null || customer.getNationality().trim().isEmpty()) {
            errors.put("nationality", "La nationalité est obligatoire");
        } else if (customer.getNationality().trim().length() != 3) {
            errors.put("nationality", "La nationalité doit comporter exactement 3 lettres (ex: TGO, SEN)");
        }

        // Validation Devise Préférée (Exactement 3 caractères)
        if (customer.getPreferredCurrency() == null || customer.getPreferredCurrency().trim().isEmpty()) {
            errors.put("preferredCurrency", "La devise préférée est obligatoire");
        } else if (customer.getPreferredCurrency().trim().length() != 3) {
            errors.put("preferredCurrency", "La devise doit comporter exactement 3 lettres (ex: XOF)");
        }

        // Validation des Enums Métier obligatoires
        if (customer.getStatus() == null) {
            errors.put("status", "Le statut du compte est obligatoire");
        }
        if (customer.getKycStatus() == null) {
            errors.put("kycStatus", "Le statut KYC est obligatoire");
        }
        if (customer.getTierLevel() == null) {
            errors.put("tierLevel", "Le niveau de tarification (Tier) est obligatoire");
        }

        // Validation du Risk Score (Entre 0.00 et 100.00)
        if (customer.getRiskScore() != null) {
            if (customer.getRiskScore().compareTo(BigDecimal.ZERO) < 0 || customer.getRiskScore().compareTo(new BigDecimal("100.00")) > 0) {
                errors.put("riskScore", "Le score de risque doit être compris entre 0.00 et 100.00");
            }
        }

        // Si des erreurs ont été collectées, on lève l'exception
        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
    }

    private void validateRequiredString(String value, String fieldName, String errorMessage, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put(fieldName, errorMessage);
        }
    }

}
