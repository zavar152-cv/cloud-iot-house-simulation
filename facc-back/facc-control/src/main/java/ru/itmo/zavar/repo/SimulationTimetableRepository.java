package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.SimulationTimetableEntity;

@Repository
public interface SimulationTimetableRepository extends CrudRepository<SimulationTimetableEntity, Long> {

}
