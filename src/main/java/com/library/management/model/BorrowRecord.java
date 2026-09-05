package com.library.management.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class BorrowRecord {
    private final String loanId;
    private final String isbn;
    private final String branchId;
    private final LocalDateTime checkoutAt;
    private LocalDateTime returnedAt;

    public BorrowRecord(String loanId, String isbn, String branchId, LocalDateTime checkoutAt) {
        this.loanId = Objects.requireNonNull(loanId, "loanId cannot be null");
        this.isbn = Objects.requireNonNull(isbn, "isbn cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "branchId cannot be null");
        this.checkoutAt = Objects.requireNonNull(checkoutAt, "checkoutAt cannot be null");
    }

    public String getLoanId() {
        return loanId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getBranchId() {
        return branchId;
    }

    public LocalDateTime getCheckoutAt() {
        return checkoutAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public boolean isReturned() {
        return returnedAt != null;
    }

    public void markReturned(LocalDateTime returnedAt) {
        if (isReturned()) {
            throw new IllegalStateException("Loan already returned");
        }
        this.returnedAt = Objects.requireNonNull(returnedAt, "returnedAt cannot be null");
    }
}
