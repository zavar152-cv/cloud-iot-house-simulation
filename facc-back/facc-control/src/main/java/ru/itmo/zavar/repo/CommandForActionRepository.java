package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.entity.CommandForActionEntity;

import java.util.Optional;

@Repository
@Transactional
public interface CommandForActionRepository extends CrudRepository<CommandForActionEntity, Long> {
    Optional<CommandForActionEntity> findByAction_Id(Long id);
}
