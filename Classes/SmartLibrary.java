/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : SmartLibrary.java
 * AUTHOR      : Yughendraa Karmukilan (25060111) & Irwina Batrisha binti Mohd Shahar(25061717)
 * DESCRIPTION : Serves as the concrete system controller implementing the LibraryADT 
 * interface contract. Manages user authentication contexts (Librarian 
 * vs. Student), coordinates inverted index HashMaps for constant-time 
 * text searches, routes primary key insertions safely into the BookBST 
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
    // Inverted indexes mapping lookup targets to Lists to cleanly manage hash collision buckets.
    private HashMap<String, List<Book>> titleIndex = new HashMap<>();
    private HashMap<String, List<Book>> authorIndex = new HashMap<>();
    // Variables used to track the current user and system state.
    private String userRole = "";
    private List<Integer> sessionBorrowedIsbns = new ArrayList<>();
    // File paths used to store and load application data.
    private final String CATALOGUE_FILE = "Database/catalogue.csv";
    private final String HISTORY_FILE = "Database/history.csv";
    
    /**
     * Constructor triggers automated file initialization routines upon class instantiation.
     */
    public SmartLibrary() {
        loadData();
    }
    /**
    * Loads data from CSV files and reconstructs the application's
    * in-memory collections.
    */
    private void loadData() {
    try {
        File databaseDir = new File("Database");
        if (!databaseDir.exists()) {
            databaseDir.mkdirs(); // Create the required directories if they do not already exist.
        }

        File catFile = new File(CATALOGUE_FILE);
        if (catFile.exists()) {
            Scanner reader = new Scanner(catFile);
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue; // Skip redundant blank line segments safely
                String[] data = line.split(",");
                if (data.length == 5) {
                    int isbn = Integer.parseInt(data[0].trim());
                    String title = data[1].trim();
                    String author = data[2].trim();
                    int totalCopies = Integer.parseInt(data[3].trim());
                    int availableCopies = Integer.parseInt(data[4].trim());

                    // Load the saved values and rebuild the object's state.
                    Book b = new Book(isbn, title, author, totalCopies, availableCopies);
                    catalogue.insert(b);
                    // Store text keys in a case-insensitive format for consistent lookups.
                    titleIndex.putIfAbsent(b.getTitle().toLowerCase(), new ArrayList<>());
                    titleIndex.get(b.getTitle().toLowerCase()).add(b);

                    authorIndex.putIfAbsent(b.getAuthor().toLowerCase(), new ArrayList<>());
                    authorIndex.get(b.getAuthor().toLowerCase()).add(b);
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
    /**
    * Saves application data to disk and verifies that the operation completes successfully.
    */
    private void saveData() {
        try {
            File databaseDir = new File("Database");
            if (!databaseDir.exists()) {
                databaseDir.mkdirs();
            }

            PrintWriter catWriter = new PrintWriter(CATALOGUE_FILE);
            for (Book b : catalogue.getAllBooks()) {
                catWriter.println(b.toCSV()); // Writes using safe internal sanitizer loops
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
    /**
     * Registers a unique book entity. Prevents primary duplicate index overlaps.
     * Complexity: O(log N) Average tree traversal route check.
     */
    @Override
    public void addBook(int isbn, String title, String author, int copies) {
        if (catalogue.search(isbn) != null) {
            System.out.println("Error: A book with ISBN " + isbn + " already exists.");
            return;
        }

        Book newBook = new Book(isbn, title, author, copies);
        catalogue.insert(newBook);
        // Add the new item to the author and title lookup maps.
        titleIndex.putIfAbsent(title.toLowerCase(), new ArrayList<>());
        titleIndex.get(title.toLowerCase()).add(newBook);
        authorIndex.putIfAbsent(author.toLowerCase(), new ArrayList<>());
        authorIndex.get(author.toLowerCase()).add(newBook);

        System.out.println("Success: '" + title + "' added to the catalogue with " + copies + " copies.");
    }
    /**
    * Finds the target item using a binary search before updating its data.
    */
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
    /**
    * Provides a menu for removing specific properties from an item.
    */
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
            System.out.println("Options:");
            System.out.println(" - Enter a [number] to delete that many available copies.");
            System.out.println(" - Type 'all' to delete ALL available copies.");
            System.out.println(" - Type 'purge' to completely remove the book from the system.");
            System.out.println(" - Type 'exit' to cancel and return to the menu.");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim().toLowerCase();

            if (choice.equals("exit") || choice.equals("cancel")) {
                System.out.println("Deletion cancelled. Returning to menu.");
                return;
            } else if (choice.equals("purge")) { 
                // Ensure the tree remains valid before deleting the node.
                if (b.getAvailableCopies() < b.getTotalCopies()) {
                    System.out.println("Error: Cannot purge book. " + (b.getTotalCopies() - b.getAvailableCopies()) + " copies are currently borrowed.");
                } else {
                    catalogue.delete(isbn); // Drop record nodes out from core search paths.

                    // Remove all references to the item from the indexing structures.
                    String titleKey = b.getTitle().toLowerCase();
                    if (titleIndex.containsKey(titleKey)) {
                    titleIndex.get(titleKey).remove(b);
                    if (titleIndex.get(titleKey).isEmpty()) {
                     titleIndex.remove(titleKey);}
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
            } else if (choice.equals("all")) {
                if (b.getAvailableCopies() == 0) {
                    System.out.println("Notice: No available copies to delete.");
                } else {
                    int removed = b.getAvailableCopies();
                    b.removeCopies(removed);
                    System.out.println("Success: Removed " + removed + " copies. " + b.getTotalCopies() + " total copies remain.");
                }
            } else {
                // Checks whether the input contains only digits.
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
    /**
    * Performs efficient primary-key lookups using a binary search tree.
    * Average time complexity: O(log N) due to logarithmic tree traversal.
    */
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
    /**
    * Performs fast lookups using hash-based key mapping.
    * Average time complexity is near O(1), avoiding linear scans.
    */   
    @Override
    public void searchBookByAuthor(String author) {
        List<Book> books = authorIndex.get(author.toLowerCase());
        // Prevents errors when the query is null.
        if (books != null && !books.isEmpty()) {
            System.out.println("Found books by " + author + ":");
            for (Book b : books) {
                System.out.println(b.toString());
            }
        } else {
            System.out.println("Result: No books found by author '" + author + "'.");
        }
    }
    /**
    * Decrements available units and logs a tracking flag instance onto the HistoryStack.
    */
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
                sessionBorrowedIsbns.add(isbn); // Append tracking token onto active list profile
                history.push(b, "Borrowed"); // Push onto chronological log stack
                System.out.println("Success: You borrowed '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies remaining.");
            } else {
                System.out.println("Error: '" + b.getTitle() + "' is currently out of stock.");
            }
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " does not exist in the catalogue.");
        }
    }
    /**
    * Restores shelf capacities and registers operation status maps.
    */
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
    /**
    * Direct call mapping out to the HistoryStack controller interface method.
    */
    @Override
    public void viewLatestHistory() {
        history.show();
    }
    /**
    * Filters repository data to find items that are currently on loan.
    */
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
    /**
    * Performs an in-order traversal to output the tree in sorted order.
    */
    @Override
    public void printWholeCatalogue() {
        catalogue.printInOrder();
    }
    /**
    * Initializes the interactive control loop for the terminal interface.
    */
    @Override
    public void runMenu() {
        System.out.println("\nWelcome to the Smart Library System");

        while (true) {
            System.out.println("\n--- Login ---");
            System.out.println("Are you logging in as a Student, Librarian, or Shut Down?");
            System.out.println("  1. Student");
            System.out.println("  2. Librarian");
            System.out.println("  3. Shut Down");

            while (true) {
                System.out.print("Choice: ");
                String roleChoice = sc.nextLine().trim();
                if (roleChoice.equals("1")) {
                    userRole = "Student";
                    sessionBorrowedIsbns.clear(); // Clear session state upon new login context
                    break;
                } else if (roleChoice.equals("2")) {
                    userRole = "Librarian";
                    sessionBorrowedIsbns.clear(); // Clear session state upon new login context
                    break;
                } else if (roleChoice.equals("3")) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }
                System.out.println("Invalid choice. Enter 1, 2, or 3.");
            }

            System.out.println("\nLogged in successfully as: " + userRole);

            boolean loggedIn = true;
            while (loggedIn) {
                printMenu();
                System.out.print("Enter Command: ");
                String command = sc.nextLine().trim().toLowerCase();

                if (command.equals("logout") || command.equals("10")) {
                    System.out.println("Logging out...");
                    sessionBorrowedIsbns.clear(); // Safely clear tracking state on explicit logout
                    loggedIn = false;
                    break;
                }

                if (command.equals("exit") || command.equals("quit") || command.equals("11")) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }

                handleChoice(command); // Pass raw command strings safely out to route handlers.
            }
        }
    }
    /**
    * Formats and displays available options based on user role.
    */
    private void printMenu() {
        System.out.println("\n<<----- SmartLibrary CLI Navigation (" + userRole + ") ----->>");
        System.out.println("Type one of the following command keywords to execute an action:\n");

        if (userRole.equals("Librarian")) {
            System.out.printf("  %-12s -> %s\n", " add", "Register a completely new book title");
            System.out.printf("  %-12s -> %s\n", " restock", "Add physical copies to an existing book");
            System.out.printf("  %-12s -> %s\n", " delete", "Remove copies or purge a book entirely");
        } else {
            System.out.println("  [Inventory Management Commands Locked for Students]");
        }

        System.out.printf("  %-12s -> %s\n", " search", "Find a book via ISBN, Title, or Author");
        System.out.printf("  %-12s -> %s\n", " borrow", "Checkout a book copy");
        System.out.printf("  %-12s -> %s\n", " return", "Check-in a borrowed book copy");
        System.out.printf("  %-12s -> %s\n", " history", "View the chronological library audit log");
        System.out.printf("  %-12s -> %s\n", " borrowed", "List all books currently missing copies");
        System.out.printf("  %-12s -> %s\n", " catalog", "Print the complete library collection sorted by ISBN");
        System.out.printf("  %-12s -> %s\n", " logout", "Log out of current profile back to main login panel");
        System.out.printf("  %-12s -> %s\n", " exit", "Save database metrics and safely kill the application process");
        System.out.println("<<----------------------------------------------------------------->>");
    }
    /**
    * Evaluates route flags mapping string inputs safely out to structural method calls.
    */
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
                    // Removes commas to prevent issues when writing database rows.
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
                }
                try {
                    System.out.print("Enter ISBN to restock: ");
                    int isbn = Integer.parseInt(sc.nextLine().trim());
                    if (isbn <= 0) {
                        System.out.println("Error: ISBN must be a positive number.");
                        break;
                    }
                    System.out.print("Enter number of additional copies: ");
                    int copies = Integer.parseInt(sc.nextLine().trim());
                    if (copies < 1) {
                        System.out.println("Error: Number of copies must be greater than 0.");
                        break;
                    }
                    addCopiesToBook(isbn, copies);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Numeric inputs must be valid integers.");
                }
                break;

            case "delete", "3":
                if (!userRole.equals("Librarian")) {
                    System.out.println("Permission Denied: Only Librarians can manage library inventory.");
                    break;
                }
                deleteBook();
                break;

            case "search", "4":
                System.out.println("Search options: 1. ISBN | 2. Title | 3. Author");
                System.out.print("Choice: ");
                String searchType = sc.nextLine().trim();

                if (searchType.equals("1") || searchType.equalsIgnoreCase("isbn")) {
                    try {
                        System.out.print("Enter ISBN: ");
                        searchBookByIsbn(Integer.parseInt(sc.nextLine().trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: ISBN must be a valid integer.");
                    }
                } else if (searchType.equals("2") || searchType.equalsIgnoreCase("title")) {
                    System.out.print("Enter Title: ");
                    String titleQuery = sc.nextLine().trim();
                    if (titleQuery.isEmpty()) {
                        System.out.println("Error: Title cannot be empty.");
                    } else {
                        searchBookByTitle(titleQuery);
                    }
                } else if (searchType.equals("3") || searchType.equalsIgnoreCase("author")) {
                    System.out.print("Enter Author: ");
                    String authorQuery = sc.nextLine().trim();
                    if (authorQuery.isEmpty()) {
                        System.out.println("Error: Author name cannot be empty.");
                    } else {
                        searchBookByAuthor(authorQuery);
                    }
                } else {
                    System.out.println("Invalid search options context.");
                }
                break;

            case "borrow", "5":
                try {
                    System.out.print("Enter ISBN to borrow: ");
                    borrowBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case "return", "6":
                try {
                    System.out.print("Enter ISBN to return: ");
                    returnBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case "history", "7":
                viewLatestHistory();
                break;

            case "borrowed", "8":
                viewBorrowedBooks();
                break;

            case "catalog", "9":
                printWholeCatalogue();
                break;

            default:
                System.out.println("Unknown command flag. Look at the guidelines chart above for reference.");
        }
    }
}