
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

// interface for options
interface LibraryADT {

    void addBook(int isbn, String title, String author, int copies);

    void addCopiesToBook(int isbn, int copies);

    void searchBookByIsbn(int isbn);

    void searchBookByTitle(String title);

    void searchBookByAuthor(String author);

    void borrowBook(int isbn);

    void returnBook(int isbn);

    void viewLatestHistory();

    void viewBorrowedBooks();

    void deleteBook();

    void printWholeCatalogue();
}

// ==========================================
// Pure Data Models
// ==========================================
class Book {

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

class HistoryRecord {

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

// ==========================================
// 2. Catalogue Architect: Binary Search Tree
// ==========================================
class BookBST {

    private class Node {

        Book book;
        Node left, right;

        Node(Book book) {
            this.book = book;
        }
    }

    private Node root;

    public void insert(Book book) {
        root = insertRec(root, book);
    }

    private Node insertRec(Node root, Book book) {
        if (root == null) {
            return new Node(book);
        }
        if (book.getIsbn() < root.book.getIsbn()) {
            root.left = insertRec(root.left, book);
        } else if (book.getIsbn() > root.book.getIsbn()) {
            root.right = insertRec(root.right, book);
        }
        return root;
    }

    public Book search(int isbn) {
        Node result = searchRec(root, isbn);
        return result == null ? null : result.book;
    }

    private Node searchRec(Node root, int isbn) {
        if (root == null || root.book.getIsbn() == isbn) {
            return root;
        }
        if (root.book.getIsbn() > isbn) {
            return searchRec(root.left, isbn);
        }
        return searchRec(root.right, isbn);
    }

    public void delete(int isbn) {
        root = deleteRec(root, isbn);
    }

    private Node deleteRec(Node root, int isbn) {
        if (root == null) {
            return root;
        }

        if (isbn < root.book.getIsbn()) {
            root.left = deleteRec(root.left, isbn);
        } else if (isbn > root.book.getIsbn()) {
            root.right = deleteRec(root.right, isbn);
        } else {
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            root.book = minValue(root.right);
            root.right = deleteRec(root.right, root.book.getIsbn());
        }
        return root;
    }

    private Book minValue(Node root) {
        Book minv = root.book;
        while (root.left != null) {
            minv = root.left.book;
            root = root.left;
        }
        return minv;
    }

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        getAllBooksRec(root, list);
        return list;
    }

    private void getAllBooksRec(Node root, List<Book> list) {
        if (root != null) {
            getAllBooksRec(root.left, list);
            list.add(root.book);
            getAllBooksRec(root.right, list);
        }
    }

    public void printInOrder() {
        if (root == null) {
            System.out.println("The catalogue is currently empty.");
            return;
        }
        System.out.println("\n--- Complete Library Catalogue ---");
        printInOrderRec(root);
        System.out.println("----------------------------------");
    }

    private void printInOrderRec(Node root) {
        if (root != null) {
            printInOrderRec(root.left);
            System.out.println(root.book.toString());
            printInOrderRec(root.right);
        }
    }
}

// ==========================================
// 3. History: LIFO Stack with Action Tracking
// ==========================================
class HistoryStack {

    private Stack<HistoryRecord> stack = new Stack<>();

    public void push(Book b, String action) {
        stack.push(new HistoryRecord(b, action));
    }

    public List<HistoryRecord> getAllRecords() {
        return new ArrayList<>(stack);
    }

    public void show() {
        if (stack.isEmpty()) {
            System.out.println("History is empty. No recent activity.");
        } else {
            System.out.println("\n--- Library Transaction History (Most Recent First) ---");
            for (int i = stack.size() - 1; i >= 0; i--) {
                HistoryRecord record = stack.get(i);
                Book b = record.getBook();
                System.out.println("[" + record.getAction().toUpperCase() + "] - "
                        + "[ISBN: " + b.getIsbn() + "] " + b.getTitle());
            }
            System.out.println("-------------------------------------------------------");
        }
    }
}

// ==========================================
// 4. Main System Interface
// ==========================================
class SmartLibrary implements LibraryADT {

