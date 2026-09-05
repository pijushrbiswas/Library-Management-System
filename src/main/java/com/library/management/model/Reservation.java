package com.library.management.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Reservation {
    private final String isbn;
    private final String patronId;
    private final String branchId;
    private final LocalDateTime createdAt;

    public Reservation(String isbn, String patronId, String branchId, LocalDateTime createdAt) {
        this.isbn = Objects.requireNonNull(isbn, "isbn cannot be null");
        this.patronId = Objects.requireNonNull(patronId, "patronId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "branchId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public String getBranchId() {
        return branchId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
