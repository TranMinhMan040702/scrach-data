package com.criscode.greenshop.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VariantDto(
        Long id,
        String created_at,
        String updated_at,
        String created_by,
        String updated_by,
        String name,
        String sku,
        long quantity,
        BigDecimal item_price,
        BigDecimal total_price,
        BigDecimal promotional_item_price,
        BigDecimal total_promotional_price,
        String status,
        Long product_id
) {
}
