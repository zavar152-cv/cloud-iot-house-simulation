package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.TypeEntity;

@Repository
public interface TypeRepository extends CrudRepository<TypeEntity, Long> {
}
