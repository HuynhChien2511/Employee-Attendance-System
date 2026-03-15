/*
 * FILE: SuggestionService.java
 * PURPOSE: Manages the employee suggestion/feedback messaging system.
 *          Employees can send suggestions to managers; managers can read and
 *          reply. Tracks read/unread status and reply timestamps.
 *
 * METHODS:
 *  - send(sender, receiver, subject, message)
 *      Creates and saves a new Suggestion with isRead = false.
 *      Called by POST /api/employee/suggestions when an employee submits feedback.
 *
 *  - getInbox(userId)
 *      Returns all suggestions received by a user, newest first.
 *      Used on the manager's inbox view.
 *
 *  - getSent(userId)
 *      Returns all suggestions sent by a user, newest first.
 *      Used on the employee's sent-messages view.
 *
 *  - reply(suggestionId, replyText)
 *      Sets the reply text, marks the suggestion as read, records repliedAt,
 *      and saves. Returns null if the suggestion ID is not found.
 *      Called from POST /api/manager/suggestions/{id}/reply.
 *
 *  - markRead(suggestionId)
 *      Sets isRead = true on a single suggestion. Called when the receiver opens
 *      the suggestion detail view.
 *
 *  - countUnread(userId)
 *      Returns the count of unread suggestions for a user.
 *      Used by the frontend to display the unread badge on the inbox tab.
 *
 * HOW TO MODIFY:
 *  - To allow employees to send suggestions to admins as well: change the receiver
 *    selection in the controller to allow any user role, not just MANAGER.
 *  - To support file attachments in suggestions: add a filePath field to the
 *    Suggestion entity and handle multipart upload in the controller.
 *  - To add notification on reply: inject NotificationService and call create()
 *    inside reply() to notify the original sender.
 */
package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Suggestion;
import com.example.demo.entity.User;
import com.example.demo.repository.SuggestionRepository;

@Service
public class SuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    public Suggestion send(User sender, User receiver, String subject, String message) {
        Suggestion s = new Suggestion();
        s.setSender(sender);
        s.setReceiver(receiver);
        s.setSubject(subject);
        s.setMessage(message);
        s.setIsRead(false);
        return suggestionRepository.save(s);
    }

    public List<Suggestion> getInbox(Long userId) {
        return suggestionRepository.findByReceiver_IdOrderByCreatedAtDesc(userId);
    }

    public List<Suggestion> getSent(Long userId) {
        return suggestionRepository.findBySender_IdOrderByCreatedAtDesc(userId);
    }

    public Suggestion reply(Long suggestionId, String replyText) {
        return suggestionRepository.findById(suggestionId).map(s -> {
            s.setReply(replyText);
            s.setIsRead(true);
            s.setRepliedAt(java.time.LocalDateTime.now());
            return suggestionRepository.save(s);
        }).orElse(null);
    }

    public void markRead(Long suggestionId) {
        suggestionRepository.findById(suggestionId).ifPresent(s -> {
            s.setIsRead(true);
            suggestionRepository.save(s);
        });
    }

    public long countUnread(Long userId) {
        return suggestionRepository.countByReceiver_IdAndIsReadFalse(userId);
    }
}
