package com.library.management.model;

import java.util.Objects;

public final class Book {
    private String title;
    private String author;
    private final String isbn;
    private int publicationYear;

    public Book(String title, String author, String isbn, int publicationYear) {
        this.title = requireNonBlank(title, "title");
        this.author = requireNonBlank(author, "author");
        this.isbn = requireNonBlank(isbn, "isbn");
        this.publicationYear = publicationYear;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void updateDetails(String title, String author, int publicationYear) {
        this.title = requireNonBlank(title, "title");
        this.author = requireNonBlank(author, "author");
        this.publicationYear = publicationYear;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book book)) {
            return false;
        }
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
