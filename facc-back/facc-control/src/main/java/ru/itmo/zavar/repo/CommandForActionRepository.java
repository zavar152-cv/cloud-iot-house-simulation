package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.CommandForActionEntity;

@Repository
public interface CommandForActionRepository extends CrudRepository<CommandForActionEntity, String> {
}
