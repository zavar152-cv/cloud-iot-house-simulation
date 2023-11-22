package ru.itmo.zavar.faccauth.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.faccauth.entity.RoleEntity;

import javax.swing.text.html.parser.Entity;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Entity> {
    Optional<RoleEntity> findByName(String name);
}
