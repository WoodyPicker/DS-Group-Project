/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : HistoryStack.java
 * AUTHOR      : Irwina Batrisha binti Mohd Shahar (25061717)
 * DESCRIPTION : Implements the linear borrowing history archive using a Last-In,
 * First-Out (LIFO) tracking arrangement. Leverages private collection
 * encapsulation boundaries to satisfy structural information hiding
 * rules while utilizing a non-destructive temporary copy algorithm
 * for chronological rendering loops without data erasure.
 */
package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HistoryStack {

    //Prevents code outside the module from directly accessing the structure's internal data to ensure information hiding.
    private Stack<HistoryRecord> stack = new Stack<>();

    /**
     * Pushes a new transaction frame onto the top of the collection. Time
     * Complexity: O(1) constant runtime insertion.
     */
    public void push(Book b, String action) {
        stack.push(new HistoryRecord(b, action));
    }

    /**
     * Exposes records for external list utilities while preserving internal
     * structure encapsulation.
     */
    public List<HistoryRecord> getAllRecords() {
        return new ArrayList<>(stack);
    }

    /**
     * Displays log entries in chronological order by working on a temporary
     * copy of the stack, ensuring the original records remain unchanged.
     */
    public void show() {
        // Checks whether the structure is empty before proceeding with navigation.
        if (stack.isEmpty()) {
            System.out.println("No recent borrowing or return activity.");
        } else {
            System.out.println("\n<<-- Recent Library Transaction Records -->>");

            // Create a temporary copy to preserve the original log data.
            Stack<HistoryRecord> tempStack = new Stack<>();
            tempStack.addAll(stack);

            // Remove entries from the temporary stack one by one using pop().
            // This displays transactions from newest to oldest, following LIFO order.
            while (!tempStack.isEmpty()) {
                HistoryRecord record = tempStack.pop(); // Pure Stack operation!
                Book b = record.getBook();

                // Display the item's details in a formatted layout.
                System.out.println("[" + record.getAction().toUpperCase() + "] - "
                        + "[ISBN: " + b.getIsbn() + "] " + b.getTitle());
            }
            System.out.println("<<----------------------------------------------------->>");
        }
    }
}
