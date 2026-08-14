package com.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment.model.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String>{

}
