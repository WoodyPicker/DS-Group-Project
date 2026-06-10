/*
 * COURSE      : WIA1002 Data Structure
 * GROUP       : GROUP 1 OCC 9
 * PROJECT     : SMART LIBRARY MANAGEMENT SYSTEM
 * COMPONENT   : LibraryADT
 * AUTHOR      : Nur Alya Natasya Binti Mohamad Jaffry (24002061)
 * DESCRIPTION : The LibraryADT is the core architectural blueprint for your Smart Library system. 
                 As a Java interface, it contains no actual code. Instead, it defines the strict 
                 rules your team must follow for adding, searching, borrowing, returning, and 
                 deleting books, ensuring the final program is organized, secure, and perfectly structured.
 */
package Classes;

public interface LibraryADT {

    // Starts the whole program — shows the login screen, then loops the role-based menu.
    // Main calls this once and hands control over to the system.
    void runMenu();

    // Adds a brand new book to the BST. 
    // We added 'copies' so multiple students can borrow the same book at once.
    void addBook(int isbn, String title, String author, int copies);

    // Use this to update the inventory quantity of a book already in the system.
    // It finds the existing node by ISBN and increments the copy count.
    void addCopiesToBook(int isbn, int copies);
   
    // The search uses the BST key.
    void searchBookByIsbn(int isbn);

    // This will probably require traversing the whole tree since it's not sorted by title,
    // but it's much more user-friendly than forcing them to memorize ISBNs
    void searchBookByTitle(String title);

    // Same here, good for finding all books written by a specific author.
    void searchBookByAuthor(String author);

    // Moves a book from the catalogue to the student's history stack.
    // Remember to decrease the copy count in the BST when this happens
    void borrowBook(int isbn);

    // Puts the book back into the catalogue (increases the copy count).
    void returnBook(int isbn);
    
    // Handle book deletion by option
    void deleteBook();

    // Pops/iterates through the Stack to show the most recently borrowed books first (LIFO).
    void viewLatestHistory();

    // Used to show everything a user currently has checked out,
    // rather than their entire past history.
    void viewBorrowedBooks(); 

    // Prints every single book in the BST. 
    void printWholeCatalogue();
    
}