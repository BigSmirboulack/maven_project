package fr.univ_lyon1.info.m1.elizagpt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Logic to process a message (and probably reply to it).
 */
public class MessageProcessor {
    private List<HBox> messages;
    private final Random random = new Random();

    /**
     * Normlize the text: remove extra spaces, add a final dot if missing.
     * 
     * @param text
     * @return normalized text.
     */
    public String normalize(final String text) {
        return text.replaceAll("\\s+", " ")
                .replaceAll("^\\s+", "")
                .replaceAll("\\s+$", "")
                .replaceAll("[^\\.!?:]$", "$0.");
    }

    /**
     * Information about conjugation of a verb.
     */
    public static class Verb {
        private final String firstSingular;
        private final String secondPlural;

        public String getFirstSingular() {
            return firstSingular;
        }

        public String getSecondPlural() {
            return secondPlural;
        }

        Verb(final String firstSingular, final String secondPlural) {
            this.firstSingular = firstSingular;
            this.secondPlural = secondPlural;
        }
    }

    /**
     * List of 3rd group verbs and their correspondance from 1st person signular
     * (Je) to 2nd person plural (Vous).
     */
    protected static final List<Verb> VERBS = Arrays.asList(
            new Verb("suis", "êtes"),
            new Verb("vais", "allez"),
            new Verb("dis", "dites"),
            new Verb("ai", "avez"),
            new Verb("peux", "pouvez"),
            new Verb("dois", "devez"),
            new Verb("fais", "faites"),
            new Verb("sais", "savez"),
            new Verb("dois", "devez"));

    /**
     * Turn a 1st-person sentence (Je ...) into a plural 2nd person (Vous ...).
     * The result is not capitalized to allow forming a new sentence.
     *
     * TODO: does not deal with all 3rd group verbs.
     *
     * @param text
     * @return The 2nd-person sentence.
     */
    public String firstToSecondPerson(final String text) {
        String processedText = text
                .replaceAll("[Jj]e ([a-z]*)e ", "vous $1ez ");
        for (Verb v : VERBS) {
            processedText = processedText.replaceAll(
                    "[Jj]e " + v.getFirstSingular(),
                    "vous " + v.getSecondPlural());
        }
        processedText = processedText
                .replaceAll("[Jj]e ([a-z]*)s ", "vous $1ssez ")
                .replace("mon ", "votre ")
                .replace("ma ", "votre ")
                .replace("mes ", "vos ")
                .replace("moi", "vous");
        return processedText;
    }

    /** Pick an element randomly in the array. */
    public <T> T pickRandom(final T[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * find a random element in the array that matches the pattern.
     */
    public List<HBox> filterMessagesByPattern(final String patternText) {
        Pattern pattern;
        List<HBox> filteredMessages = new ArrayList<>();

        try {
            pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return messages; // Return all messages if pattern is invalid
        }

        for (HBox hBox : messages) {
            for (Node label : hBox.getChildren()) {
                String labelText = ((Label) label).getText();
                if (pattern.matcher(labelText).find()) {
                    filteredMessages.add(hBox);
                    break;
                }
            }
        }

        return filteredMessages;
    }

}
