package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.dto.GroupOnEntity;
import ru.itmo.zavar.model.JobGroup;

import java.util.Optional;

@Repository
public interface GroupOnRepository extends CrudRepository<GroupOnEntity, Long> {
    Optional<GroupOnEntity> findByJobGroup(JobGroup jobGroup);
}
