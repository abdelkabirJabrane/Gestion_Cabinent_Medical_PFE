package com.shop.user_service.repository;

import com.shop.user_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    boolean existsByPlanIdIgnoreCase(String planId);
    Optional<Plan> findByPlanIdIgnoreCase(String planId);
}
