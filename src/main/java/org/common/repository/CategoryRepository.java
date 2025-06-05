package org.common.repository;

import org.common.service.Category;
import org.common.service.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<UserState> findById(Long id);

    void deleteById(Long id);

}
