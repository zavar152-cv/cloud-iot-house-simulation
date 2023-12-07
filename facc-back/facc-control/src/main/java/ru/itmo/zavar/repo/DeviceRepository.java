package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceEntity;

@Repository
public interface DeviceRepository extends CrudRepository<DeviceEntity, String> {

}
