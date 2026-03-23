package com.algaworks.algashop.billing.domain.model.invoice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

class InvoiceTest {

    @Test
    public void shouldCreateInvoiceIssue() {
        var address = Address.builder()
                .street("rua a")
                .state("california")
                .number("123")
                .city("Berlin")
                .zipCode("12345")
                .neighborhood("12314546")
                .build();
        var payer = Payer.builder()
                .fullName("Kiev Maia")
                .phone("123456789")
                .email("kievmaia@gmail.com")
                .document("123456")
                .address(address)
                .build();
        var item = LineItem.builder()
                .name("Kiev Maia")
                .amount(BigDecimal.ONE)
                .number(2)
                .build();
        var invoice = Invoice.issue("123", UUID.randomUUID(), payer, Set.of(item));

        Assertions.assertThat(invoice).isNotNull();
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
}