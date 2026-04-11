package com.shop.user_service.service;

import com.shop.user_service.entity.Plan;
import java.util.List;

public interface PlanService {
    List<Plan> getAllPlans();
    Plan getPlanById(Long id);
    Plan createPlan(Plan plan);
    Plan updatePlan(Long id, Plan plan);
    void deletePlan(Long id);
}
