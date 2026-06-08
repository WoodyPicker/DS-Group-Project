/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : (classname)
 * AUTHOR      : (name)
 * DESCRIPTION : (describe what u used like in my class(historystack))
 */
package Classes;

import java.util.ArrayList;
import java.util.List;

public class BookBST {

    // Keep your private Node class hidden inside here!
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
