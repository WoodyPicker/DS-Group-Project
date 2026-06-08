/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : (classname)
 * AUTHOR      : (name)
 * DESCRIPTION : (describe what u used like in my class(historystack))
 */
package Classes;

public class Book {

    private int isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

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

    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public void addCopies(int newCopies) {
        this.totalCopies += newCopies;
        this.availableCopies += newCopies;
    }

    public void removeCopies(int count) {
        if (count < 0 || count > this.availableCopies) {
            throw new IllegalArgumentException("Cannot remove more copies than available.");
        }
        this.totalCopies -= count;
        this.availableCopies -= count;
    }

    public String toCSV() {
        return isbn + "," + title + "," + author + "," + totalCopies + "," + availableCopies;
    }

    @Override
    public String toString() {
        return "[ISBN: " + isbn + "] " + title + " by " + author
                + " | Total: " + totalCopies + " (" + availableCopies + " Available)";
    }
}
