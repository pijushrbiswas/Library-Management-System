package com.library.management.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Patron {
    private final String patronId;
    private String name;
    private String email;
    private final List<BorrowRecord> borrowingHistory = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();
    private final Set<String> preferredAuthors = ConcurrentHashMap.newKeySet();

    public Patron(String patronId, String name, String email) {
        this.patronId = requireNonBlank(patronId, "patronId");
        this.name = requireNonBlank(name, "name");
        this.email = requireNonBlank(email, "email");
    }

    public String getPatronId() {
        return patronId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<BorrowRecord> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public Set<String> getPreferredAuthors() {
        return Collections.unmodifiableSet(preferredAuthors);
    }

    public void updateInfo(String name, String email) {
        this.name = requireNonBlank(name, "name");
        this.email = requireNonBlank(email, "email");
    }

    public void addPreferenceAuthor(String author) {
        preferredAuthors.add(requireNonBlank(author, "author"));
    }

    public void addBorrowRecord(BorrowRecord record) {
        borrowingHistory.add(Objects.requireNonNull(record, "record cannot be null"));
    }

    public void receiveNotification(String message) {
        notifications.add(Objects.requireNonNull(message, "message cannot be null"));
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}
