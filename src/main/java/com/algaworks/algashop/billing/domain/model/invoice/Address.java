package com.algaworks.algashop.billing.domain.model.invoice;

import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {
    public String street;
    public String number;
    public String complement;
    public String neighborhood;
    public String city;
    public String state;
    public String zipCode;
}
