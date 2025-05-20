package org.common.repository;

import org.common.service.UserState;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ContactRepository extends CrudRepository<UserState, Integer> {

    Optional<UserState> findById(Long id);

    void deleteById(Long id);
}
