package pl.ismop.web.repository;

import org.springframework.data.repository.CrudRepository;

import pl.ismop.web.domain.ExperimentPlan;

public interface ExperimentPlanRepository extends CrudRepository<ExperimentPlan, Long> {
}