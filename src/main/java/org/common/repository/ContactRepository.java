package org.common.repository;

import org.common.service.UserState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends CrudRepository<UserState, Integer> {

    Optional<UserState> findById(Long id);

    void deleteById(Long id);

    @Query("SELECT c FROM UserState c WHERE " +
            "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:emailAddress IS NULL OR LOWER(c.emailAddress) LIKE LOWER(CONCAT('%', :emailAddress, '%')))")
    List<UserState> searchByFields(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("emailAddress") String emailAddress
    );


}
