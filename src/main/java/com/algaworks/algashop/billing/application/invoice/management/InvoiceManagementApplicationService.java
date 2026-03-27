package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceManagementApplicationService {

    private final PaymentGatewayService paymentGatewayService;
    private final InvoicingService invoicingService;
    private final InvoiceRepository invoiceRepository;
    private final CreditCardRepository creditCardRepository;

    @Transactional
    public UUID generate(GenerateInvoiceInput input) {
        var paymentSettings = input.getPaymentSettings();
        verifyCreditCardId(paymentSettings.getCreditCardId(), input.getCustomerId());

        var payer = converToPayer(input.getPayer());
        var items = convertToLineItems(input.getItems());

        var invoice = invoicingService.issue(input.getOrderId(), input.getCustomerId(), payer, items);
        invoice.changePaymentSettings(paymentSettings.getMethod(), paymentSettings.getCreditCardId());

        invoiceRepository.saveAndFlush(invoice);

        return invoice.getId();
    }

    @Transactional
    public void processPayment(UUID invoiceId) {
        var invoice = invoiceRepository.findById(invoiceId).orElseThrow(InvoiceNotFoundException::new);
        var paymentRequest = toPaymentRequest(invoice);

        Payment payment;
        try {
            payment = paymentGatewayService.capture(paymentRequest);
        } catch (Exception e) {
            var paymentCaptureFailed = "Payment capture failed";
            log.error(paymentCaptureFailed);
            invoice.markAsCancelled(paymentCaptureFailed);
            invoiceRepository.saveAndFlush(invoice);
            return;
        }
        invoicingService.assignPayment(invoice, payment);
        invoiceRepository.saveAndFlush(invoice);
    }

    private PaymentRequest toPaymentRequest(final Invoice invoice) {
        return PaymentRequest.builder()
                .amount(invoice.getTotalAmount())
                .method(invoice.getPaymentSettings().getPaymentMethod())
                .creditCardId(invoice.getPaymentSettings().getCreditCardId())
                .payer(invoice.getPayer())
                .invoiceId(invoice.getId())
                .build();
    }

    private Set<LineItem> convertToLineItems(Set<LineItemInput> items) {
        Set<LineItem> lineItems = new LinkedHashSet<>();
        int itemNumber = 1;
        for (LineItemInput item : items) {
            lineItems.add(LineItem.builder()
                                  .number(itemNumber++)
                                  .name(item.getName())
                                  .amount(item.getAmount())
                                  .build());
        }
        return lineItems;
    }

    private Payer converToPayer(PayerData payerData) {
        var addressData = payerData.getAddress();

        return Payer.builder()
                .fullName(payerData.getFullName())
                .email(payerData.getEmail())
                .document(payerData.getDocument())
                .phone(payerData.getPhone())
                .address(Address.builder()
                                 .city(addressData.getCity())
                                 .state(addressData.getState())
                                 .neighborhood(addressData.getNeighborhood())
                                 .complement(addressData.getComplement())
                                 .zipCode(addressData.getZipCode())
                                 .street(addressData.getStreet())
                                 .number(addressData.getNumber())
                                 .build())
                .build();
    }

    private void verifyCreditCardId(UUID creditCardId, @NonNull UUID customerId) {
        if (creditCardId != null && !creditCardRepository.existsByIdAndCustomerId(creditCardId, customerId)) {
            throw new CreditCardNotFoundException();
        }
    }
}
