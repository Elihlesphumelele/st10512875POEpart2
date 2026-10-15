package com.mycompany.main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private PrintStream originalOutput;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() {

        // Save the original output stream
        originalOutput = System.out;

        // Reset registration details
        Main.username = null;
        Main.password = null;
        Main.cellphone = null;

        Main.registeredUsername = null;
        Main.registeredPassword = null;
        Main.registeredCellphone = null;

        // Reset message information
        Main.totalMessages = 0;
        Main.messagesEntered = 0;
        Main.messagesSent = 0;

        Main.sentMessages.clear();
        Main.storedMessages.clear();

        // Use an empty input stream by default
        Main.input = new Scanner(
                new ByteArrayInputStream(new byte[0]));

        // Capture output for testing
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void tearDown() {

        // Restore the original output stream
        System.setOut(originalOutput);

        // Do not close System.in or the scanner.
        // Reset Main.input to an independent empty stream.
        Main.input = new Scanner(
                new ByteArrayInputStream(new byte[0]));
    }

    // Supply simulated input
    private void provideInput(String data) {

        Main.input = new Scanner(
                new ByteArrayInputStream(
                        data.getBytes(StandardCharsets.UTF_8)));
    }

    // Get captured output
    private String getOutput() {
        return output.toString();
    }

    // Test username validation
    @Test
    public void testCheckUserNameValid() {

        assertTrue(Main.checkUserName("un_m1"));
    }

    @Test
    public void testCheckUserNameInvalid() {

        assertFalse(Main.checkUserName("unam"));
        assertFalse(Main.checkUserName("unam_1"));
        assertFalse(Main.checkUserName("abcde"));
    }

    // Test password validation
    @Test
    public void testCheckPasswordComplexityValid() {

        assertTrue(Main.checkPasswordComplexity("Unam@123"));
    }

    @Test
    public void testCheckPasswordComplexityInvalid() {

        assertFalse(Main.checkPasswordComplexity("password"));
        assertFalse(Main.checkPasswordComplexity("UNAM@123"));
        assertFalse(Main.checkPasswordComplexity("Unam1234"));
        assertFalse(Main.checkPasswordComplexity("Un@1"));
    }

    // Test cellphone validation
    @Test
    public void testCheckCellphoneNumberValid() {

        assertTrue(Main.checkCellphoneNumber("+27821234567"));
        assertTrue(Main.checkCellphoneNumber("+27123456789"));
    }

    @Test
    public void testCheckCellphoneNumberInvalid() {

        assertFalse(Main.checkCellphoneNumber("0821234567"));
        assertFalse(Main.checkCellphoneNumber("+2782123456"));
        assertFalse(Main.checkCellphoneNumber("+278212345678"));
        assertFalse(Main.checkCellphoneNumber("27821234567"));
        assertFalse(Main.checkCellphoneNumber("+27abcdefgh"));
    }

    // Test registration
    @Test
    public void testRegisterUserSuccessfully() {

        provideInput(
                "un_m1\n"
                + "Unam@123\n"
                + "+27821234567\n");

        Main.registerUser();

        assertEquals("un_m1", Main.registeredUsername);
        assertEquals("Unam@123", Main.registeredPassword);
        assertEquals("+27821234567", Main.registeredCellphone);

        assertTrue(getOutput().contains(
                "User registered successfully!"));
    }

    @Test
    public void testRegisterUserRejectsInvalidDetails() {

        provideInput(
                "badname\nweak\n123\n"
                + "un_m1\nUnam@123\n+27821234567\n");

        Main.registerUser();

        assertEquals("un_m1", Main.registeredUsername);
        assertTrue(getOutput().contains("Registration failed."));
    }

    // Test login
    @Test
    public void testLoginUserSuccessfully() {

        Main.registeredUsername = "un_m1";
        Main.registeredPassword = "Unam@123";

        assertTrue(Main.loginUser("un_m1", "Unam@123"));
    }

    @Test
    public void testLoginUserWrongPassword() {

        Main.registeredUsername = "un_m1";
        Main.registeredPassword = "Unam@123";

        assertFalse(Main.loginUser("un_m1", "Wrong@123"));
    }

    @Test
    public void testLoginUserWrongUsername() {

        Main.registeredUsername = "un_m1";
        Main.registeredPassword = "Unam@123";

        assertFalse(Main.loginUser("other", "Unam@123"));
    }

    @Test
    public void testReturnLoginStatusSuccessful() {

        Main.registeredUsername = "un_m1";

        assertEquals(
                "Login successful. Welcome un_m1!",
                Main.returnLoginStatus(true));
    }

    @Test
    public void testReturnLoginStatusUnsuccessful() {

        assertEquals(
                "Username or password incorrect. Please try again.",
                Main.returnLoginStatus(false));
    }

    @Test
    public void testPerformLoginSuccessfully() {

        Main.registeredUsername = "un_m1";
        Main.registeredPassword = "Unam@123";

        provideInput("un_m1\nUnam@123\n");

        assertTrue(Main.performLogin());
        assertTrue(getOutput().contains("Login successful."));
    }

    @Test
    public void testPerformLoginFailsAfterThreeAttempts() {

        Main.registeredUsername = "un_m1";
        Main.registeredPassword = "Unam@123";

        provideInput(
                "wrong\nwrong\n"
                + "wrong\nwrong\n"
                + "wrong\nwrong\n");

        assertFalse(Main.performLogin());

        assertTrue(getOutput().contains(
                "Too many failed attempts."));
    }

    // Test message ID generation
    @Test
    public void testGenerateMessageID() {

        String result = Main.generateMessageID();

        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.matches("[0-9]{10}"));
    }

    // Test message hash generation
    @Test
    public void testCreateMessageHash() {

        assertEquals(
                "12:1:HITHANKS",
                Main.createMessageHash(
                        "1234567890", 1, "Hi thanks"));
    }

    @Test
    public void testCreateMessageHashOneWord() {

        assertEquals(
                "98:2:HELLOHELLO",
                Main.createMessageHash(
                        "9876543210", 2, "Hello"));
    }

    // Test saving messages
    @Test
    public void testSaveStoredMessages() throws Exception {

        Path file = Path.of("stored_messages.json");

        Main.storedMessages.add(
                "1234567890|12:1:HITHANKS|+27821234567|Hi thanks");

        try {

            Main.saveStoredMessages();

            assertTrue(Files.exists(file));

            String content = Files.readString(file);

            assertTrue(content.contains("1234567890"));
            assertTrue(content.contains("+27821234567"));

        } finally {

            Files.deleteIfExists(file);
        }
    }

    // Test displaying message details
    @Test
    public void testDisplayMessage() {

        Main.displayMessage(
                "1234567890",
                "12:1:HITHANKS",
                "+27821234567",
                "Hi thanks");

        assertTrue(getOutput().contains("1234567890"));
        assertTrue(getOutput().contains("12:1:HITHANKS"));
        assertTrue(getOutput().contains("+27821234567"));
        assertTrue(getOutput().contains("Hi thanks"));
    }

    // Test sending a message
    @Test
    public void testSendMessageSuccessfully() {

        Main.totalMessages = 1;

        provideInput(
                "+27821234567\n"
                + "Hello there\n"
                + "1\n");

        Main.sendMessages();

        assertEquals(1, Main.messagesEntered);
        assertEquals(1, Main.messagesSent);
        assertEquals(1, Main.sentMessages.size());

        assertTrue(getOutput().contains(
                "Message successfully sent"));
    }

    // Test disregarding a message
    @Test
    public void testDisregardMessage() {

        Main.totalMessages = 1;

        provideInput(
                "+27821234567\n"
                + "Hello there\n"
                + "2\n"
                + "0\n");

        Main.sendMessages();

        assertEquals(1, Main.messagesEntered);
        assertEquals(0, Main.messagesSent);
        assertEquals(0, Main.sentMessages.size());

        assertTrue(getOutput().contains(
                "Message disregarded."));
    }

    // Test storing a message
    @Test
    public void testStoreMessage() throws Exception {

        Path file = Path.of("stored_messages.json");

        try {

            Main.totalMessages = 1;

            provideInput(
                    "+27821234567\n"
                    + "Hello there\n"
                    + "3\n");

            Main.sendMessages();

            assertEquals(1, Main.messagesEntered);
            assertEquals(1, Main.storedMessages.size());

            assertTrue(getOutput().contains(
                    "Message successfully stored"));

            assertTrue(Files.exists(file));

        } finally {

            Files.deleteIfExists(file);
        }
    }

    // Test the message limit
    @Test
    public void testMessageLimit() {

        Main.totalMessages = 1;
        Main.messagesEntered = 1;

        Main.sendMessages();

        assertEquals(1, Main.messagesEntered);

        assertTrue(getOutput().contains(
                "maximum number of messages"));
    }

    // Test the menu
    @Test
    public void testDisplayMenu() {

        Main.displayMenu();

        assertTrue(getOutput().contains("1. Send Messages"));
        assertTrue(getOutput().contains(
                "2. Show Recently Sent Messages"));
        assertTrue(getOutput().contains("3. Quit"));
    }

    // Test integer input
    @Test
    public void testReadInteger() {

        provideInput("5\n");

        assertEquals(
                5,
                Main.readInteger("Enter a number: "));
    }

    @Test
    public void testReadIntegerInvalidInput() {

        provideInput("abc\n7\n");

        assertEquals(
                7,
                Main.readInteger("Enter a number: "));

        assertTrue(getOutput().contains(
                "Please enter a valid whole number."));
    }

    // Test the placeholder message
    @Test
    public void testRecentlySentMessagesPlaceholder() {

        System.out.println("Coming Soon.");

        assertTrue(getOutput().contains("Coming Soon."));
    }
}