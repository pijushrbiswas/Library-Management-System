package com.library.management.search;

import com.library.management.model.Book;

public final class AuthorSearchStrategy implements BookSearchStrategy {
    @Override
    public boolean matches(Book book, String query) {
        return book.getAuthor().toLowerCase().contains(query.toLowerCase());
    }
}
