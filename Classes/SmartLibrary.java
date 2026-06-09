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
            }

            if (deleteChoice == 1) {
                // Remove a specific number of available copies
                System.out.print("Enter number of copies to remove: ");
                int amount = Integer.parseInt(sc.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("Error: Amount must be greater than 0.");
                } else if (amount > b.getAvailableCopies()) {
                    System.out.println("Error: Cannot delete " + amount + " copies. Only " + b.getAvailableCopies() + " are available.");
                } else {
                    b.removeCopies(amount);
                    System.out.println("Success: Removed " + amount + " copies. " + b.getAvailableCopies() + " available copies remain.");
                }

            } else if (deleteChoice == 2) {
                // Remove every available copy
                if (b.getAvailableCopies() == 0) {
                    System.out.println("Notice: No available copies to delete.");
                } else {
                    int removed = b.getAvailableCopies();
                    b.removeCopies(removed);
                    System.out.println("Success: Removed all " + removed + " available copies.");
                }

            } else if (deleteChoice == 3) {
                // Fully remove the book — only allowed if no copies are currently borrowed
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
                System.out.println("Error: Invalid choice. Please enter 1, 2, 3, or 4.");
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

        while (true) { // outer loop — keeps showing login screen after each logout

            // --- Login Screen ---
            System.out.println("\n--- Login ---");
            System.out.println("1. Login as Student");
            System.out.println("2. Login as Librarian");
            System.out.println("3. Save and Shut Down");
            System.out.print("Choice: ");

            int loginChoice;
            try {
                loginChoice = Integer.parseInt(sc.nextLine().trim()); // read login choice as a number
            } catch (NumberFormatException e) {
                // User typed letters instead of a number
                System.out.println("Error: Please enter 1, 2, or 3.");
                continue; // restart the outer loop to show login again
            }

            if (loginChoice == 1) {
                userRole = "Student";
                sessionBorrowedIsbns.clear(); // clear session state upon new login
            } else if (loginChoice == 2) {
                userRole = "Librarian";
                sessionBorrowedIsbns.clear(); // clear session state upon new login
            } else if (loginChoice == 3) {
                // User wants to shut down — save all data then exit
                System.out.println("Saving and shutting down. Goodbye!");
                saveData(); // write all books and history to CSV files
                return;     // exit runMenu(), which ends the program
            } else {
                System.out.println("Error: Please enter 1, 2, or 3.");
                continue; // invalid number — show login again
            }

            System.out.println("\nLogged in successfully as: " + userRole);

            boolean loggedIn = true; // controls the inner loop
            while (loggedIn) {       // inner loop — keeps showing the menu until logout

                printMenu(); // show the correct menu for the current role
                System.out.print("Enter your choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine().trim()); // read menu choice as a number
                } catch (NumberFormatException e) {
                    System.out.println("Error: Please enter a valid number.");
                    continue; // restart inner loop to show menu again
                }

                if ((userRole.equals("Student") && choice == 7)
                        || (userRole.equals("Librarian") && choice == 11)) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();        // write all books and history to CSV files before exiting
                    return;            // exit runMenu() entirely, ends the program


                } else if (userRole.equals("Student") && choice == 8) {
                    System.out.println("Logging out...");
                    sessionBorrowedIsbns.clear(); // clear borrow tracking on logout
                    loggedIn = false;             // exit inner loop, go back to login screen

                } else if (userRole.equals("Librarian") && choice == 12) {
                    System.out.println("Logging out...");
                    sessionBorrowedIsbns.clear(); // clear borrow tracking on logout
                    loggedIn = false;             // exit inner loop, go back to login screen

                } else {
                    handleChoice(choice); // route the number to the correct method
                }
            }
        }
    }

    /**
     * Formats and displays available options based on user role.
     */
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
            System.out.println("1. Add Book");              // Librarian only — adds a new book
            System.out.println("2. Restock Book");          // Librarian only — adds more copies
            System.out.println("3. Delete Book");           // Librarian only — removes copies or purges
            System.out.println("4. Search Book");
            System.out.println("5. Borrow Book");
            System.out.println("6. Return Book");
            System.out.println("7. View History");
            System.out.println("8. View Borrowed Books");
            System.out.println("9. View Full Catalogue");
            System.out.println("10. Print Whole Catalogue");
            System.out.println("11. Save and Exit");
            System.out.println("12. Logout");
            System.out.println("<<------------------------------------------>>");
        }
    }

    /**
     * Evaluates route flags mapping string inputs safely out to structural method calls.
     */
    private void handleChoice(int choice) {
        if (userRole.equals("Student")) {
            switch (choice) {
                case 1:
                    handleSearch();        // search by ISBN, title, or author
                    break;
                case 2:
                    handleBorrow();        // borrow a book by ISBN
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
                case 10:
                    printWholeCatalogue(); // print all books sorted by ISBN (BST in-order traversal)
                    break;

                default:
                    System.out.println("Error: Please enter a number between 1 and 12.");
            }
        }
    }

    /**
     * handleAddBook() — reads all inputs then calls addBook()
     */
    private void handleAddBook() {
        try {
            System.out.print("Enter ISBN (Numbers only): ");
            int isbn = Integer.parseInt(sc.nextLine().trim()); // read ISBN, convert to int
            if (isbn <= 0) {
                System.out.println("Error: ISBN must be a positive number.");
                return;
            }
            System.out.print("Enter Title: ");
            // Removes commas to prevent issues when writing database rows.
            String title = sc.nextLine().replace(",", " ").trim();
            if (title.isEmpty()) {
                System.out.println("Error: Title cannot be empty.");
                return;
            }
            System.out.print("Enter Author: ");
            String author = sc.nextLine().replace(",", " ").trim();
            if (author.isEmpty()) {
                System.out.println("Error: Author cannot be empty.");
                return;
            }
            System.out.print("Enter Initial Number of Copies: ");
            int copies = Integer.parseInt(sc.nextLine().trim()); // read copies, convert to int
            if (copies < 1) {
                System.out.println("Error: Number of copies must be at least 1.");
                return;
            }
            addBook(isbn, title, author, copies); // pass all validated inputs to addBook()
        } catch (NumberFormatException e) {
            System.out.println("Error: Numeric inputs must be valid integers.");
        }
    }

    /**
     * handleRestock() — reads inputs then calls addCopiesToBook()
     */
    private void handleRestock() {
        try {
            System.out.print("Enter ISBN to restock: ");
            int isbn = Integer.parseInt(sc.nextLine().trim()); // read ISBN, convert to int
            if (isbn <= 0) {
                System.out.println("Error: ISBN must be a positive number.");
                return;
            }
            System.out.print("Enter number of additional copies: ");
            int copies = Integer.parseInt(sc.nextLine().trim()); // read copies, convert to int
            if (copies < 1) {
                System.out.println("Error: Number of copies must be greater than 0.");
                return;
            }
            addCopiesToBook(isbn, copies); // pass validated inputs to addCopiesToBook()
        } catch (NumberFormatException e) {
            System.out.println("Error: Numeric inputs must be valid integers.");
        }
    }

    /**
     * handleSearch() — shows a sub-menu then calls the correct search method
     */
    private void handleSearch() {
        System.out.println("Search options: 1. ISBN | 2. Title | 3. Author");
        System.out.print("Choice: ");

        int searchChoice;
        try {
            searchChoice = Integer.parseInt(sc.nextLine().trim()); // read search type as number
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter 1, 2, or 3.");
            return;
        }

        if (searchChoice == 1) {
            // Search by ISBN — uses BST recursive search, O(log n)
            try {
                System.out.print("Enter ISBN: ");
                searchBookByIsbn(Integer.parseInt(sc.nextLine().trim()));
            } catch (NumberFormatException e) {
                System.out.println("Error: ISBN must be a valid integer.");
            }
        } else if (searchChoice == 2) {
            // Search by title — uses titleIndex HashMap, substring match
            System.out.print("Enter Title: ");
            String titleQuery = sc.nextLine().trim();
            if (titleQuery.isEmpty()) {
                System.out.println("Error: Title cannot be empty.");
            } else {
                searchBookByTitle(titleQuery);
            }
        } else if (searchChoice == 3) {
            // Search by author — uses authorIndex HashMap, O(1)
            System.out.print("Enter Author: ");
            String authorQuery = sc.nextLine().trim();
            if (authorQuery.isEmpty()) {
                System.out.println("Error: Author name cannot be empty.");
            } else {
                searchBookByAuthor(authorQuery);
            }
        } else {
            System.out.println("Error: Please enter 1, 2, or 3.");
        }
    }

    /**
     *handleBorrow() — reads ISBN input then calls borrowBook()
     */

    private void handleBorrow() {
        try {
            System.out.print("Enter ISBN to borrow: ");
            borrowBook(Integer.parseInt(sc.nextLine().trim())); // read ISBN and pass directly to borrowBook()
        } catch (NumberFormatException e) {
            System.out.println("Error: ISBN must be a valid integer.");
        }
    }

    /**
     *handleReturn() — reads ISBN input then calls returnBook()
     */
    private void handleReturn() {
        try {
            System.out.print("Enter ISBN to return: ");
            returnBook(Integer.parseInt(sc.nextLine().trim())); // read ISBN and pass directly to returnBook()
        } catch (NumberFormatException e) {
            System.out.println("Error: ISBN must be a valid integer.");
        }
    }
}
