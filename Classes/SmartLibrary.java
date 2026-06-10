/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : SmartLibrary.java
 * AUTHOR      : Yughendraa Karmukilan (25060111) & Irwina Batrisha binti Mohd Shahar(25061717)
 * DESCRIPTION : Serves as the concrete system controller implementing the LibraryADT
 * interface contract. Manages user authentication contexts (Librarian
 * vs. Student), coordinates auxiliary index HashMaps for fast text
 * searches, routes primary key insertions safely into the BookBST
 * engine, logs operational transactions onto the HistoryStack, and
 * drives flat-file CSV synchronization mechanisms.
 */
package Classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SmartLibrary implements LibraryADT {

    // Keep internal implementation details hidden from client code.
    private BookBST catalogue = new BookBST();
    private HistoryStack history = new HistoryStack();
    private Scanner sc = new Scanner(System.in);
    // Auxiliary indexes mapping lookup targets to Lists to cleanly manage duplicate keys.
    private HashMap<String, List<Book>> titleIndex = new HashMap<>();
    private HashMap<String, List<Book>> authorIndex = new HashMap<>();
    // Variables used to track the current user and system state.
    private String userRole = "";
    private List<Integer> sessionBorrowedIsbns = new ArrayList<>();
    // File paths used to store and load application data.
    private final String CATALOGUE_FILE = "Database/catalogue.csv";
    private final String HISTORY_FILE = "Database/history.csv";

    public SmartLibrary() {
        loadData();
    }

    private void loadData() {
    try {
        File databaseDir = new File("Database");
        if (!databaseDir.exists()) {
            databaseDir.mkdirs();
        }

        File catFile = new File(CATALOGUE_FILE);
        if (catFile.exists()) {
            Scanner reader = new Scanner(catFile);
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length == 5) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String author = data[2].trim();
                    int totalCopies = Integer.parseInt(data[3].trim());
                    int availableCopies = Integer.parseInt(data[4].trim());

                    Book b = new Book(isbn, title, author, totalCopies, availableCopies);
                    catalogue.insert(b);
                    titleIndex.putIfAbsent(b.getTitle().toLowerCase(), new ArrayList<>());
                    titleIndex.get(b.getTitle().toLowerCase()).add(b);

                    authorIndex.putIfAbsent(b.getAuthor().toLowerCase(), new ArrayList<>());
                    authorIndex.get(b.getAuthor().toLowerCase()).add(b);
                }
            }

        File histFile = new File(HISTORY_FILE);
        if (histFile.exists()) {
            Scanner reader = new Scanner(histFile);
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length == 2) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String status = data[1].trim();

                    Book b = catalogue.search(isbn);
                    if (b != null) {
                        history.push(b, status);
                    }
                }
                reader.close();
            }

            File histFile = new File(HISTORY_FILE);
            if (histFile.exists()) {
                Scanner reader = new Scanner(histFile);
                while (reader.hasNextLine()) {
                    String line = reader.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] data = line.split(",");
                    if (data.length == 2) {
                        int isbn = Integer.parseInt(data[0].trim());
                        String status = data[1].trim();

                        // Link the stored records back to their corresponding data objects.
                        Book b = catalogue.search(isbn);
                        if (b != null) {
                            history.push(b, status);
                        }
                    }
                }
                reader.close();
            }
            System.out.println("System Initialised: Data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("System Initialised: No previous save data found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Warning: Could not perfectly load all save data.");
        }
    }
}

    private void saveData() {
        try {
            File databaseDir = new File("Database");
            if (!databaseDir.exists()) {
                databaseDir.mkdirs();
            }

            PrintWriter catWriter = new PrintWriter(CATALOGUE_FILE);
            for (Book b : catalogue.getAllBooks()) {
                catWriter.println(b.toCSV());
            }
            catWriter.close();

            PrintWriter histWriter = new PrintWriter(HISTORY_FILE);
            for (HistoryRecord r : history.getAllRecords()) {
                histWriter.println(r.toCSV());
            }
            histWriter.close();

            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error: Could not save data to files.");
        }
    }

    @Override
    public void addBook(int isbn, String title, String author, int copies) {
        if (catalogue.search(isbn) != null) {
            System.out.println("Error: A book with ISBN " + isbn + " already exists.");
            return;
        }

        Book newBook = new Book(isbn, title, author, copies);
        catalogue.insert(newBook);
        titleIndex.putIfAbsent(title.toLowerCase(), new ArrayList<>());
        titleIndex.get(title.toLowerCase()).add(newBook);
        authorIndex.putIfAbsent(author.toLowerCase(), new ArrayList<>());
        authorIndex.get(author.toLowerCase()).add(newBook);

        System.out.println("Success: '" + title + "' added to the catalogue with " + copies + " copies.");
    }

    @Override
    public void addCopiesToBook(int isbn, int copies) {
        Book b = catalogue.search(isbn);
        if (b != null) {
            b.addCopies(copies);
            System.out.println("Success: Added " + copies + " copies. '" + b.getTitle() + "' now has " + b.getTotalCopies() + " total copies.");
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " not found.");
        }
    }

    @Override
    public void deleteBook() {
        try {
            System.out.print("Enter ISBN to delete: ");
            int isbn = Integer.parseInt(sc.nextLine().trim());
            if (isbn <= 0) {
                System.out.println("Error: ISBN must be a positive number.");
                return;
            }
            Book b = catalogue.search(isbn);

            if (b == null) {
                System.out.println("Error: Book with ISBN " + isbn + " not found.");
                return;
            }

            System.out.println("\nSelected: " + b.toString());
            System.out.println("Delete options:");
            System.out.println("1. Delete a specific number of copies");
            System.out.println("2. Delete ALL available copies");
            System.out.println("3. Purge book completely from system");
            System.out.println("4. Cancel and go back");
            System.out.print("Choice: ");

            int deleteChoice;
            try {
                deleteChoice = Integer.parseInt(sc.nextLine().trim()); // read the sub-menu choice as a number
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a number between 1 and 4.");
                return;
            } else if (choice.equals("purge")) {
                if (b.getAvailableCopies() < b.getTotalCopies()) {
                    System.out.println("Error: Cannot purge book. " + (b.getTotalCopies() - b.getAvailableCopies()) + " copies are currently borrowed.");
                } else {
                    catalogue.delete(isbn);

                    String titleKey = b.getTitle().toLowerCase();
                    if (titleIndex.containsKey(titleKey)) {
                        titleIndex.get(titleKey).remove(b);
                        if (titleIndex.get(titleKey).isEmpty()) {
                            titleIndex.remove(titleKey);
                        }
                    }
                    String authorKey = b.getAuthor().toLowerCase();
                    if (authorIndex.containsKey(authorKey)) {
                        authorIndex.get(authorKey).remove(b);
                        if (authorIndex.get(authorKey).isEmpty()) {
                            authorIndex.remove(authorKey);
                        }
                    }
                    System.out.println("Success: All records of '" + b.getTitle() + "' have been deleted.");
                }

            } else if (deleteChoice == 4) {
                // User chose to cancel — return to menu without doing anything
                System.out.println("Deletion cancelled. Returning to menu.");

            } else {
                if (choice.matches("\\d+")) {
                    int amount = Integer.parseInt(choice);
                    if (amount <= 0) {
                        System.out.println("Error: Amount must be greater than 0.");
                    } else if (amount > b.getAvailableCopies()) {
                        System.out.println("Error: Cannot delete " + amount + " copies. Only " + b.getAvailableCopies() + " are available.");
                    } else {
                        b.removeCopies(amount);
                        System.out.println("Success: Removed " + amount + " copies. " + b.getAvailableCopies() + " available copies remain.");
                    }
                } else {
                    System.out.println("Error: Invalid option selection. Expected a number, 'all', 'purge', or 'exit'.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Expected a valid integer for ISBN.");
        }
    }

    @Override
    public void searchBookByIsbn(int isbn) {
        Book b = catalogue.search(isbn);
        if (b != null) {
            System.out.println("Found: " + b.toString());
        } else {
            System.out.println("Result: Book with ISBN " + isbn + " not found.");
        }
    }

    // Iterates through text entries and checks for substring matches.
    @Override
    public void searchBookByTitle(String title) {
        String lowerQuery = title.toLowerCase();
        boolean found = false;
        for (String storedTitle : titleIndex.keySet()) {
            if (storedTitle.contains(lowerQuery)) {
                for (Book b : titleIndex.get(storedTitle)) {
                    System.out.println("Found: " + b.toString());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Result: No book found containing the title '" + title + "'.");
        }
    }
    if (!found) {
        System.out.println("Result: No book found containing the title '" + title + "'.");
    }
}

    @Override
    public void searchBookByAuthor(String author) {
        List<Book> books = authorIndex.get(author.toLowerCase());
        if (books != null && !books.isEmpty()) {
            System.out.println("Found books by " + author + ":");
            for (Book b : books) {
                System.out.println(b.toString());
            }
        } else {
            System.out.println("Result: No books found by author '" + author + "'.");
        }
    }

    @Override
    public void borrowBook(int isbn) {
        if (isbn <= 0) {
            System.out.println("Error: ISBN must be a positive number.");
            return;
        }
        Book b = catalogue.search(isbn);
        if (b != null) {
            if (b.getAvailableCopies() > 0) {
                b.borrowCopy();
                sessionBorrowedIsbns.add(isbn);
                history.push(b, "Borrowed");
                System.out.println("Success: You borrowed '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies remaining.");
            } else {
                System.out.println("Error: '" + b.getTitle() + "' is currently out of stock.");
            }
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " does not exist in the catalogue.");
        }
    }

    @Override
    public void returnBook(int isbn) {
        if (isbn <= 0) {
            System.out.println("Error: ISBN must be a positive number.");
            return;
        }
        Book b = catalogue.search(isbn);
        if (b == null) {
            System.out.println("Error: This library does not own a book with ISBN " + isbn + ".");
            return;
        }
        if (b.getAvailableCopies() < b.getTotalCopies()) {
            b.returnCopy();
            sessionBorrowedIsbns.remove(Integer.valueOf(isbn)); // Clear session loan parameters safely.
            history.push(b, "Returned");
            System.out.println("Success: You returned '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies now available.");
        } else {
            System.out.println("Notice: All copies of '" + b.getTitle() + "' are already in the library.");
        }
    }
    Book b = catalogue.search(isbn);
    if (b == null) {
        System.out.println("Error: This library does not own a book with ISBN " + isbn + ".");
        return;
    }
    if (b.getAvailableCopies() < b.getTotalCopies()) {
        b.returnCopy();
        sessionBorrowedIsbns.remove(Integer.valueOf(isbn));
        history.push(b, "Returned");
        System.out.println("Success: You returned '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies now available.");
    } else {
        System.out.println("Notice: All copies of '" + b.getTitle() + "' are already in the library.");
    }
}

    @Override
    public void viewLatestHistory() {
        history.show();
    }

    @Override
    public void viewBorrowedBooks() {
        System.out.println("\n<<--- Currently Borrowed Books --->>");
        List<Book> allBooks = catalogue.getAllBooks();
        boolean found = false;

        for (Book b : allBooks) {
            int borrowedAmount = b.getTotalCopies() - b.getAvailableCopies();
            if (borrowedAmount > 0) {
                System.out.println("[ISBN: " + b.getIsbn() + "] " + b.getTitle()
                        + " | Copies Out: " + borrowedAmount);
                found = true;
            }
        }

        if (!found) {
            System.out.println("All books are currently in the library.");
        }
        System.out.println("<<-------------------------------->>");
    }

    @Override
    public void printWholeCatalogue() {
        catalogue.printInOrder();
    }

    @Override
    public void runMenu() {
        System.out.println("\nWelcome to the Smart Library System");

        while (true) { // outer loop — keeps showing login screen after each logout

            while (true) {
                System.out.print("Choice: ");
                String roleChoice = sc.nextLine().trim();
                if (roleChoice.equals("1")) {
                    userRole = "Student";
                    sessionBorrowedIsbns.clear();
                    break;
                } else if (roleChoice.equals("2")) {
                    userRole = "Librarian";
                    sessionBorrowedIsbns.clear();
                    break;
                } else if (roleChoice.equals("3")) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }
                System.out.println("Invalid choice. Enter 1, 2, or 3.");
            }

            System.out.println("\nLogged in successfully as: " + userRole);

            boolean loggedIn = true; // controls the inner loop
            while (loggedIn) {       // inner loop — keeps showing the menu until logout

                if (command.equals("logout") || command.equals("10")) {
                    System.out.println("Logging out...");
                    sessionBorrowedIsbns.clear();
                    loggedIn = false;
                    break;
                }

                // Save and Exit — Student = 7, Librarian = 10
                if ((userRole.equals("Student") && choice == 7)
                        || (userRole.equals("Librarian") && choice == 10)) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();  // write all books and history to CSV files before exiting
                    return;      // exit runMenu() entirely, ends the program

                handleChoice(command);
            }
        }
    }

    private void printMenu() {
        if (userRole.equals("Student")) {
            System.out.println("\n<<----- Smart Library - Student Menu ----->>");
            System.out.println("1. Search Book");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. View History");
            System.out.println("5. View Borrowed Books");
            System.out.println("6. View Full Catalogue");
            System.out.println("7. Save and Exit");
            System.out.println("8. Logout");
            System.out.println("<<---------------------------------------->>");

        } else if (userRole.equals("Librarian")) {
            System.out.println("\n<<----- Smart Library - Librarian Menu ----->>");
            System.out.println("1. Add Book");             // Librarian only — adds a new book
            System.out.println("2. Restock Book");         // Librarian only — adds more copies
            System.out.println("3. Delete Book");          // Librarian only — removes copies or purges
            System.out.println("4. Search Book");
            System.out.println("5. Borrow Book");
            System.out.println("6. Return Book");
            System.out.println("7. View History");
            System.out.println("8. View Borrowed Books");
            System.out.println("9. View Full Catalogue");
            System.out.println("10. Save and Exit");
            System.out.println("11. Logout");
            System.out.println("<<------------------------------------------>>");
        }
    }

    private void handleChoice(String command) {
        switch (command) {
            case "add", "1":
                if (!userRole.equals("Librarian")) {
                    System.out.println("Permission Denied: Only Librarians can manage library inventory.");
                    break;
                }
                try {
                    System.out.print("Enter ISBN (Numbers only): ");
                    int isbn = Integer.parseInt(sc.nextLine().trim());
                    if (isbn <= 0) {
                        System.out.println("Error: ISBN must be a positive number.");
                        break;
                    }
                    System.out.print("Enter Title: ");
                    String title = sc.nextLine().replace(",", " ").trim();
                    if (title.isEmpty()) {
                        System.out.println("Error: Title cannot be empty.");
                        break;
                    }
                    System.out.print("Enter Author: ");
                    String author = sc.nextLine().replace(",", " ").trim();
                    if (author.isEmpty()) {
                        System.out.println("Error: Author cannot be empty.");
                        break;
                    }
                    System.out.print("Enter Initial Number of Copies: ");
                    int copies = Integer.parseInt(sc.nextLine().trim());
                    if (copies < 1) {
                        System.out.println("Error: Number of copies must be at least 1.");
                        break;
                    }
                    addBook(isbn, title, author, copies);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Numeric inputs must be valid integers.");
                }
                break;

            case "restock", "2":
                if (!userRole.equals("Librarian")) {
                    System.out.println("Permission Denied: Only Librarians can manage library inventory.");
                    break;
                case 3:
                    handleReturn();        // return a previously borrowed book
                    break;
                case 4:
                    viewLatestHistory();   // show history stack in LIFO order
                    break;
                case 5:
                    viewBorrowedBooks();   // show all books with copies currently out
                    break;
                case 6:
                    printWholeCatalogue(); // print all books sorted by ISBN (BST in-order traversal)
                    break;

                default:
                    System.out.println("Error: Please enter a number between 1 and 8.");
            }

        } else if (userRole.equals("Librarian")) {
            switch (choice) {
                case 1:
                    handleAddBook();       // add a brand new book to the catalogue
                    break;
                case 2:
                    handleRestock();       // add more copies to an existing book
                    break;
                case 3:
                    deleteBook();          // remove copies or fully purge a book
                    break;
                case 4:
                    handleSearch();        // search by ISBN, title, or author
                    break;
                case 5:
                    handleBorrow();        // borrow a book by ISBN
                    break;
                case 6:
                    handleReturn();        // return a previously borrowed book
                    break;
                case 7:
                    viewLatestHistory();   // show history stack in LIFO order
                    break;
                case 8:
                    viewBorrowedBooks();   // show all books with copies currently out
                    break;
                case 9:
                    printWholeCatalogue(); // print all books sorted by ISBN (BST in-order traversal)
                    break;

                default:
                    System.out.println("Error: Please enter a number between 1 and 11.");
            }
        }
    }
}
