package ru.itmo.zavar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.ActionEntity;

@Repository
public interface ActionRepository extends CrudRepository<ActionEntity, Long> {
}
