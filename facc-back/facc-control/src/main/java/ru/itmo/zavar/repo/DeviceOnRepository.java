package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceOnEntity;

@Repository
public interface DeviceOnRepository extends CrudRepository<DeviceOnEntity, Long> {
}
