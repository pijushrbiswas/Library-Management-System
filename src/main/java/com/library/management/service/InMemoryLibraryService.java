package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.Branch;
import com.library.management.model.Patron;
import com.library.management.model.Reservation;
import com.library.management.notification.LoggingNotificationObserver;
import com.library.management.notification.NotificationObserver;
import com.library.management.recommendation.HistoryBasedRecommendationStrategy;
import com.library.management.recommendation.RecommendationStrategy;
import com.library.management.search.BookSearchStrategy;
import com.library.management.search.SearchStrategyFactory;
import com.library.management.search.SearchType;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class InMemoryLibraryService implements LibraryService {
    private static final Logger LOGGER = Logger.getLogger(InMemoryLibraryService.class.getName());

    private final Map<String, Book> booksByIsbn = new HashMap<>();
    private final Map<String, Branch> branchesById = new HashMap<>();
    private final Map<String, Patron> patronsById = new HashMap<>();
    private final Map<String, BorrowRecord> activeLoansById = new HashMap<>();
    private final Map<String, Deque<Reservation>> reservationsByKey = new HashMap<>();
    private final SearchStrategyFactory searchStrategyFactory;
    private final RecommendationStrategy recommendationStrategy;
    private final List<NotificationObserver> observers = new ArrayList<>();

    public InMemoryLibraryService() {
        this(new SearchStrategyFactory(), new HistoryBasedRecommendationStrategy());
    }

    public InMemoryLibraryService(SearchStrategyFactory searchStrategyFactory, RecommendationStrategy recommendationStrategy) {
        this.searchStrategyFactory = Objects.requireNonNull(searchStrategyFactory, "searchStrategyFactory cannot be null");
        this.recommendationStrategy = Objects.requireNonNull(recommendationStrategy, "recommendationStrategy cannot be null");
        this.observers.add(new LoggingNotificationObserver());
    }

    @Override
    public void addBook(Book book) {
        Objects.requireNonNull(book, "book cannot be null");
        if (booksByIsbn.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("Book already exists: " + book.getIsbn());
        }
        booksByIsbn.put(book.getIsbn(), book);
        LOGGER.info(() -> "Added book " + book.getIsbn());
    }

    @Override
    public void updateBook(String isbn, String title, String author, int publicationYear) {
        Book book = getBookOrThrow(isbn);
        book.updateDetails(title, author, publicationYear);
        LOGGER.info(() -> "Updated book " + isbn);
    }

    @Override
    public void removeBook(String isbn) {
        Book book = getBookOrThrow(isbn);
        for (Branch branch : branchesById.values()) {
            if (branch.getBorrowedCopies(isbn) > 0) {
                throw new IllegalStateException("Cannot remove borrowed book: " + isbn);
            }
        }
        booksByIsbn.remove(book.getIsbn());
        LOGGER.info(() -> "Removed book " + isbn);
    }

    @Override
    public List<Book> searchBooks(SearchType searchType, String query) {
        validateNonBlank(query, "query");
        BookSearchStrategy strategy = searchStrategyFactory.create(Objects.requireNonNull(searchType, "searchType cannot be null"));
        return booksByIsbn.values().stream()
                .filter(book -> strategy.matches(book, query))
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    @Override
    public void addBranch(String branchId, String name) {
        validateNonBlank(branchId, "branchId");
        if (branchesById.containsKey(branchId)) {
            throw new IllegalArgumentException("Branch already exists: " + branchId);
        }
        branchesById.put(branchId, new Branch(branchId, name));
        LOGGER.info(() -> "Added branch " + branchId);
    }

    @Override
    public void updateBranch(String branchId, String name) {
        Branch branch = getBranchOrThrow(branchId);
        branch.updateName(name);
        LOGGER.info(() -> "Updated branch " + branchId);
    }

    @Override
    public void addBookCopiesToBranch(String branchId, String isbn, int quantity) {
        getBookOrThrow(isbn);
        Branch branch = getBranchOrThrow(branchId);
        branch.addCopies(isbn, quantity);
        LOGGER.info(() -> "Added " + quantity + " copies of " + isbn + " to branch " + branchId);
    }

    @Override
    public void transferBook(String sourceBranchId, String targetBranchId, String isbn, int quantity) {
        getBookOrThrow(isbn);
        Branch source = getBranchOrThrow(sourceBranchId);
        Branch target = getBranchOrThrow(targetBranchId);
        source.transferOut(isbn, quantity);
        target.addCopies(isbn, quantity);
        LOGGER.info(() -> "Transferred " + quantity + " copies of " + isbn + " from " + sourceBranchId + " to " + targetBranchId);
    }

    @Override
    public void addPatron(Patron patron) {
        Objects.requireNonNull(patron, "patron cannot be null");
        if (patronsById.containsKey(patron.getPatronId())) {
            throw new IllegalArgumentException("Patron already exists: " + patron.getPatronId());
        }
        patronsById.put(patron.getPatronId(), patron);
        LOGGER.info(() -> "Added patron " + patron.getPatronId());
    }

    @Override
    public void updatePatron(String patronId, String name, String email) {
        Patron patron = getPatronOrThrow(patronId);
        patron.updateInfo(name, email);
        LOGGER.info(() -> "Updated patron " + patronId);
    }

    @Override
    public void addPatronPreferenceAuthor(String patronId, String author) {
        Patron patron = getPatronOrThrow(patronId);
        patron.addPreferenceAuthor(author);
        LOGGER.info(() -> "Added author preference for patron " + patronId + ": " + author);
    }

    @Override
    public String checkoutBook(String branchId, String patronId, String isbn) {
        getBookOrThrow(isbn);
        Branch branch = getBranchOrThrow(branchId);
        Patron patron = getPatronOrThrow(patronId);
        if (branch.getAvailableCopies(isbn) <= 0) {
            throw new IllegalStateException("Book is not available at branch " + branchId + ". Place a reservation.");
        }
        branch.checkoutCopy(isbn);
        String loanId = UUID.randomUUID().toString();
        BorrowRecord record = new BorrowRecord(loanId, isbn, branchId, LocalDateTime.now());
        patron.addBorrowRecord(record);
        activeLoansById.put(loanId, record);
        LOGGER.info(() -> "Checked out book " + isbn + " for patron " + patronId + " at branch " + branchId);
        return loanId;
    }

    @Override
    public void returnBook(String loanId) {
        validateNonBlank(loanId, "loanId");
        BorrowRecord record = activeLoansById.remove(loanId);
        if (record == null) {
            throw new IllegalArgumentException("Unknown or already returned loanId: " + loanId);
        }
        Branch branch = getBranchOrThrow(record.getBranchId());
        branch.returnCopy(record.getIsbn());
        record.markReturned(LocalDateTime.now());
        LOGGER.info(() -> "Returned loan " + loanId);
        notifyNextReservation(record.getBranchId(), record.getIsbn());
    }

    @Override
    public void reserveBook(String branchId, String patronId, String isbn) {
        getBookOrThrow(isbn);
        getPatronOrThrow(patronId);
        Branch branch = getBranchOrThrow(branchId);
        if (branch.getAvailableCopies(isbn) > 0) {
            throw new IllegalStateException("Book is available; checkout instead of reserving.");
        }
        String key = reservationKey(branchId, isbn);
        reservationsByKey.computeIfAbsent(key, unused -> new ArrayDeque<>())
                .addLast(new Reservation(isbn, patronId, branchId, LocalDateTime.now()));
        LOGGER.info(() -> "Reserved book " + isbn + " for patron " + patronId + " at branch " + branchId);
    }

    @Override
    public int getAvailableCopies(String branchId, String isbn) {
        getBookOrThrow(isbn);
        return getBranchOrThrow(branchId).getAvailableCopies(isbn);
    }

    @Override
    public int getBorrowedCopies(String branchId, String isbn) {
        getBookOrThrow(isbn);
        return getBranchOrThrow(branchId).getBorrowedCopies(isbn);
    }

    @Override
    public List<BorrowRecord> getBorrowingHistory(String patronId) {
        return getPatronOrThrow(patronId).getBorrowingHistory();
    }

    @Override
    public List<Book> recommendBooks(String patronId, int limit) {
        Patron patron = getPatronOrThrow(patronId);
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        Collection<Book> availableBooks = booksByIsbn.values().stream()
                .filter(book -> totalAvailableCopies(book.getIsbn()) > 0)
                .collect(Collectors.toList());
        return recommendationStrategy.recommend(patron, availableBooks, limit);
    }

    public void registerObserver(NotificationObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer cannot be null"));
    }

    private int totalAvailableCopies(String isbn) {
        return branchesById.values().stream()
                .mapToInt(branch -> branch.getAvailableCopies(isbn))
                .sum();
    }

    private void notifyNextReservation(String branchId, String isbn) {
        String key = reservationKey(branchId, isbn);
        Deque<Reservation> queue = reservationsByKey.get(key);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        Reservation reservation = queue.removeFirst();
        Patron patron = getPatronOrThrow(reservation.getPatronId());
        Book book = getBookOrThrow(isbn);
        String message = "Reserved book is now available: " + book.getTitle() + " (" + isbn + ") at branch " + branchId;
        for (NotificationObserver observer : observers) {
            observer.notify(patron, message);
        }
        LOGGER.info(() -> "Sent reservation availability notification for " + isbn + " to patron " + patron.getPatronId());
    }

    private Book getBookOrThrow(String isbn) {
        validateNonBlank(isbn, "isbn");
        Book book = booksByIsbn.get(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found: " + isbn);
        }
        return book;
    }

    private Branch getBranchOrThrow(String branchId) {
        validateNonBlank(branchId, "branchId");
        Branch branch = branchesById.get(branchId);
        if (branch == null) {
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        return branch;
    }

    private Patron getPatronOrThrow(String patronId) {
        validateNonBlank(patronId, "patronId");
        Patron patron = patronsById.get(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron not found: " + patronId);
        }
        return patron;
    }

    private static String reservationKey(String branchId, String isbn) {
        return branchId + "::" + isbn;
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }
}
