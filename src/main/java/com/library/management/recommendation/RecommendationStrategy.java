package com.library.management.recommendation;

import com.library.management.model.Book;
import com.library.management.model.Patron;

import java.util.Collection;
import java.util.List;

public interface RecommendationStrategy {
    List<Book> recommend(Patron patron, Collection<Book> candidateBooks, int limit);
}
