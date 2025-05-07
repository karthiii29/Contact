package org.example.repository;

import org.example.UserState;
import org.springframework.data.repository.CrudRepository;

public interface ContactRepository extends CrudRepository<UserState, Integer> {

}
