/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : HistoryRecord.java
 * AUTHOR      : Muhammad Adam bin Md.Yusuf (24002965)
 * DESCRIPTION : Represents a single transaction entry in the borrowing history,
 * pairing a Book with its corresponding action (borrow or return).
 * Acts as the data unit stored within the HistoryStack.
 */
package src;

public class HistoryRecord {

    // The book in the transaction and the action performed on it
    private Book book;
    private String action;

    // Creates a new history entry linking a Book to its transaction action
    public HistoryRecord(Book book, String action) {
        this.book = book;
        this.action = action;
    }

    // Getters
    public Book getBook() {
        return book;
    }

    public String getAction() {
        return action;
    }

    // Output Format
    // Returns a comma-separated string for saving this record to a CSV file
    public String toCSV() {
        return book.getIsbn() + "," + action;
    }
}
