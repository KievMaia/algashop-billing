package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.FieldValidations;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineItem {
    private Integer number;
    private String name;

    private BigDecimal amount;

    @Builder
    public LineItem(final Integer number, final String name, final BigDecimal amount) {
        FieldValidations.requiresNonBlank(name);
        Objects.requireNonNull(number);
        Objects.requireNonNull(amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if(number <= 0) {
            throw new IllegalArgumentException("Number must be positive");
        }

        this.number = number;
        this.name = name;
        this.amount = amount;
    }
}
