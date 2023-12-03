package ru.itmo.zavar.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.TimetableEntryEntity;

@Repository
public interface TimetableEntryRepository extends CrudRepository<TimetableEntryEntity, Long> {
    //Optional<TimetableEntryEntity> findById(@NonNull Long id);
}
