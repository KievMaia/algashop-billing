package com.algaworks.algashop.billing.domain.model.invoice;

import com.algaworks.algashop.billing.domain.model.FieldValidations;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Address {
    public String street;
    public String number;
    public String complement;
    public String neighborhood;
    public String city;
    public String state;
    public String zipCode;

    @Builder
    public Address(final String street,
                   final String number,
                   final String complement,
                   final String neighborhood,
                   final String city,
                   final String state,
                   final String zipCode) {
        FieldValidations.requiresNonBlank(street);
        FieldValidations.requiresNonBlank(number);
        FieldValidations.requiresNonBlank(neighborhood);
        FieldValidations.requiresNonBlank(city);
        FieldValidations.requiresNonBlank(state);
        FieldValidations.requiresNonBlank(zipCode);
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
}