    private BookBST catalogue = new BookBST();
    private HistoryStack history = new HistoryStack();
    private Scanner sc = new Scanner(System.in);

    private HashMap<String, Book> titleIndex = new HashMap<>();
    private HashMap<String, List<Book>> authorIndex = new HashMap<>();

    private String userRole = "";

    // Tracks ISBNs borrowed in this session to validate returns
    private List<Integer> sessionBorrowedIsbns = new ArrayList<>();

    private final String CATALOGUE_FILE = "catalogue.csv";
    private final String HISTORY_FILE = "history.csv";

    public SmartLibrary() {
        loadData();
    }

    private void loadData() {
        try {
            File catFile = new File(CATALOGUE_FILE);
            if (catFile.exists()) {
                Scanner reader = new Scanner(catFile);
                while (reader.hasNextLine()) {
                    String[] data = reader.nextLine().split(",");
                    if (data.length == 5) {
                        Book b = new Book(Integer.parseInt(data[0]), data[1], data[2],
                                Integer.parseInt(data[3]), Integer.parseInt(data[4]));
                        catalogue.insert(b);
                        titleIndex.put(b.getTitle().toLowerCase(), b);
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
                    String[] data = reader.nextLine().split(",");
                    if (data.length == 2) {
                        Book b = catalogue.search(Integer.parseInt(data[0]));
                        if (b != null) {
                            history.push(b, data[1]);
                        }
                    }
                }
                reader.close();
            }
            System.out.println("System Initialized: Data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("System Initialized: No previous save data found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Warning: Could not perfectly load all save data.");
        }
    }

    private void saveData() {
        try {
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
        titleIndex.put(title.toLowerCase(), newBook);
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
            System.out.println("Options:");
            System.out.println(" - Enter a [number] to delete that many available copies.");
            System.out.println(" - Type 'all' to delete ALL available copies.");
            System.out.println(" - Type 'purge' to completely remove the book from the system.");
            System.out.println(" - Type 'exit' to cancel and return to the menu."); // <-- NEW OPTION
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim().toLowerCase();

            // Check for exit first
            if (choice.equals("exit") || choice.equals("cancel")) {
                System.out.println("Deletion cancelled. Returning to menu.");
                return;
            } else if (choice.equals("purge")) {
                if (b.getAvailableCopies() < b.getTotalCopies()) {
                    System.out.println("Error: Cannot purge book. " + (b.getTotalCopies() - b.getAvailableCopies()) + " copies are currently borrowed.");
                } else {
                    catalogue.delete(isbn);
                    titleIndex.remove(b.getTitle().toLowerCase());
                    if (authorIndex.containsKey(b.getAuthor().toLowerCase())) {
                        authorIndex.get(b.getAuthor().toLowerCase()).remove(b);
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
                int amount = Integer.parseInt(choice);
                if (amount <= 0) {
                    System.out.println("Error: Amount must be greater than 0.");
                } else if (amount > b.getAvailableCopies()) {
                    System.out.println("Error: Cannot delete " + amount + " copies. Only " + b.getAvailableCopies() + " are available.");
                } else {
                    b.removeCopies(amount);
                    System.out.println("Success: Removed " + amount + " copies. " + b.getAvailableCopies() + " available copies remain.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Expected a number, 'all', 'purge', or 'exit'.");
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

    @Override
    public void searchBookByTitle(String title) {
        Book b = titleIndex.get(title.toLowerCase());
        if (b != null) {
            System.out.println("Found: " + b.toString());
        } else {
            System.out.println("Result: No book found with title '" + title + "'.");
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
        if (!sessionBorrowedIsbns.contains(isbn)) {
            System.out.println("Error: You have not borrowed a book with ISBN " + isbn + " in this session.");
            return;
        }
        Book b = catalogue.search(isbn);
        if (b != null) {
            if (b.getAvailableCopies() < b.getTotalCopies()) {
                b.returnCopy();
                sessionBorrowedIsbns.remove(Integer.valueOf(isbn));
                history.push(b, "Returned");
                System.out.println("Success: You returned '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies now available.");
            } else {
                System.out.println("Error: All known copies of '" + b.getTitle() + "' are already in the library.");
            }
        } else {
            System.out.println("Error: This library does not own a book with ISBN " + isbn + ".");
        }
    }

    @Override
    public void viewLatestHistory() {
        history.show();
    }

    @Override
    public void viewBorrowedBooks() {
        System.out.println("\n--- Currently Borrowed Books ---");
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
        System.out.println("--------------------------------");
    }

    @Override
    public void printWholeCatalogue() {
        catalogue.printInOrder();
    }

    public void runMenu() {
        System.out.println("\nWelcome to the Smart Library System");

        while (true) {
            System.out.println("\n--- Login ---");
            System.out.println("Are you logging in as a [1] Student, [2] Librarian, or [3] Shut Down?");

            while (true) {
                System.out.print("Choice: ");
                String roleChoice = sc.nextLine().trim();
                if (roleChoice.equals("1")) {
                    userRole = "Student";
                    break;
                } else if (roleChoice.equals("2")) {
                    userRole = "Librarian";
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
                System.out.print("Choice: ");
                String input = sc.nextLine();
                int choice;

                try {
                    choice = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    continue;
                }

                if (userRole.equals("Student") && (choice >= 1 && choice <= 3)) {
                    System.out.println("Permission Denied: Only Librarians can manage inventory.");
                    continue;
                }

                if (choice == 10) {
                    System.out.println("Logging out...");
                    loggedIn = false;
                    break;
                }

                if (choice == 11) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }

                handleChoice(choice);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n--- SmartLibrary Menu (" + userRole + ") ---");
        System.out.println(userRole.equals("Librarian") ? "1. Add New Book" : "1. Add New Book [LOCKED]");
        System.out.println(userRole.equals("Librarian") ? "2. Add Copies (Restock)" : "2. Add Copies (Restock) [LOCKED]");
        System.out.println(userRole.equals("Librarian") ? "3. Delete Book" : "3. Delete Book [LOCKED]");
        System.out.println("4. Search Book");
        System.out.println("5. Borrow Book");
        System.out.println("6. Return Book");
        System.out.println("7. View History Log");
        System.out.println("8. View Borrowed Books");
        System.out.println("9. Print Whole Catalogue");
        System.out.println("10. Logout (Change Role)");
        System.out.println("11. Exit System");
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1:
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
                    System.out.println("Error: ISBN and number of copies must be valid integers.");
                }
                break;

            case 2:
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
                    System.out.println("Error: ISBN and number of copies must be valid integers.");
                }
                break;

            case 3:
                deleteBook();
                break;

            case 4:
                System.out.println("Search by: [1] ISBN [2] Title [3] Author");
                System.out.print("Choice: ");
                String searchType = sc.nextLine().trim();

                if (searchType.equals("1")) {
                    try {
                        System.out.print("Enter ISBN: ");
                        searchBookByIsbn(Integer.parseInt(sc.nextLine().trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: ISBN must be a number.");
                    }
                } else if (searchType.equals("2")) {
                    System.out.print("Enter Title: ");
                    String titleQuery = sc.nextLine().trim();
                    if (titleQuery.isEmpty()) {
                        System.out.println("Error: Title cannot be empty.");
                    } else {
                        searchBookByTitle(titleQuery);
                    }
                } else if (searchType.equals("3")) {
                    System.out.print("Enter Author: ");
                    String authorQuery = sc.nextLine().trim();
                    if (authorQuery.isEmpty()) {
                        System.out.println("Error: Author name cannot be empty.");
                    } else {
                        searchBookByAuthor(authorQuery);
                    }
                } else {
                    System.out.println("Invalid search type.");
                }
                break;

            case 5:
                try {
                    System.out.print("Enter ISBN to borrow: ");
                    borrowBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case 6:
                try {
                    System.out.print("Enter ISBN to return: ");
                    returnBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case 7:
                viewLatestHistory();
                break;

            case 8:
                viewBorrowedBooks();
                break;

            case 9:
                printWholeCatalogue();
                break;

            default:
                System.out.println("Invalid option. Please choose a valid menu number.");
        }
    }
}

// ==========================================
// Application Entry Point
// ==========================================
public class Main {

    public static void main(String[] args) {
        new SmartLibrary().runMenu();
    }
}
