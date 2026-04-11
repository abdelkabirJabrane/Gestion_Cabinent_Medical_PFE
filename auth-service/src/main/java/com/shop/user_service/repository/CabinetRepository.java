package com.shop.user_service.repository;

import com.shop.user_service.entity.Cabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CabinetRepository extends JpaRepository<Cabinet, Long> {
    boolean existsByEmail(String email);
    List<Cabinet> findByStatut(String statut);
}
