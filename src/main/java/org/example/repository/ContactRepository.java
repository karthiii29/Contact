package org.example.repository;

import org.example.UserState;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ContactRepository extends CrudRepository<UserState, Integer> {

    Optional<UserState> findById(Long id);

    void deleteById(Long id);
}
