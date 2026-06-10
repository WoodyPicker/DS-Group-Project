/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : Book.java
 * AUTHOR      : Irwina Batrisha Binti Mohd Shahar (25061717)
 * DESCRIPTION : Defines the core data model (entity) for a single library book.
 * Encapsulates the identity fields (ISBN, Title, Author) and the inventory
 * counters (Total and Available Copies) behind private access, exposing
 * controlled mutators such as borrowCopy() and returnCopy() that guard the
 * copy-count invariants, along with a toCSV() serializer used for flat-file
 * persistence.
 */
package Classes;

public class Book {
/**
 * Represents one book title in the catalogue. Every field is private so that the
 * rest of the system can only change a book's state through the methods below,
 * which keeps the copy counts consistent.
 */

    private int isbn; // Unique identifier; also the key used by the BST.
    private String title;
    private String author;
    private int totalCopies;  // How many copies the library owns in total.
    private int availableCopies;  // How many are currently on the shelf (not borrowed).

    public Book(int isbn, String title, String author, int copies) {
        this(isbn, title, author, copies, copies);
    }

    public Book(int isbn, String title, String author, int totalCopies, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }
    // Read-only accessors. The fields stay private; callers can look but not assign.
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
    /**
    * Checks out one copy. The guard makes sure we never go below zero, so the
    * count can't drift into an invalid state even if called carelessly.
    */
    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }
    /**
    * Returns one copy. The guard prevents the available count from rising above
    * the number of copies the library actually owns.
    */
    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }
    /**
    * Restocks the title by adding new physical copies to both the total and the available count.
    */
    public void addCopies(int newCopies) {
        this.totalCopies += newCopies;
        this.availableCopies += newCopies;
    }
    /**
    * Removes physical copies from the library. Rejects a count that is negative
    * or larger than what is currently available, since borrowed copies cannot be removed from the shelf.
    */
    public void removeCopies(int count) {
        if (count < 0 || count > this.availableCopies) {
            throw new IllegalArgumentException("Cannot remove more copies than available.");
        }
        this.totalCopies -= count;
        this.availableCopies -= count;
    }
    /**
    * Serialises this book into a single CSV row for flat-file storage, matching the column order the loader expects.
    */
    public String toCSV() {
        return isbn + "," + title + "," + author + "," + totalCopies + "," + availableCopies;
    }
    /**
    * Human-readable summary used whenever a book is printed to the console.
    */
    @Override
    public String toString() {
        return "[ISBN: " + isbn + "] " + title + " by " + author
                + " | Total: " + totalCopies + " (" + availableCopies + " Available)";
    }
}
