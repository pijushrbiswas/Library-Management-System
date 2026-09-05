package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.BorrowRecord;
import com.library.management.model.Patron;
import com.library.management.search.SearchType;

import java.util.List;

public interface LibraryService {
    void addBook(Book book);

    void updateBook(String isbn, String title, String author, int publicationYear);

    void removeBook(String isbn);

    List<Book> searchBooks(SearchType searchType, String query);

    void addBranch(String branchId, String name);

    void updateBranch(String branchId, String name);

    void addBookCopiesToBranch(String branchId, String isbn, int quantity);

    void transferBook(String sourceBranchId, String targetBranchId, String isbn, int quantity);

    void addPatron(Patron patron);

    void updatePatron(String patronId, String name, String email);

    void addPatronPreferenceAuthor(String patronId, String author);

    String checkoutBook(String branchId, String patronId, String isbn);

    void returnBook(String loanId);

    void reserveBook(String branchId, String patronId, String isbn);

    int getAvailableCopies(String branchId, String isbn);

    int getBorrowedCopies(String branchId, String isbn);

    List<BorrowRecord> getBorrowingHistory(String patronId);

    List<Book> recommendBooks(String patronId, int limit);

}
