package com.algaworks.algashop.billing.domain.model.creditcard;

import com.algaworks.algashop.billing.domain.model.IdGenerator;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditCard {
    @EqualsAndHashCode.Include
    private UUID id;
    private OffsetDateTime createdAt;
    private UUID customerId;

    private String lastNumbers;
    private String brand;
    private Integer expirationMonth;
    private Integer expirationYear;

    private String gatewayCode;

    public static CreditCard brandNew(UUID customerId,
                                      String lastNumbers,
                                      String brand,
                                      Integer expirationMonth,
                                      Integer expirationYear,
                                      String gatewayCodeCreditCardCode) {

        Objects.requireNonNull(customerId);
        Objects.requireNonNull(expirationMonth);
        Objects.requireNonNull(expirationYear);

        if (StringUtils.isAnyBlank(lastNumbers, brand, gatewayCodeCreditCardCode)) {
            throw new IllegalArgumentException("Last numbers cannot be blank");
        }

        return new CreditCard(
                IdGenerator.generateTimeBasedUUID(),
                OffsetDateTime.now(),
                customerId,
                lastNumbers,
                brand,
                expirationMonth,
                expirationYear,
                gatewayCodeCreditCardCode
        );
    }

    public void setGatewayCode(String gatewayCode) {
        if (StringUtils.isBlank(gatewayCode)) throw new IllegalArgumentException("Gateway code cannot be blank");
        this.gatewayCode = gatewayCode;
    }
}
