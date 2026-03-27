package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardTestDataBuilder;
import com.algaworks.algashop.billing.domain.model.creditcard.GenerateInvoiceInputTestDataBuilder;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.algaworks.algashop.billing.infrastructure.listener.InvoiceEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.algaworks.algashop.billing.domain.model.invoice.InvoiceStatus.UNPAID;
import static com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentStatus.FAILED;
import static com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentStatus.PAID;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
class InvoiceManagementApplicationServiceIT {

    @Autowired
    private InvoiceManagementApplicationService applicationService;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @MockitoSpyBean
    private InvoicingService invoicingService;

    @MockitoBean
    private PaymentGatewayService  paymentGatewayService;

    @MockitoSpyBean
    private InvoiceEventListener invoiceEventListener;

    @Test
    public void shouldGenerateInvoice() {
        var customerId = UUID.randomUUID();
        var creditCard = CreditCardTestDataBuilder.aCreditCard().customerId(customerId).build();
        creditCardRepository.saveAndFlush(creditCard);

        var generateInvoiceInput = GenerateInvoiceInputTestDataBuilder.anInput().customerId(customerId).build();

        generateInvoiceInput.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .creditCardId(creditCard.getId())
                        .method(PaymentMethod.CREDIT_CARD)
                        .build()
        );

        var invoiceId = applicationService.generate(generateInvoiceInput);

        var invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(generateInvoiceInput.getOrderId());

        Mockito.verify(invoicingService).issue(any(), any(), any(), any());

        Mockito.verify(invoiceEventListener).listen(Mockito.any(InvoiceIssuedEvent.class));
    }

    @Test
    public void shouldGenerateInvoiceWithCreditCardAsPayment() {
        var customerId = UUID.randomUUID();
        var creditCard = CreditCardTestDataBuilder.aCreditCard().customerId(customerId).build();
        creditCardRepository.saveAndFlush(creditCard);

        var generateInvoiceInput = GenerateInvoiceInputTestDataBuilder.anInput().customerId(customerId).build();

        generateInvoiceInput.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .creditCardId(creditCard.getId())
                        .method(PaymentMethod.CREDIT_CARD)
                        .build()
        );

        var invoiceId = applicationService.generate(generateInvoiceInput);

        var invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(generateInvoiceInput.getOrderId());

        Assertions.assertThat(invoice.getVersion()).isEqualTo(0);
        Assertions.assertThat(invoice.getCreatedAt()).isNotNull();
        Assertions.assertThat(invoice.getCreatedByUserId()).isNotNull();

        Mockito.verify(invoicingService).issue(any(), any(), any(), any());
    }

    @Test
    public void shouldGenerateInvoiceWithGatewayBalanceAsPayment() {
        var customerId = UUID.randomUUID();

        var generateInvoiceInput = GenerateInvoiceInputTestDataBuilder.anInput().customerId(customerId).build();

        generateInvoiceInput.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .method(PaymentMethod.GATEWAY_BALANCE)
                        .build()
        );

        var invoiceId = applicationService.generate(generateInvoiceInput);

        var invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(generateInvoiceInput.getOrderId());

        Mockito.verify(invoicingService).issue(any(), any(), any(), any());
    }

    @Test
    public void shouldProcessInvoicePayment() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, null);
        invoiceRepository.saveAndFlush(invoice);

        var payment = Payment.builder()
                .gatewayCode("12345")
                .invoiceId(invoice.getId())
                .method(invoice.getPaymentSettings().getPaymentMethod())
                .status(PAID)
                .build();

        Mockito.when(paymentGatewayService.capture(Mockito.any(PaymentRequest.class))).thenReturn(payment);

        applicationService.processPayment(invoice.getId());

        var paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();

        Assertions.assertThat(paidInvoice.isPaid()).isTrue();

        Mockito.verify(paymentGatewayService).capture(Mockito.any(PaymentRequest.class));
        Mockito.verify(invoicingService).assignPayment(Mockito.any(Invoice.class), Mockito.any(Payment.class));

        Mockito.verify(invoiceEventListener).listen(Mockito.any(InvoicePaidEvent.class));
    }

    @Test
    public void shouldProcessInvoicePaymentAndCanceledInvoice() {
        var invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, null);
        invoiceRepository.saveAndFlush(invoice);

        var payment = Payment.builder()
                .gatewayCode("12345")
                .invoiceId(invoice.getId())
                .method(invoice.getPaymentSettings().getPaymentMethod())
                .status(FAILED)
                .build();

        Mockito.when(paymentGatewayService.capture(Mockito.any(PaymentRequest.class))).thenReturn(payment);

        applicationService.processPayment(invoice.getId());

        var paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();

        Assertions.assertThat(paidInvoice.isCanceled()).isTrue();

        Mockito.verify(paymentGatewayService).capture(Mockito.any(PaymentRequest.class));
        Mockito.verify(invoicingService).assignPayment(Mockito.any(Invoice.class), Mockito.any(Payment.class));

        Mockito.verify(invoiceEventListener).listen(Mockito.any(InvoiceCanceledEvent.class));
    }
}