package org.common.service;

import jakarta.persistence.*;

@Entity
@Table(name = "duplicate_contacts")
public class DuplicateContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contact1_id")
    private Long contact1Id;
    
    @Column(name = "contact2_id")
    private Long contact2Id;
    
    @Column(name = "similarity_score")
    private Double similarityScore;
    
    @Column(name = "match_type")
    private String matchType; // EMAIL, PHONE, NAME, FUZZY
    
    @Column(name = "status")
    private String status; // PENDING, MERGED, IGNORED
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    public DuplicateContact() {
        this.createdAt = java.time.LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public DuplicateContact(Long contact1Id, Long contact2Id, Double similarityScore, String matchType) {
        this();
        this.contact1Id = contact1Id;
        this.contact2Id = contact2Id;
        this.similarityScore = similarityScore;
        this.matchType = matchType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getContact1Id() {
        return contact1Id;
    }
    
    public void setContact1Id(Long contact1Id) {
        this.contact1Id = contact1Id;
    }
    
    public Long getContact2Id() {
        return contact2Id;
    }
    
    public void setContact2Id(Long contact2Id) {
        this.contact2Id = contact2Id;
    }
    
    public Double getSimilarityScore() {
        return similarityScore;
    }
    
    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
    
    public String getMatchType() {
        return matchType;
    }
    
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}