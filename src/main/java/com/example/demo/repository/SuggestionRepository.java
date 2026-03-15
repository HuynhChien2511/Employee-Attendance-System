/*
 * FILE: SuggestionRepository.java
 * PURPOSE: Spring Data JPA repository for Suggestion (internal messages from employees
 *          to managers).
 *
 * METHODS:
 *   - findByReceiver_IdOrderByCreatedAtDesc(userId)  : Inbox — suggestions received by a user.
 *   - findBySender_IdOrderByCreatedAtDesc(userId)    : Sent box — suggestions sent by a user.
 *   - countByReceiver_IdAndIsReadFalse(userId)       : Unread inbox count (badge indicator).
 *
 * HOW TO MODIFY:
 *   - To support bidirectional messaging threads, you would need a separate Thread/
 *     Message model; this model handles simple one-shot suggestions with a single reply.
 *   - To search suggestions by subject, add findByReceiver_IdAndSubjectContainingIgnoreCase(...).
 */
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Suggestion;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId);
    List<Suggestion> findBySender_IdOrderByCreatedAtDesc(Long senderId);
    long countByReceiver_IdAndIsReadFalse(Long receiverId);
}
