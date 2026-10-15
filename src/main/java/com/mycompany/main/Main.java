package com.mycompany.main;

// Imports for user input, random numbers, lists and JSON file storage
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class Main {

    // Scanner for user input
    static Scanner input = new Scanner(System.in);

    // Registration details
    static String username;
    static String password;
    static String cellphone;

    static String registeredUsername;
    static String registeredPassword;
    static String registeredCellphone;

    // Message information
    static int totalMessages;
    static int messagesEntered = 0;
    static int messagesSent = 0;

    // Store sent and pending messages
    static List<String> sentMessages = new ArrayList<>();
    static List<String> storedMessages = new ArrayList<>();

    // Check username
    public static boolean checkUserName(String username) {

        return username.length() == 5
                && username.contains("_");
    }

    // Check password complexity
    public static boolean checkPasswordComplexity(String password) {

        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    // Check South African cellphone number
    public static boolean checkCellphoneNumber(String number) {

        return number.matches("^\\+27[0-9]{9}$");
    }

    // Register the user
    public static void registerUser() {

        boolean validRegistration = false;

        while (!validRegistration) {

            System.out.println("\n===== QUICKCHAT REGISTRATION =====");

            System.out.print("Enter your username: ");
            username = input.nextLine();

            System.out.print("Enter your password: ");
            password = input.nextLine();

            System.out.print("Enter cellphone number (+27): ");
            cellphone = input.nextLine();

            boolean usernameCorrect = checkUserName(username);
            boolean passwordCorrect = checkPasswordComplexity(password);
            boolean phoneCorrect = checkCellphoneNumber(cellphone);

            if (usernameCorrect && passwordCorrect && phoneCorrect) {

                registeredUsername = username;
                registeredPassword = password;
                registeredCellphone = cellphone;

                System.out.println("User registered successfully!");

                validRegistration = true;

            } else {

                System.out.println("\nRegistration failed.");

                if (!usernameCorrect) {
                    System.out.println(
                            "Username must be exactly 5 characters "
                            + "and contain an underscore (_).");
                }

                if (!passwordCorrect) {
                    System.out.println(
                            "Password must have at least 8 characters, "
                            + "an uppercase letter, lowercase letter, "
                            + "number and special character.");
                }

                if (!phoneCorrect) {
                    System.out.println(
                            "Cellphone number must start with +27 "
                            + "followed by exactly 9 digits.");
                }

                System.out.println("Please enter your details again.");
            }
        }
    }

    // Log in using registered details
    public static boolean loginUser(String username, String password) {

        return username.equals(registeredUsername)
                && password.equals(registeredPassword);
    }

    // Display login status
    public static String returnLoginStatus(boolean loginSuccessful) {

        if (loginSuccessful) {
            return "Login successful. Welcome "
                    + registeredUsername + "!";
        } else {
            return "Username or password incorrect. Please try again.";
        }
    }

    // Ask the user to log in
    public static boolean performLogin() {

        int attempts = 0;

        while (attempts < 3) {

            System.out.println("\n===== QUICKCHAT LOGIN =====");

            System.out.print("Enter your username: ");
            String loginUsername = input.nextLine();

            System.out.print("Enter your password: ");
            String loginPassword = input.nextLine();

            boolean successful = loginUser(
                    loginUsername, loginPassword);

            System.out.println(returnLoginStatus(successful));

            if (successful) {
                return true;
            }

            attempts++;
        }

        System.out.println("Too many failed attempts. Exiting QuickChat.");
        return false;
    }

    // Generate a random 10-digit message ID
    public static String generateMessageID() {

        long number = ThreadLocalRandom.current().nextLong(
                1_000_000_000L, 10_000_000_000L);

        return String.valueOf(number);
    }

    // Generate the message hash
    public static String createMessageHash(
            String messageID, int messageNumber, String message) {

        String[] words = message.trim().split("\\s+");

        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        return (messageID.substring(0, 2) + ":"
                + messageNumber + ":"
                + firstWord + lastWord).toUpperCase();
    }

    // Save stored messages in a JSON file
    public static void saveStoredMessages() {

        StringBuilder json = new StringBuilder();

        json.append("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {

            String message = storedMessages.get(i);

            // Escape characters so the message is valid JSON text
            message = message.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            json.append("  \"").append(message).append("\"");

            if (i < storedMessages.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]");

        try {
            Files.writeString(
                    Path.of("stored_messages.json"),
                    json.toString());

            System.out.println("Stored messages saved to JSON file.");

        } catch (IOException e) {
            System.out.println("Could not save messages to the JSON file.");
        }
    }

    // Display the message details
    public static void displayMessage(
            String messageID, String messageHash,
            String recipient, String message) {

        System.out.println("\n===== MESSAGE DETAILS =====");
        System.out.println("Message ID: " + messageID);
        System.out.println("Message Hash: " + messageHash);
        System.out.println("Recipient: " + recipient);
        System.out.println("Message: " + message);
    }

    // Create and process a message
    public static void sendMessages() {

        if (messagesEntered >= totalMessages) {

            System.out.println(
                    "You have already entered your maximum number of messages.");
            return;
        }

        System.out.println("\n===== SEND MESSAGE =====");

        // Enter recipient cellphone number
        String recipient;

        while (true) {

            System.out.print("Enter recipient number (+27): ");
            recipient = input.nextLine();

            if (checkCellphoneNumber(recipient)) {
                break;
            }

            System.out.println(
                    "Invalid number. Use +27 followed by 9 digits.");
        }

        // Enter message
        String message;

        while (true) {

            System.out.print("Enter your message: ");
            message = input.nextLine();

            // Message must contain at least one non-space character
            if (message.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
            } else if (message.length() >= 250) {
                System.out.println(
                        "Please enter a message of less than 250 characters.");
            } else {
                break;
            }
        }

        // Generate message information
        String messageID = generateMessageID();

        int messageNumber = messagesEntered + 1;

        String messageHash = createMessageHash(
                messageID, messageNumber, message);

        // Display message information before choosing an action
        displayMessage(messageID, messageHash, recipient, message);

        // Choose what to do with the message
        System.out.println("\nChoose an option:");
        System.out.println("1. Send Message");
        System.out.println("2. Disregard Message");
        System.out.println("3. Store Message to send later");

        int choice = readInteger("Enter your choice: ");

        while (choice < 1 || choice > 3) {
            System.out.println("Invalid choice. Choose 1, 2 or 3.");
            choice = readInteger("Enter your choice: ");
        }

        // Count each message that has been entered
        messagesEntered++;

        if (choice == 1) {

            sentMessages.add(messageID + "|" + messageHash
                    + "|" + recipient + "|" + message);

            messagesSent++;

            System.out.println("Message successfully sent");

        } else if (choice == 2) {

            System.out.println("Press 0 to delete the message.");

            String deleteChoice = input.nextLine();

            if (deleteChoice.equals("0")) {
                System.out.println("Message disregarded.");
            } else {
                System.out.println("Message not sent or stored.");
            }

        } else {

            storedMessages.add(messageID + "|" + messageHash
                    + "|" + recipient + "|" + message);

            saveStoredMessages();

            System.out.println("Message successfully stored");
        }

        // Show how many messages have been entered
        System.out.println("Messages entered: "
                + messagesEntered + " of " + totalMessages);
    }

    // Display the menu
    public static void displayMenu() {

        System.out.println("\n===== QUICKCHAT =====");
        System.out.println("1. Send Messages");
        System.out.println("2. Show Recently Sent Messages");
        System.out.println("3. Quit");
    }

    // Read an integer safely
    public static int readInteger(String prompt) {

        while (true) {

            System.out.print(prompt);

            try {
                int number = Integer.parseInt(input.nextLine());
                return number;

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    // Main method
    public static void main(String[] args) {

        System.out.println("Welcome to QuickChat.");

        // Register first
        registerUser();

        // Login before allowing access to messaging
        boolean loginSuccessful = performLogin();

        if (!loginSuccessful) {
            input.close();
            return;
        }

        // Ask how many messages the user wants to enter
        while (true) {

            totalMessages = readInteger(
                    "How many messages would you like to enter? ");

            if (totalMessages > 0) {
                break;
            }

            System.out.println("Enter a number greater than zero.");
        }

        int option;

        // Continue until the user chooses Quit
        do {

            displayMenu();

            option = readInteger("Choose an option: ");

            if (option == 1) {

                if (messagesEntered < totalMessages) {
                    sendMessages();
                } else {
                    System.out.println(
                            "You have reached your message limit.");
                }

            } else if (option == 2) {

                // This feature is still in development
                System.out.println("Coming Soon.");

            } else if (option == 3) {

                System.out.println("\n===== QUICKCHAT SUMMARY =====");
                System.out.println("Messages entered: " + messagesEntered);
                System.out.println("Messages successfully sent: " + messagesSent);
                System.out.println("Messages stored: " + storedMessages.size());

                System.out.println("Thank you for using QuickChat.");

            } else {

                System.out.println(
                        "Invalid option. Please choose 1, 2 or 3.");
            }

        } while (option != 3);

        input.close();
    }
}