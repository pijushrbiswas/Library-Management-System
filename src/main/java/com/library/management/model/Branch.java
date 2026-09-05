package com.library.management.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Branch {
    private final String branchId;
    private String name;
    private final Map<String, Integer> availableCopies = new ConcurrentHashMap<>();
    private final Map<String, Integer> borrowedCopies = new ConcurrentHashMap<>();

    public Branch(String branchId, String name) {
        this.branchId = requireNonBlank(branchId, "branchId");
        this.name = requireNonBlank(name, "name");
    }

    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public void updateName(String name) {
        this.name = requireNonBlank(name, "name");
    }

    public void addCopies(String isbn, int quantity) {
        validatePositive(quantity);
        availableCopies.merge(requireNonBlank(isbn, "isbn"), quantity, Integer::sum);
    }

    public void transferOut(String isbn, int quantity) {
        validatePositive(quantity);
        String validIsbn = requireNonBlank(isbn, "isbn");
        int available = getAvailableCopies(validIsbn);
        if (available < quantity) {
            throw new IllegalStateException("Insufficient available copies for transfer");
        }
        availableCopies.put(validIsbn, available - quantity);
    }

    public void checkoutCopy(String isbn) {
        String validIsbn = requireNonBlank(isbn, "isbn");
        int available = getAvailableCopies(validIsbn);
        if (available <= 0) {
            throw new IllegalStateException("No available copies");
        }
        availableCopies.put(validIsbn, available - 1);
        borrowedCopies.merge(validIsbn, 1, Integer::sum);
    }

    public void returnCopy(String isbn) {
        String validIsbn = requireNonBlank(isbn, "isbn");
        int borrowed = getBorrowedCopies(validIsbn);
        if (borrowed <= 0) {
            throw new IllegalStateException("No borrowed copies to return");
        }
        borrowedCopies.put(validIsbn, borrowed - 1);
        availableCopies.merge(validIsbn, 1, Integer::sum);
    }

    public int getAvailableCopies(String isbn) {
        return availableCopies.getOrDefault(isbn, 0);
    }

    public int getBorrowedCopies(String isbn) {
        return borrowedCopies.getOrDefault(isbn, 0);
    }

    public Map<String, Integer> snapshotAvailableCopies() {
        return Collections.unmodifiableMap(availableCopies);
    }

    public Map<String, Integer> snapshotBorrowedCopies() {
        return Collections.unmodifiableMap(borrowedCopies);
    }

    private static void validatePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}
