package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.io.FileInputStream;

/**
 * Wordle application with enhanced logging.
 */
public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Failed to load logging configuration", e);
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");
        
        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.info("Wordle database created and connected.");
        } else {
            logger.severe("Database connection failed. Exiting application.");
            return;
        }
        
        if (wordleDatabaseConnection.createWordleTables()) {
            logger.info("Wordle structures initialized.");
        } else {
            logger.severe("Failed to create Wordle structures. Exiting application.");
            return;
        }

        // Load words from data.txt
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (isValidWord(line)) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added: " + line);
                    i++;
                } else {
                    logger.severe("Invalid word found in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load words from file.", e);
            return;
        }

        // Game loop
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4-letter word for a guess or 'q' to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! The word is in the list.");
                } else {
                    System.out.println("Sorry. This word is NOT in the list.");
                    logger.warning("Invalid guess: " + guess);
                }

                System.out.print("Enter a 4-letter word for a guess or 'q' to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Unexpected error during user input.", e);
        }
        
        logger.info("Game ended successfully.");
    }

    private static boolean isValidWord(String word) {
        return word.matches("^[a-zA-Z]{4}$"); // Ensures the word is exactly 4 letters long
    }
}
