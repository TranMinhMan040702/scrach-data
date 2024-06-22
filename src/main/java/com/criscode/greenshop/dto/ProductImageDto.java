package com.criscode.greenshop.dto;

import lombok.Builder;

@Builder
public record ProductImageDto(
        Long id,
        String created_at,
        String updated_at,
        String created_by,
        String updated_by,
        String image,
        Double size,
        String content_type,
        boolean is_default,
        Long product_id
) {
}
