package com.library.management.search;

import com.library.management.model.Book;

public interface BookSearchStrategy {
    boolean matches(Book book, String query);
}
