package com.idamart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Change how many of an item are in the cart. */
public record QuantityRequest(

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 10, message = "You can buy at most 10 of one item")
        Integer quantity
) {
}
