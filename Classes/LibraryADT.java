/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : (classname)
 * AUTHOR      : (name)
 * DESCRIPTION : (describe what u used like in my class(historystack))
 */
package Classes;

public interface LibraryADT {
    void runMenu();
    void addBook(int isbn, String title, String author, int copies);
    void addCopiesToBook(int isbn, int copies);
    void deleteBook();
    void searchBookByIsbn(int isbn);
    void searchBookByTitle(String title);
    void searchBookByAuthor(String author);
    void borrowBook(int isbn);
    void returnBook(int isbn);
    void viewLatestHistory();
    void viewBorrowedBooks();
    void printWholeCatalogue();
}