/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : (classname)
 * AUTHOR      : (name)
 * DESCRIPTION : (describe what u used like in my class(historystack))
 */
package Classes;

public class HistoryRecord {

    private Book book;
    private String action;

    public HistoryRecord(Book book, String action) {
        this.book = book;
        this.action = action;
    }

    public Book getBook() {
        return book;
    }

    public String getAction() {
        return action;
    }

    public String toCSV() {
        return book.getIsbn() + "," + action;
    }
}
