package com.algaworks.algashop.billing.domain.model.invoice;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {

    @EqualsAndHashCode.Include
    private UUID id;
    private String orderId;
    private UUID customerId;

    private OffsetDateTime issuedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime cancelledAt;
    private OffsetDateTime expiredAt;

    private BigDecimal totalAmount;

    private InvoiceStatus status;

    private PaymentSettings paymentSettings;

    private Set<LineItem> items = new HashSet<>();

    private Payer payer;

    private String cancelReason;

    public Set<LineItem> getItems() {
        return Collections.unmodifiableSet(this.items);
    }

    public void markAsPaid() {
        this.paidAt = OffsetDateTime.now();
    }

    public void markAsCancelled() {
        this.cancelledAt = OffsetDateTime.now();
    }

    public void markAsExpired() {
        this.expiredAt = OffsetDateTime.now();
    }

    public void assignPaymentGatewayCode(String code) {

    }

    public void changePaymentSettings(PaymentMethod method, UUID creditCard) {

    }
}
