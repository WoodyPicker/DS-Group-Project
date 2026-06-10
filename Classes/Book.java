/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : Book.java
 * AUTHOR      : Ali Naif Ahmed Jiawi (25071095)
 * DESCRIPTION : Represents a single book entity in the library system, storing
 * its ISBN, title, author, and copy counts. Provides methods to
 * borrow, return, add, and remove copies with basic validation.
 */
package Classes;

public class Book {

    // Core book details kept private to enforce encapsulation
    private int isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    // Creates a Book where all copies are initially available
    public Book(int isbn, String title, String author, int copies) {
        this(isbn, title, author, copies, copies);
    }

    /**
     * Creates a Book with separate total and available copy counts
     * Used when loading existing records
     */
    public Book(int isbn, String title, String author, int totalCopies, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Getters

    public int getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    // Copy Management

    /**
     * Decreases available copies by 1 when a book is borrowed
     * Does nothing if no copies are currently available
     */
    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    /**
     *  Increases available copies by 1 when a book is returned
     * Does nothing if all copies are already in stock
     */
    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    // Adds new physical copies to both the total and available counts
    public void addCopies(int newCopies) {
        this.totalCopies += newCopies;
        this.availableCopies += newCopies;
    }

    /**
     * Removes copies from the library, throws an exception if the count is negative
     * or exceeds the number of currently available copies
     */
    public void removeCopies(int count) {
        if (count < 0 || count > this.availableCopies) {
            throw new IllegalArgumentException("Cannot remove more copies than available.");
        }
        this.totalCopies -= count;
        this.availableCopies -= count;
    }

    // Output Formats

    // Returns a comma-separated string for saving book data to a CSV file

    public String toCSV() {
        return isbn + "," + title + "," + author + "," + totalCopies + "," + availableCopies;
    }

    // Returns a formatted string of the book's details for console display
    @Override
    public String toString() {
        return "[ISBN: " + isbn + "] " + title + " by " + author
                + " | Total: " + totalCopies + " (" + availableCopies + " Available)";
    }
}
