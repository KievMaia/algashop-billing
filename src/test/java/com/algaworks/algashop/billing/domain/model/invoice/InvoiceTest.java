package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.DomainException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class InvoiceTest {

    @Test
    public void shouldCreateInvoiceIssue() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        var orderId = invoice.getOrderId();
        var customerId = invoice.getCustomerId();
        var payer = invoice.getPayer();
        var items = invoice.getItems();

        var invoiceIssue = Invoice.issue(orderId, customerId, payer, items);

        Assertions.assertThat(invoiceIssue).isNotNull();
    }

    @Test
    public void shouldMarkInvoiceAsPaid() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsPaid();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    public void shouldMarkAsCanceled() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsCancelled("Canceled");

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELED);
        Assertions.assertThat(invoice.getCancelledAt()).isNotNull();
        Assertions.assertThat(invoice.getCancelReason()).isEqualTo("Canceled");
    }

    @Test
    public void shouldChangePaymentSettings() {
        var creditCardId = UUID.randomUUID();
        var invoice = InvoiceTestDataBuilder.anInvoice()
                .paymentSettings(PaymentMethod.GATEWAY_BALANCE, creditCardId)
                .build();

        invoice.changePaymentSettings(PaymentMethod.CREDIT_CARD, UUID.randomUUID());

        Assertions.assertThat(invoice.getPaymentSettings().getPaymentMethod()).isNotEqualTo(PaymentMethod.GATEWAY_BALANCE);
        Assertions.assertThat(invoice.getPaymentSettings().getCreditCardId()).isNotEqualTo(creditCardId);
    }

    @Test
    public void shouldAttributeGatewayCodeCorrectly() {
        var invoice = InvoiceTestDataBuilder.anInvoice().paymentSettings(PaymentMethod.CREDIT_CARD, UUID.randomUUID()).build();
        invoice.assignPaymentGatewayCode("123");

        Assertions.assertThat(invoice.getPaymentSettings().getGatewayCode()).isNotNull();
        Assertions.assertThat(invoice.getPaymentSettings().getGatewayCode()).isEqualTo("123");
    }

    @Test
    public void shouldThrowExceptionWhenCreateInvoiceWithEmptyItems() {
        Assertions.assertThatThrownBy(() -> InvoiceTestDataBuilder.anInvoice()
                .items(new HashSet<>())
                .build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSetPaidInvoiceAlreadyCanceled() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsCancelled("Canceled");

        Assertions.assertThatThrownBy(invoice::markAsPaid).isInstanceOf(DomainException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSetCanceledInvoiceAlreadyCanceled() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsCancelled("Canceled");

        Assertions.assertThatThrownBy(
                () -> invoice.markAsCancelled("Cancelar")
        ).isInstanceOf(DomainException.class);
    }

    @Test
    public void shouldThrowExceptionWhenTryChangePaymentSettingsOfAlreadyPaidInvoice() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsPaid();

        Assertions.assertThatThrownBy(
                () -> invoice.changePaymentSettings(PaymentMethod.CREDIT_CARD, UUID.randomUUID())
        ).isInstanceOf(DomainException.class);
    }

    @Test
    public void shouldThrowExceptionWhenTryChangeGatewayCodeOfAlreadyPaidInvoice() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.markAsPaid();

        Assertions.assertThatThrownBy(
                () -> invoice.assignPaymentGatewayCode("123")
        ).isInstanceOf(DomainException.class);
    }

    @Test
    public void shouldThrowExceptionWhenTryChangItemsList() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();

        Assertions.assertThatThrownBy(
                () -> invoice.getItems().clear()
        ).isInstanceOf(UnsupportedOperationException.class);
    }
}