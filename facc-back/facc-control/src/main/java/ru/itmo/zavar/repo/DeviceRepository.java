package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceEntity;
import ru.itmo.zavar.model.JobGroup;

import java.util.List;

@Repository
public interface DeviceRepository extends CrudRepository<DeviceEntity, String> {
    List<DeviceEntity> findAllByJobGroup(JobGroup jobGroup);
}
