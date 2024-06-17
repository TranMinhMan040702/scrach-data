package com.criscode.greenshop.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductDto(
        String name,
        String short_description,
        String description,
        String code,
        BigDecimal quantity,
        BigDecimal actual_inventory,
        BigDecimal sold,
        Double rating,
        String slug,
        String status,
        Long product_category_id,
        Long brand_id,
        Long unit_id
) {
}
