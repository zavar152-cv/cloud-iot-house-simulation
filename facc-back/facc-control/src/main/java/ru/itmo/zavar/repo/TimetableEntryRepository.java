package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.TimetableEntryEntity;

@Repository
public interface TimetableEntryRepository extends CrudRepository<TimetableEntryEntity, Long> {

}
