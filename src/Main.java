/**
 * COURSE      : WIA1002 Data Structures
 * GROUP       : Group 1 OCC 9
 * PROJECT     : Smart Library Management System
 * COMPONENT   : Main.java
 * AUTHOR      : Irwina Batrisha Binti Mohd Shahar (25061717)
 * DESCRIPTION : Application entry point. Instantiates the concrete SmartLibrary behind a LibraryADT interface reference - programming to the interface rather
 * than the implementation - and launches the interactive console menu loop.
 */
package src;
// Entry point of the program; the JVM starts execution here.

public class Main {
    // Declare the variable using the ADT interface type, not the concrete class.
    // The client (Main) depends only on the LibraryADT contract, which enforces
    // information hiding and keeps it decoupled from SmartLibrary's internals.

    public static void main(String[] args) {
        LibraryADT library = new SmartLibrary();
        // Hand control to the system: starts the interactive login and menu loop.
        library.runMenu();
    }
}
