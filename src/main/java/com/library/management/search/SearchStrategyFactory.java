package com.library.management.search;

import java.util.EnumMap;
import java.util.Map;

public final class SearchStrategyFactory {
    private final Map<SearchType, BookSearchStrategy> strategies = new EnumMap<>(SearchType.class);

    public SearchStrategyFactory() {
        strategies.put(SearchType.TITLE, new TitleSearchStrategy());
        strategies.put(SearchType.AUTHOR, new AuthorSearchStrategy());
        strategies.put(SearchType.ISBN, new IsbnSearchStrategy());
    }

    public BookSearchStrategy create(SearchType type) {
        BookSearchStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported search type: " + type);
        }
        return strategy;
    }
}
