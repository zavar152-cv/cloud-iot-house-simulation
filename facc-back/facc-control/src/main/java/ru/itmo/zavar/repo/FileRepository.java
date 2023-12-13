package ru.itmo.zavar.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.entity.FileEntity;

import java.util.Optional;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, Long> {
    Optional<FileEntity> findByName(String name);
}
