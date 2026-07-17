package net.tanguydev.paymentservice.Domain.Entities;

import java.time.OffsetDateTime;

public class DomainIdempotencyKey {

    private String key;
    private int responseStatus;
    private String responseBody;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public DomainIdempotencyKey() {}

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
