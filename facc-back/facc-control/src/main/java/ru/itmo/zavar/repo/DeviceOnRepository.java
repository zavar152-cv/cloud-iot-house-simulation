package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.DeviceOnEntity;

import java.util.Optional;

@Repository
public interface DeviceOnRepository extends CrudRepository<DeviceOnEntity, Long> {
    Optional<DeviceOnEntity> findByDevice_Id(String deviceID);

    void deleteByDevice_Id(String deviceId);
}
