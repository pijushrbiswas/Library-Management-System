package com.library.management.search;

import com.library.management.model.Book;

public final class IsbnSearchStrategy implements BookSearchStrategy {
    @Override
    public boolean matches(Book book, String query) {
        return book.getIsbn().equalsIgnoreCase(query);
    }
}
