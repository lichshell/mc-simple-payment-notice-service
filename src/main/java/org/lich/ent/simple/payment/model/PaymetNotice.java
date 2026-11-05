package org.lich.ent.simple.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;

/**
 * Simple payment note. The Confluent JSON Schema serializer derives the
 * JSON Schema from this class and registers it as subject "payments-json-value".
 * Plain POJO (not a record) keeps schema generation predictable.
 */
public class PaymetNotice {

    @JsonProperty(required = true)
    @JsonPropertyDescription("Unique payment identifier")
    private String paymentId;

    @JsonProperty(required = true)
    private String payer;

    @JsonProperty(required = true)
    private String payee;

    @JsonProperty(required = true)
    private BigDecimal amount;

    @JsonProperty(required = true)
    private String currency;

    @JsonProperty
    private String note;

    @JsonProperty
    private String createdAt;   // ISO-8601 string keeps the schema simple

    public PaymetNotice() { }

    public PaymetNotice(String paymentId, String payer, String payee,
                        BigDecimal amount, String currency, String note, String createdAt) {
        this.paymentId = paymentId;
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
        this.currency = currency;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getPayer() { return payer; }
    public void setPayer(String payer) { this.payer = payer; }
    public String getPayee() { return payee; }
    public void setPayee(String payee) { this.payee = payee; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "PaymetNotice{paymentId='" + paymentId + "', payer='" + payer + "', payee='" + payee
                + "', amount=" + amount + " " + currency + ", note='" + note + "'}";
    }
}
