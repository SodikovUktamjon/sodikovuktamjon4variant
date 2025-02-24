package com.example.repositories;

import com.example.domains.Order;
import com.example.domains.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_ChatId(String chatId);
}