/*
 * FILE: Suggestion.java
 * PURPOSE: JPA entity representing an internal suggestion/message sent from one user
 *          (typically an employee) to another (typically a manager). Maps to `suggestions`.
 *
 * KEY FIELDS:
 *   - sender / receiver : The User who sent / who should receive the suggestion.
 *   - subject           : Optional short subject line.
 *   - message           : Full suggestion body.
 *   - reply             : The receiver's reply text; null until a reply is posted.
 *   - isRead            : false until the receiver reads or replies.
 *   - repliedAt         : Timestamp when a reply was saved.
 *
 * HOW TO MODIFY:
 *   - Reply logic is in SuggestionService.reply(); extend it to send a notification to the
 *     original sender when a reply is posted.
 *   - To allow attaching files to suggestions, add fileName/filePath fields here and handle
 *     upload in SharedApiController similar to TaskApiController.
 */
package com.example.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "suggestions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User receiver;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String reply;

    private Boolean isRead = false;

    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
