package org.common.controller;

import org.common.repository.ContactRepository;
import org.common.repository.DuplicateContactRepository;
import org.common.service.DuplicateContact;
import org.common.service.DuplicateDetectionService;
import org.common.service.UserState;
import org.common.util.ApiResponse;
import org.common.util.CommonUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contacts/duplicates")
public class DuplicateDetectionController {
    
    @Autowired
    private DuplicateDetectionService duplicateDetectionService;
    
    @Autowired
    private DuplicateContactRepository duplicateContactRepository;
    
    @Autowired
    private ContactRepository contactRepository;
    
    private <T> ResponseEntity<ApiResponse<T>> buildResponse(boolean success, String message, T data, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse<>(success, message, data), status);
    }
    
    /**
     * Scan all contacts and detect duplicates
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scanForDuplicates() {
        try {
            List<DuplicateContact> duplicates = duplicateDetectionService.detectAllDuplicates();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("duplicatesFound", duplicates.size());
            responseData.put("status", "scan_completed");
            
            if (duplicates.isEmpty()) {
                responseData.put("message", "No duplicates found in your contacts");
                return buildResponse(true, "Duplicate scan completed", responseData, HttpStatus.OK);
            }
            
            // Group duplicates by match type for summary
            Map<String, Long> matchTypeCounts = duplicates.stream()
                .collect(Collectors.groupingBy(DuplicateContact::getMatchType, Collectors.counting()));
            
            responseData.put("matchTypes", matchTypeCounts);
            responseData.put("message", "Potential duplicates detected");
            
            return buildResponse(true, "Duplicate scan completed", responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error during duplicate scan", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all pending duplicate pairs with contact details
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPendingDuplicates() {
        try {
            List<DuplicateContact> pendingDuplicates = duplicateContactRepository.findPendingDuplicates();
            
            if (pendingDuplicates.isEmpty()) {
                Map<String, Object> responseData = Map.of(
                    "count", 0,
                    "duplicates", new ArrayList<>(),
                    "message", "No pending duplicates found"
                );
                return buildResponse(true, "No pending duplicates", responseData, HttpStatus.OK);
            }
            
            List<Map<String, Object>> duplicateDetails = new ArrayList<>();
            
            for (DuplicateContact duplicate : pendingDuplicates) {
                Optional<UserState> contact1 = contactRepository.findById(duplicate.getContact1Id());
                Optional<UserState> contact2 = contactRepository.findById(duplicate.getContact2Id());
                
                if (contact1.isPresent() && contact2.isPresent()) {
                    Map<String, Object> duplicateInfo = new HashMap<>();
                    duplicateInfo.put("duplicateId", duplicate.getId());
                    duplicateInfo.put("similarityScore", Math.round(duplicate.getSimilarityScore() * 100));
                    duplicateInfo.put("matchType", duplicate.getMatchType());
                    duplicateInfo.put("createdAt", duplicate.getCreatedAt().toString());
                    
                    // Contact 1 details
                    Map<String, String> contact1Details = CommonUtility.contactToMap(contact1.get());
                    duplicateInfo.put("contact1", contact1Details);
                    
                    // Contact 2 details
                    Map<String, String> contact2Details = CommonUtility.contactToMap(contact2.get());
                    duplicateInfo.put("contact2", contact2Details);
                    
                    duplicateDetails.add(duplicateInfo);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("count", duplicateDetails.size());
            responseData.put("duplicates", duplicateDetails);
            
            return buildResponse(true, "Pending duplicates retrieved", responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error retrieving duplicates", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Check for duplicates of a specific contact
     */
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findDuplicatesForContact(@PathVariable Long contactId) {
        try {
            // Check if contact exists
            Optional<UserState> targetContact = contactRepository.findById(contactId);
            if (targetContact.isEmpty()) {
                return buildResponse(false, "Contact not found", null, HttpStatus.NOT_FOUND);
            }
            
            List<DuplicateContact> duplicates = duplicateDetectionService.findDuplicatesForContact(contactId);
            
            List<Map<String, Object>> duplicateDetails = new ArrayList<>();
            
            for (DuplicateContact duplicate : duplicates) {
                Long otherContactId = duplicate.getContact1Id().equals(contactId) ? 
                    duplicate.getContact2Id() : duplicate.getContact1Id();
                
                Optional<UserState> otherContact = contactRepository.findById(otherContactId);
                
                if (otherContact.isPresent()) {
                    Map<String, Object> duplicateInfo = new HashMap<>();
                    duplicateInfo.put("duplicateId", duplicate.getId());
                    duplicateInfo.put("similarityScore", Math.round(duplicate.getSimilarityScore() * 100));
                    duplicateInfo.put("matchType", duplicate.getMatchType());
                    duplicateInfo.put("otherContact", CommonUtility.contactToMap(otherContact.get()));
                    
                    duplicateDetails.add(duplicateInfo);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("targetContact", CommonUtility.contactToMap(targetContact.get()));
            responseData.put("duplicatesFound", duplicateDetails.size());
            responseData.put("duplicates", duplicateDetails);
            
            return buildResponse(true, "Duplicate check completed", responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error checking for duplicates", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Merge two contacts (mark duplicate as resolved)
     */
    @PostMapping("/merge/{duplicateId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> mergeDuplicateContacts(
            @PathVariable Long duplicateId,
            @RequestParam Long keepContactId) {
        
        try {
            Optional<DuplicateContact> duplicateOpt = duplicateContactRepository.findById(duplicateId);
            if (duplicateOpt.isEmpty()) {
                return buildResponse(false, "Duplicate record not found", null, HttpStatus.NOT_FOUND);
            }
            
            DuplicateContact duplicate = duplicateOpt.get();
            
            // Validate that keepContactId is one of the duplicate contacts
            if (!keepContactId.equals(duplicate.getContact1Id()) && !keepContactId.equals(duplicate.getContact2Id())) {
                return buildResponse(false, "Invalid contact ID for merge", null, HttpStatus.BAD_REQUEST);
            }
            
            Long deleteContactId = keepContactId.equals(duplicate.getContact1Id()) ? 
                duplicate.getContact2Id() : duplicate.getContact1Id();
            
            // Check if both contacts exist
            Optional<UserState> keepContact = contactRepository.findById(keepContactId);
            Optional<UserState> deleteContact = contactRepository.findById(deleteContactId);
            
            if (keepContact.isEmpty() || deleteContact.isEmpty()) {
                return buildResponse(false, "One or both contacts not found", null, HttpStatus.NOT_FOUND);
            }
            
            // Delete the unwanted contact
            contactRepository.deleteById(deleteContactId);
            
            // Mark duplicate as merged
            duplicate.setStatus("MERGED");
            duplicateContactRepository.save(duplicate);
            
            Map<String, String> responseData = Map.of(
                "status", "merged",
                "keptContactId", String.valueOf(keepContactId),
                "deletedContactId", String.valueOf(deleteContactId),
                "duplicateId", String.valueOf(duplicateId)
            );
            
            return buildResponse(true, "Contacts merged successfully", responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, String> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error merging contacts", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Ignore a duplicate (mark as not a duplicate)
     */
    @PostMapping("/ignore/{duplicateId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> ignoreDuplicate(@PathVariable Long duplicateId) {
        try {
            Optional<DuplicateContact> duplicateOpt = duplicateContactRepository.findById(duplicateId);
            if (duplicateOpt.isEmpty()) {
                return buildResponse(false, "Duplicate record not found", null, HttpStatus.NOT_FOUND);
            }
            
            DuplicateContact duplicate = duplicateOpt.get();
            duplicate.setStatus("IGNORED");
            duplicateContactRepository.save(duplicate);
            
            Map<String, String> responseData = Map.of(
                "status", "ignored",
                "duplicateId", String.valueOf(duplicateId)
            );
            
            return buildResponse(true, "Duplicate marked as ignored", responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, String> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error ignoring duplicate", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get duplicate statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDuplicateStats() {
        try {
            List<DuplicateContact> allDuplicates = duplicateContactRepository.findAll();
            
            Map<String, Long> statusCounts = allDuplicates.stream()
                .collect(Collectors.groupingBy(DuplicateContact::getStatus, Collectors.counting()));
            
            Map<String, Long> matchTypeCounts = allDuplicates.stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .collect(Collectors.groupingBy(DuplicateContact::getMatchType, Collectors.counting()));
            
            long highConfidenceCount = allDuplicates.stream()
                .filter(d -> "PENDING".equals(d.getStatus()) && d.getSimilarityScore() >= 0.9)
                .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDuplicates", allDuplicates.size());
            stats.put("statusBreakdown", statusCounts);
            stats.put("matchTypeBreakdown", matchTypeCounts);
            stats.put("highConfidenceDuplicates", highConfidenceCount);
            
            return buildResponse(true, "Duplicate statistics retrieved", stats, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorData = Map.of("error", e.getMessage());
            return buildResponse(false, "Error retrieving statistics", errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}