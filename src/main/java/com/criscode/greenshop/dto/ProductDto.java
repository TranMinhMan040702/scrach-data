package com.criscode.greenshop.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductDto(
        Long id,
        String created_at,
        String updated_at,
        String created_by,
        String updated_by,
        String name,
        String short_description,
        String description,
        String code,
        Long quantity,
        Long actual_inventory,
        Long sold,
        BigDecimal cost,
        Double rating,
        String slug,
        String status,
        Long product_category_id,
        Long brand_id,
        Long unit_id
) {
}
