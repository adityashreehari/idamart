package com.idamart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Place an order using everything currently in the cart. */
public record CheckoutRequest(

        @NotBlank(message = "Shipping address is required")
        @Size(min = 10, max = 500, message = "Shipping address must be 10 to 500 characters")
        String shippingAddress
) {
}
