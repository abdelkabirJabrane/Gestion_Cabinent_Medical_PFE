package com.shop.user_service.service;

import com.shop.user_service.entity.Plan;
import com.shop.user_service.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan introuvable avec l'ID: " + id));
    }

    @Override
    public Plan createPlan(Plan plan) {
        if (planRepository.existsByPlanIdIgnoreCase(plan.getPlanId())) {
            throw new IllegalArgumentException("Un plan avec l'identifiant '" + plan.getPlanId() + "' existe déjà.");
        }
        log.info("Creating plan: {}", plan.getPlanId());
        return planRepository.save(plan);
    }

    @Override
    public Plan updatePlan(Long id, Plan updated) {
        Plan existing = getPlanById(id);
        existing.setLabel(updated.getLabel());
        existing.setPrix(updated.getPrix());
        existing.setColor(updated.getColor());
        existing.setFeatures(updated.getFeatures());
        log.info("Updating plan: {}", existing.getPlanId());
        return planRepository.save(existing);
    }

    @Override
    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        planRepository.delete(plan);
        log.info("Plan {} deleted", plan.getPlanId());
    }
}
