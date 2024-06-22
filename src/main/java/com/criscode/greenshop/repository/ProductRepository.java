package com.criscode.greenshop.repository;

import com.criscode.greenshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query(value = "SELECT MAX(t1.id) FROM Product t1")
    Long findMaxId();

    boolean existsByName(String name);

}