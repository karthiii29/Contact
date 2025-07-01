package org.common.repository;

import org.common.service.DuplicateContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuplicateContactRepository extends JpaRepository<DuplicateContact, Long> {
    
    @Query("SELECT d FROM DuplicateContact d WHERE d.status = 'PENDING' ORDER BY d.similarityScore DESC")
    List<DuplicateContact> findPendingDuplicates();
    
    @Query("SELECT d FROM DuplicateContact d WHERE (d.contact1Id = :contactId OR d.contact2Id = :contactId) AND d.status = 'PENDING'")
    List<DuplicateContact> findPendingDuplicatesForContact(@Param("contactId") Long contactId);
    
    @Query("SELECT d FROM DuplicateContact d WHERE " +
           "((d.contact1Id = :contact1Id AND d.contact2Id = :contact2Id) OR " +
           "(d.contact1Id = :contact2Id AND d.contact2Id = :contact1Id))")
    Optional<DuplicateContact> findExistingDuplicate(@Param("contact1Id") Long contact1Id, @Param("contact2Id") Long contact2Id);
    
    @Query("SELECT d FROM DuplicateContact d WHERE d.similarityScore >= :threshold AND d.status = 'PENDING'")
    List<DuplicateContact> findHighConfidenceDuplicates(@Param("threshold") Double threshold);
}