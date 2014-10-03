package pl.ismop.web.repository;

import org.springframework.data.repository.CrudRepository;

import pl.ismop.web.domain.Experiment;

public interface ExperimentRepository extends CrudRepository<Experiment, Long> {
}