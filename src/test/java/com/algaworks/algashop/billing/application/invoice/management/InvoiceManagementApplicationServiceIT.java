package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardTestDataBuilder;
import com.algaworks.algashop.billing.domain.model.creditcard.GenerateInvoiceInputTestDataBuilder;
import com.algaworks.algashop.billing.domain.model.invoice.Invoice;
import com.algaworks.algashop.billing.domain.model.invoice.InvoiceRepository;
import com.algaworks.algashop.billing.domain.model.invoice.InvoicingService;
import com.algaworks.algashop.billing.domain.model.invoice.PaymentMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.algaworks.algashop.billing.domain.model.invoice.InvoiceStatus.UNPAID;
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
}