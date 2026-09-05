package com.library.management.recommendation;

import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.Patron;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class HistoryBasedRecommendationStrategy implements RecommendationStrategy {
    @Override
    public List<Book> recommend(Patron patron, Collection<Book> candidateBooks, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }

        Map<String, Integer> authorFrequency = new HashMap<>();
        Set<String> borrowedIsbns = new HashSet<>();

        for (BorrowRecord record : patron.getBorrowingHistory()) {
            borrowedIsbns.add(record.getIsbn());
        }

        for (Book book : candidateBooks) {
            if (borrowedIsbns.contains(book.getIsbn())) {
                continue;
            }
            if (patron.getPreferredAuthors().contains(book.getAuthor())) {
                authorFrequency.merge(book.getAuthor(), 2, Integer::sum);
            }
        }

        for (BorrowRecord record : patron.getBorrowingHistory()) {
            String borrowedIsbn = record.getIsbn();
            for (Book candidate : candidateBooks) {
                if (candidate.getIsbn().equals(borrowedIsbn)) {
                    authorFrequency.merge(candidate.getAuthor(), 1, Integer::sum);
                }
            }
        }

        return candidateBooks.stream()
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .sorted(Comparator
                        .comparingInt((Book book) -> authorFrequency.getOrDefault(book.getAuthor(), 0))
                        .reversed()
                        .thenComparing(Book::getTitle))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
