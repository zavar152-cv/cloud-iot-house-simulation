package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.StateEntity;
import ru.itmo.zavar.entity.StatusEntity;

@Repository
public interface StateRepository extends CrudRepository<StateEntity, Long> {
}
