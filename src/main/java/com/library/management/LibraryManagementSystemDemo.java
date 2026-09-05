package com.library.management;

import com.library.management.model.Book;
import com.library.management.model.Patron;
import com.library.management.search.SearchType;
import com.library.management.service.InMemoryLibraryService;
import com.library.management.service.LibraryService;

import java.util.List;

public final class LibraryManagementSystemDemo {
    private LibraryManagementSystemDemo() {
    }

    public static void main(String[] args) {
        LibraryService library = new InMemoryLibraryService();

        library.addBranch("B1", "Central");
        library.addBranch("B2", "North");

        library.addBook(new Book("Effective Java", "Joshua Bloch", "9780134685991", 2018));
        library.addBook(new Book("Clean Code", "Robert C. Martin", "9780132350884", 2008));
        library.addBook(new Book("Clean Architecture", "Robert C. Martin", "9780134494166", 2017));

        library.addBookCopiesToBranch("B1", "9780134685991", 2);
        library.addBookCopiesToBranch("B1", "9780132350884", 1);
        library.addBookCopiesToBranch("B2", "9780134494166", 1);

        Patron patron = new Patron("P1", "Alex", "alex@example.com");
        library.addPatron(patron);
        library.addPatronPreferenceAuthor("P1", "Robert C. Martin");

        String loanId = library.checkoutBook("B1", "P1", "9780134685991");
        library.returnBook(loanId);

        List<Book> searchResults = library.searchBooks(SearchType.AUTHOR, "Martin");
        System.out.println("Author search results: " + searchResults.size());

        List<Book> recommendations = library.recommendBooks("P1", 2);
        System.out.println("Recommendations: " + recommendations.size());
    }
}
