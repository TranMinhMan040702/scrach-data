package com.criscode.greenshop.repository;

import com.criscode.greenshop.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>,
        JpaSpecificationExecutor<ProductCategory> {

    ProductCategory findProductCategoryBySlug(String slug);

}