package com.saas.report.plan.application.usecase;

import com.saas.report.plan.application.dto.PlanResponse;
import com.saas.report.plan.domain.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListPlansUseCase {

    private final PlanRepository planRepository;

    public ListPlansUseCase(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return planRepository.findAll().stream()
                .map(PlanResponse::from)
                .toList();
    }
}