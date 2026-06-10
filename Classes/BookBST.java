/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : BookBST.java
 * AUTHOR      : Ali Naif Ahmed Jiawi (25071095) & Muhammad Adam bin Md.Yusuf (24002965)
 * DESCRIPTION : Implements the library catalogue using a Binary Search Tree (BST)
 * organised by ISBN. Supports insertion, search, deletion, and sorted
 * traversal of Book records through recursive algorithms.
 */
package Classes;

import java.util.ArrayList;
import java.util.List;

public class BookBST {

    // Inner Node class is kept private to hide the tree's internal structure.
    private class Node {

        Book book;
        Node left, right;

        Node(Book book) {
            this.book = book;
        }
    }
    
     // Entry point into the tree.
    private Node root;

    /**
     * Inserts a new Book into the tree based on its ISBN.
     * Time Complexity: O(log n) average case.
     */
    
    public void insert(Book book) {
        root = insertRec(root, book);
    }

    // Recursively finds the correct empty slot and places the new node there.
    private Node insertRec(Node root, Book book) {
        if (root == null) {
            return new Node(book);
        }
        if (book.getIsbn() < root.book.getIsbn()) {
            root.left = insertRec(root.left, book);
        } else if (book.getIsbn() > root.book.getIsbn()) {
            root.right = insertRec(root.right, book);
        }
        // Duplicate ISBNs are ignored.
        return root;
    }

    /**
     * Searches for a Book by ISBN. Returns the Book if found, null otherwise.
     * Time Complexity: O(log n) average case.
     */
    public Book search(int isbn) {
        Node result = searchRec(root, isbn);
        return result == null ? null : result.book;
    }
    
    // Recursively moves left or right until the target ISBN is found or the subtree is empty.
    private Node searchRec(Node root, int isbn) {
        if (root == null || root.book.getIsbn() == isbn) {
            return root;
        }
        if (root.book.getIsbn() > isbn) {
            return searchRec(root.left, isbn);
        }
        return searchRec(root.right, isbn);
    }

    /**
     * Deletes the Book with the matching ISBN from the tree.
     * Time Complexity: O(log n) average case.
     */
    public void delete(int isbn) {
        root = deleteRec(root, isbn);
    }
    
    // Recursively locates and removes the target node, handling all three deletion cases.
    private Node deleteRec(Node root, int isbn) {
        if (root == null) {
            return root;
        }

        if (isbn < root.book.getIsbn()) {
            root.left = deleteRec(root.left, isbn);
        } else if (isbn > root.book.getIsbn()) {
            root.right = deleteRec(root.right, isbn);
        } else {
            // Case 1 & 2: Node has one or no children.
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }
            
            // Case 3: Node has two children, replace with the in-order successor.
            root.book = minValue(root.right);
            root.right = deleteRec(root.right, root.book.getIsbn());
        }
        return root;
    }
    
    // Finds the smallest ISBN in a subtree, used as the in-order successor during deletion.
    private Book minValue(Node root) {
        Book minv = root.book;
        while (root.left != null) {
            minv = root.left.book;
            root = root.left;
        }
        return minv;
    }

    //Returns all Books as a sorted list without exposing the tree structure.
    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        getAllBooksRec(root, list);
        return list;
    }

    // Collects books in ascending ISBN order using in-order traversal.
    private void getAllBooksRec(Node root, List<Book> list) {
        if (root != null) {
            getAllBooksRec(root.left, list);
            list.add(root.book);
            getAllBooksRec(root.right, list);
        }
    }

    // Prints all books to the console in ascending ISBN order.
    public void printInOrder() {
        // Check if the catalogue is empty before traversing.
        if (root == null) {
            System.out.println("The catalogue is currently empty.");
            return;
        }
        System.out.println("\n--- Complete Library Catalogue ---");
        printInOrderRec(root);
        System.out.println("----------------------------------");
    }

    // Prints each book's details by visiting left subtree, current node, then right subtree.
    private void printInOrderRec(Node root) {
        if (root != null) {
            printInOrderRec(root.left);
            System.out.println(root.book.toString());
            printInOrderRec(root.right);
        }
    }
}
