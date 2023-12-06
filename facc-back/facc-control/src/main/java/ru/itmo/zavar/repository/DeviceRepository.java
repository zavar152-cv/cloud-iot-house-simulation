package ru.itmo.zavar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceEntity;
import ru.itmo.zavar.entity.TimetableEntryEntity;

@Repository
public interface DeviceRepository extends CrudRepository<DeviceEntity, String> {

}
