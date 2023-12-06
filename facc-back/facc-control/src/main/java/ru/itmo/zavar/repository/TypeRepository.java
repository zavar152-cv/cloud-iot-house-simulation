package ru.itmo.zavar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceEntity;
import ru.itmo.zavar.entity.TypeEntity;

@Repository
public interface TypeRepository extends CrudRepository<TypeEntity, Long> {
}
