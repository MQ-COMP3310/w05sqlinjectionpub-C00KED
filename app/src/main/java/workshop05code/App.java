package workshop05code;

import java.io.BufferedReader;
import java.io.FileInputStream; // Add this import
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.info("Wordle created and connected.");
        } else {
            logger.severe("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            logger.info("Wordle structures in place.");
        } else {
            logger.severe("Not able to launch. Sorry!");
            return;
        }

        // Add words to valid 4-letter words from the data.txt file
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added: " + line);
                    i++;
                } else {
                    logger.severe("Invalid word in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading data.txt.", e);
            return;
        }

        // Get user input for guesses
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4-letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (guess.length() != 4) {
                    logger.warning("Invalid guess: " + guess);
                    System.out.println("Invalid guess. Please enter a 4-letter word.");
                } else if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.");
                } else {
                    System.out.println("Sorry. This word is NOT in the list.");
                    logger.info("Invalid guess (not in list): " + guess);
                }

                System.out.print("Enter a 4-letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error during user input.", e);
        }
    }
}