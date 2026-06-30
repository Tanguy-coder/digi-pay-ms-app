package net.tanguydev.customerservice.Domain.Entities;

import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public class DomainCustomer {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationality;
    private String addressLine1;
    private String city;
    private String country;
    private AccountStatus status;
    private KycStatus kycStatus;
    private OffsetDateTime kycVerifiedAt;
    private BigDecimal riskScore;
    private TierLevel tierLevel;
    private BigDecimal dailyLimit;
    private String preferredCurrency;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private String profilePictureUrl;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    public DomainCustomer() {
        super();
    }

    public DomainCustomer(Long id, String firstName, String lastName, String email, String phoneNumber, String nationality, String addressLine1, String city, String country, AccountStatus status, KycStatus kycStatus, OffsetDateTime kycVerifiedAt, BigDecimal riskScore, TierLevel tierLevel, BigDecimal dailyLimit, String preferredCurrency, Boolean isEmailVerified, Boolean isPhoneVerified, String profilePictureUrl, Map<String, Object> metadata, OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime deletedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nationality = nationality;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.country = country;
        this.status = status;
        this.kycStatus = kycStatus;
        this.kycVerifiedAt = kycVerifiedAt;
        this.riskScore = riskScore;
        this.tierLevel = tierLevel;
        this.dailyLimit = dailyLimit;
        this.preferredCurrency = preferredCurrency;
        this.isEmailVerified = isEmailVerified;
        this.isPhoneVerified = isPhoneVerified;
        this.profilePictureUrl = profilePictureUrl;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public OffsetDateTime getKycVerifiedAt() { return kycVerifiedAt; }
    public void setKycVerifiedAt(OffsetDateTime kycVerifiedAt) { this.kycVerifiedAt = kycVerifiedAt; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public TierLevel getTierLevel() { return tierLevel; }
    public void setTierLevel(TierLevel tierLevel) { this.tierLevel = tierLevel; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public String getPreferredCurrency() { return preferredCurrency; }
    public void setPreferredCurrency(String preferredCurrency) { this.preferredCurrency = preferredCurrency; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsPhoneVerified() { return isPhoneVerified; }
    public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
