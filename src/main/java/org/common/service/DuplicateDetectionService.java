package org.common.service;

import org.common.repository.ContactRepository;
import org.common.repository.DuplicateContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private DuplicateContactRepository duplicateContactRepository;
    
    /**
     * Detect all potential duplicates in the contact database
     */
    public List<DuplicateContact> detectAllDuplicates() {
        List<UserState> allContacts = (List<UserState>) contactRepository.findAll();
        List<DuplicateContact> duplicates = new ArrayList<>();
        
        for (int i = 0; i < allContacts.size(); i++) {
            for (int j = i + 1; j < allContacts.size(); j++) {
                UserState contact1 = allContacts.get(i);
                UserState contact2 = allContacts.get(j);
                
                DuplicateMatch match = findDuplicateMatch(contact1, contact2);
                if (match.isMatch()) {
                    // Check if this duplicate pair already exists
                    Optional<DuplicateContact> existing = duplicateContactRepository
                        .findExistingDuplicate(contact1.getId(), contact2.getId());
                    
                    if (existing.isEmpty()) {
                        DuplicateContact duplicate = new DuplicateContact(
                            contact1.getId(), 
                            contact2.getId(), 
                            match.getScore(), 
                            match.getMatchType()
                        );
                        duplicates.add(duplicate);
                    }
                }
            }
        }
        
        // Save all new duplicates
        if (!duplicates.isEmpty()) {
            duplicateContactRepository.saveAll(duplicates);
        }
        
        return duplicates;
    }
    
    /**
     * Check if a specific contact has potential duplicates
     */
    public List<DuplicateContact> findDuplicatesForContact(Long contactId) {
        Optional<UserState> targetContact = contactRepository.findById(contactId);
        if (targetContact.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UserState> allContacts = (List<UserState>) contactRepository.findAll();
        List<DuplicateContact> duplicates = new ArrayList<>();
        
        for (UserState contact : allContacts) {
            if (!contact.getId().equals(contactId)) {
                DuplicateMatch match = findDuplicateMatch(targetContact.get(), contact);
                if (match.isMatch()) {
                    // Check if this duplicate pair already exists
                    Optional<DuplicateContact> existing = duplicateContactRepository
                        .findExistingDuplicate(contactId, contact.getId());
                    
                    if (existing.isEmpty()) {
                        DuplicateContact duplicate = new DuplicateContact(
                            contactId, 
                            contact.getId(), 
                            match.getScore(), 
                            match.getMatchType()
                        );
                        duplicates.add(duplicate);
                    }
                }
            }
        }
        
        // Save new duplicates
        if (!duplicates.isEmpty()) {
            duplicateContactRepository.saveAll(duplicates);
        }
        
        return duplicates;
    }
    
    /**
     * Core duplicate detection logic
     */
    private DuplicateMatch findDuplicateMatch(UserState contact1, UserState contact2) {
        double maxScore = 0.0;
        String matchType = "";
        
        // 1. Exact email match (highest priority)
        if (hasValidEmail(contact1) && hasValidEmail(contact2)) {
            if (contact1.getEmailAddress().equalsIgnoreCase(contact2.getEmailAddress())) {
                return new DuplicateMatch(true, 1.0, "EMAIL_EXACT");
            }
        }
        
        // 2. Exact phone match
        if (hasValidPhone(contact1) && hasValidPhone(contact2)) {
            String phone1 = normalizePhone(contact1.getMobileNumber());
            String phone2 = normalizePhone(contact2.getMobileNumber());
            if (phone1.equals(phone2)) {
                return new DuplicateMatch(true, 0.95, "PHONE_EXACT");
            }
        }
        
        // 3. Name similarity analysis
        double nameScore = calculateNameSimilarity(contact1, contact2);
        if (nameScore > maxScore) {
            maxScore = nameScore;
            matchType = "NAME_FUZZY";
        }
        
        // 4. Email domain + name similarity
        if (hasValidEmail(contact1) && hasValidEmail(contact2)) {
            String domain1 = extractDomain(contact1.getEmailAddress());
            String domain2 = extractDomain(contact2.getEmailAddress());
            if (domain1.equals(domain2)) {
                double emailNameScore = nameScore * 0.8; // Boost if same domain
                if (emailNameScore > maxScore) {
                    maxScore = emailNameScore;
                    matchType = "EMAIL_DOMAIN_NAME";
                }
            }
        }
        
        // 5. Phone similarity (partial matches)
        if (hasValidPhone(contact1) && hasValidPhone(contact2)) {
            double phoneScore = calculatePhoneSimilarity(contact1.getMobileNumber(), contact2.getMobileNumber());
            if (phoneScore > maxScore) {
                maxScore = phoneScore;
                matchType = "PHONE_FUZZY";
            }
        }
        
        // Consider it a match if score is above threshold
        boolean isMatch = maxScore >= 0.75;
        return new DuplicateMatch(isMatch, maxScore, matchType);
    }
    
    /**
     * Calculate name similarity using multiple algorithms
     */
    private double calculateNameSimilarity(UserState contact1, UserState contact2) {
        String fullName1 = buildFullName(contact1).toLowerCase().trim();
        String fullName2 = buildFullName(contact2).toLowerCase().trim();
        
        if (fullName1.isEmpty() || fullName2.isEmpty()) {
            return 0.0;
        }
        
        // Exact match
        if (fullName1.equals(fullName2)) {
            return 1.0;
        }
        
        // Levenshtein distance
        double levenshteinScore = 1.0 - (double) levenshteinDistance(fullName1, fullName2) / 
                                 Math.max(fullName1.length(), fullName2.length());
        
        // Jaccard similarity (word-based)
        double jaccardScore = calculateJaccardSimilarity(fullName1, fullName2);
        
        // Name component analysis
        double componentScore = calculateNameComponentSimilarity(contact1, contact2);
        
        // Return the highest score
        return Math.max(Math.max(levenshteinScore, jaccardScore), componentScore);
    }
    
    /**
     * Calculate similarity between individual name components
     */
    private double calculateNameComponentSimilarity(UserState contact1, UserState contact2) {
        double firstNameScore = 0.0;
        double lastNameScore = 0.0;
        
        // First name comparison
        if (hasValidName(contact1.getFirstName()) && hasValidName(contact2.getFirstName())) {
            String fn1 = contact1.getFirstName().toLowerCase().trim();
            String fn2 = contact2.getFirstName().toLowerCase().trim();
            if (fn1.equals(fn2)) {
                firstNameScore = 1.0;
            } else {
                firstNameScore = 1.0 - (double) levenshteinDistance(fn1, fn2) / Math.max(fn1.length(), fn2.length());
            }
        }
        
        // Last name comparison
        if (hasValidName(contact1.getLastName()) && hasValidName(contact2.getLastName())) {
            String ln1 = contact1.getLastName().toLowerCase().trim();
            String ln2 = contact2.getLastName().toLowerCase().trim();
            if (ln1.equals(ln2)) {
                lastNameScore = 1.0;
            } else {
                lastNameScore = 1.0 - (double) levenshteinDistance(ln1, ln2) / Math.max(ln1.length(), ln2.length());
            }
        }
        
        // If both first and last names are similar, it's a strong match
        if (firstNameScore > 0.8 && lastNameScore > 0.8) {
            return (firstNameScore + lastNameScore) / 2;
        }
        
        // If only one name component matches well, lower the score
        return Math.max(firstNameScore, lastNameScore) * 0.7;
    }
    
    /**
     * Calculate phone number similarity
     */
    private double calculatePhoneSimilarity(String phone1, String phone2) {
        String normalized1 = normalizePhone(phone1);
        String normalized2 = normalizePhone(phone2);
        
        if (normalized1.equals(normalized2)) {
            return 1.0;
        }
        
        // Check if one is a substring of the other (different formats)
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return 0.85;
        }
        
        // Check last 7 digits (local number similarity)
        if (normalized1.length() >= 7 && normalized2.length() >= 7) {
            String last7_1 = normalized1.substring(normalized1.length() - 7);
            String last7_2 = normalized2.substring(normalized2.length() - 7);
            if (last7_1.equals(last7_2)) {
                return 0.8;
            }
        }
        
        return 0.0;
    }
    
    /**
     * Calculate Jaccard similarity for word-based comparison
     */
    private double calculateJaccardSimilarity(String str1, String str2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(str1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(str2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }
    
    // Helper methods
    private boolean hasValidEmail(UserState contact) {
        return contact.getEmailAddress() != null && !contact.getEmailAddress().trim().isEmpty();
    }
    
    private boolean hasValidPhone(UserState contact) {
        return contact.getMobileNumber() != null && !contact.getMobileNumber().trim().isEmpty();
    }
    
    private boolean hasValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    
    private String buildFullName(UserState contact) {
        StringBuilder fullName = new StringBuilder();
        if (hasValidName(contact.getFirstName())) {
            fullName.append(contact.getFirstName().trim());
        }
        if (hasValidName(contact.getMiddleName())) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(contact.getMiddleName().trim());
        }
        if (hasValidName(contact.getLastName())) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(contact.getLastName().trim());
        }
        return fullName.toString();
    }
    
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }
    
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return "";
        return email.substring(email.lastIndexOf("@") + 1).toLowerCase();
    }
    
    /**
     * Inner class to represent a duplicate match result
     */
    private static class DuplicateMatch {
        private final boolean match;
        private final double score;
        private final String matchType;
        
        public DuplicateMatch(boolean match, double score, String matchType) {
            this.match = match;
            this.score = score;
            this.matchType = matchType;
        }
        
        public boolean isMatch() { return match; }
        public double getScore() { return score; }
        public String getMatchType() { return matchType; }
    }
}