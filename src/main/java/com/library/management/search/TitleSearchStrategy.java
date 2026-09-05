package com.library.management.search;

import com.library.management.model.Book;

public final class TitleSearchStrategy implements BookSearchStrategy {
    @Override
    public boolean matches(Book book, String query) {
        return book.getTitle().toLowerCase().contains(query.toLowerCase());
    }
}
