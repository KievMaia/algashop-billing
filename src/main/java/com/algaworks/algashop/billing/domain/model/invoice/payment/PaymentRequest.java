package com.algaworks.algashop.billing.domain.model.invoice.payment;

import com.algaworks.algashop.billing.domain.model.invoice.Payer;
import com.algaworks.algashop.billing.domain.model.invoice.PaymentMethod;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Builder
public class PaymentRequest {
    private PaymentMethod method;
    private BigDecimal amount;
    private UUID invoiceId;
    private UUID creditCardId;
    private Payer payer;

    public PaymentRequest(final PaymentMethod method,
                          final BigDecimal amount,
                          final UUID invoiceId,
                          final UUID creditCardId,
                          final Payer payer) {
        Objects.requireNonNull(method, "method cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(payer, "payer cannot be null");
        Objects.requireNonNull(invoiceId, "invoiceId cannot be null");

        if (method.equals(PaymentMethod.CREDIT_CARD)) {
            Objects.requireNonNull(creditCardId, "creditCardId cannot be null");
        }

        this.method = method;
        this.amount = amount;
        this.invoiceId = invoiceId;
        this.creditCardId = creditCardId;
        this.payer = payer;
    }
}
