package ru.itmo.zavar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.CommandForActionEntity;
import ru.itmo.zavar.entity.DeviceEntity;

@Repository
public interface CommandForActionRepository extends CrudRepository<CommandForActionEntity, String> {
}
