package net.tanguydev.customerservice.Domain.Events;

import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import java.util.UUID;

public class CustomerEvent {
    private String eventType;
    private UUID customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationality;
    private String country;
    private AccountStatus status;
    private KycStatus kycStatus;
    private TierLevel tierLevel;
    private String preferredCurrency;

    public CustomerEvent() {}

    public CustomerEvent(String eventType, UUID customerId, String firstName, String lastName,
                         String email, String phoneNumber, String nationality, String country,
                         AccountStatus status, KycStatus kycStatus, TierLevel tierLevel,
                         String preferredCurrency) {
        this.eventType = eventType;
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nationality = nationality;
        this.country = country;
        this.status = status;
        this.kycStatus = kycStatus;
        this.tierLevel = tierLevel;
        this.preferredCurrency = preferredCurrency;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public TierLevel getTierLevel() { return tierLevel; }
    public void setTierLevel(TierLevel tierLevel) { this.tierLevel = tierLevel; }

    public String getPreferredCurrency() { return preferredCurrency; }
    public void setPreferredCurrency(String preferredCurrency) { this.preferredCurrency = preferredCurrency; }
}
