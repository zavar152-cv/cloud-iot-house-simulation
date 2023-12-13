package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.ActionEntity;

import java.util.Optional;

@Repository
public interface ActionRepository extends CrudRepository<ActionEntity, Long> {
    Optional<ActionEntity> findByAction(String action);
}
