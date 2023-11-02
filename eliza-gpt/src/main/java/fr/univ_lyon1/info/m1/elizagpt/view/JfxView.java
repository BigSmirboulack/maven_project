package fr.univ_lyon1.info.m1.elizagpt.view;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.util.regex.PatternSyntaxException;

import fr.univ_lyon1.info.m1.elizagpt.model.MessageProcessor;

/**
 * Main class of the View (GUI) of the application.
 */
public class JfxView {
    private final VBox dialog;
    private TextField text = null;
    private TextField searchText = null;
    private Label searchTextLabel = null;
    private MessageProcessor processor = new MessageProcessor();
    private final Random random = new Random();

    /**
     * Main class of the View (GUI) of the application.
     */
    public JfxView(final Stage stage, final int width, final int height) {
        stage.setTitle("Eliza GPT");
        final VBox root = new VBox(10);
        final Pane search = createSearchWidget();
        root.getChildren().add(search);

        ScrollPane dialogScroll = new ScrollPane();
        dialog = new VBox(10);
        dialogScroll.setContent(dialog);
        // scroll to bottom by default:
        dialogScroll.vvalueProperty().bind(dialog.heightProperty());
        root.getChildren().add(dialogScroll);
        dialogScroll.setFitToWidth(true);

        final Pane input = createInputWidget();
        root.getChildren().add(input);
        replyToUser("Bonjour");

        // Everything's ready: add it to the scene and display it
        final Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        text.requestFocus();
        stage.show();
    }

    static final String BASE_STYLE = "-fx-padding: 8px; "
            + "-fx-margin: 5px; "
            + "-fx-background-radius: 5px;";
    static final String ELIZA_STYLE = "-fx-background-color: #A0E0A0; " + BASE_STYLE;
    static final String USER_STYLE = "-fx-background-color: #A0A0E0; " + BASE_STYLE;

    private void replyToUser(final String text) {
        HBox hBox = new HBox();
        final Label label = new Label(text);
        hBox.getChildren().add(label);
        label.setStyle(USER_STYLE);
        hBox.setAlignment(Pos.BASELINE_LEFT);
        dialog.getChildren().add(hBox);
        // Ajoute un gestionnaire d'événements pour gérer le clic sur la HBox
        hBox.setOnMouseClicked(event -> {
            // Supprime la HBox (le message) lors du clic
            dialog.getChildren().remove(hBox);
        });
    }

    private void sendMessage(final String text) {
        HBox hBox = new HBox();
        final Label label = new Label(text);
        hBox.getChildren().add(label);
        label.setStyle(ELIZA_STYLE);
        hBox.setAlignment(Pos.BASELINE_RIGHT);
        dialog.getChildren().add(hBox);
        hBox.setOnMouseClicked(e -> {
            dialog.getChildren().remove(hBox);
        });

        String normalizedText = processor.normalize(text);

        Pattern pattern;
        Matcher matcher;

        // First, try to answer specifically to what the user said
        pattern = Pattern.compile(".*Je m'appelle (.*)\\.", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            replyToUser("Bonjour " + matcher.group(1) + ".");
            return;
        }
        pattern = Pattern.compile("Quel est mon nom \\?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            if (getName() != null) {
                replyToUser("Votre nom est " + getName() + ".");
            } else {
                replyToUser("Je ne connais pas votre nom.");
            }
            return;
        }
        pattern = Pattern.compile("Qui est le plus (.*) \\?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            replyToUser("Le plus " + matcher.group(1)
                    + " est bien sûr votre enseignant de MIF01 !");
            return;
        }
        pattern = Pattern.compile("(Je .*)\\.", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            final String startQuestion = processor.pickRandom(new String[] {
                    "Pourquoi dites-vous que ",
                    "Pourquoi pensez-vous que ",
                    "Êtes-vous sûr que ",
            });
            replyToUser(startQuestion + processor.firstToSecondPerson(matcher.group(1)) + " ?");
            return;
        }
        pattern = Pattern.compile(".*\\?$", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            final String startQuestion = processor.pickRandom(new String[] {
                    "Je vous renvoie la question.",
                    "Ici, c'est moi qui pose les questions."
            });
            replyToUser(startQuestion);
            return;
        }
        // Nothing clever to say, answer randomly
        if (random.nextBoolean()) {
            replyToUser("Il faut beau aujourd'hui, vous ne trouvez pas ?");
            return;
        }
        if (random.nextBoolean()) {
            replyToUser("Je ne comprends pas.");
            return;
        }
        if (random.nextBoolean()) {
            replyToUser("Hmmm, hmm ...");
            return;
        }
        // Default answer
        if (getName() != null) {
            replyToUser("Qu'est-ce qui vous fait dire cela, " + getName() + " ?");
        } else {
            replyToUser("Qu'est-ce qui vous fait dire cela ?");
        }
    }

    public TextField getSearchText() {
        return searchText;
    }

    /**
     * Update the messages displayed in the dialog.
     * @param messages
     */
    public void updateMessages(final List<HBox> messages) {
        dialog.getChildren().clear();
        dialog.getChildren().addAll(messages);
    }

    /**
     * Update the text of the search label.
     * @param text
     */
    public void updateSearchLabel(final String text) {
        searchTextLabel.setText(text);
    }
    /*
     * return the text of the search text field.
     */
    public String getInputText() {
        return text.getText();
    }

    /**
     * clear the search text field.
     */
    public void clearSearchText() {
        searchText.setText("");
    }
    /**
     * clear the input text field.
     */
    public void clearInputText() {
        text.setText("");
    }

    /**
     * Extract the name of the user from the dialog.
     * TODO: this totally breaks the MVC pattern, never, ever, EVER do that.
     *
     * @return
     */
    private String getName() {
        for (Node hBox : dialog.getChildren()) {
            for (Node label : ((HBox) hBox).getChildren()) {
                if (((Label) label).getStyle().equals("-fx-background-color: #A0E0A0;")) {
                    String text = ((Label) label).getText();
                    Pattern pattern = Pattern.compile("Je m'appelle (.*)\\.",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        }
        return null;
    }

    private Pane createSearchWidget() {
        final HBox firstLine = new HBox();
        final HBox secondLine = new HBox();
        firstLine.setAlignment(Pos.BASELINE_LEFT);
        secondLine.setAlignment(Pos.BASELINE_LEFT);
        searchText = new TextField();
        searchText.setOnAction(e -> {
            searchText(searchText);
        });
        firstLine.getChildren().add(searchText);
        final Button send = new Button("Search");
        send.setOnAction(e -> {
            searchText(searchText);
        });
        searchTextLabel = new Label();
        final Button undo = new Button("Undo search");
        undo.setOnAction(e -> {
            throw new UnsupportedOperationException("TODO: implement undo for search");
        });
        secondLine.getChildren().addAll(send, searchTextLabel, undo);
        final VBox input = new VBox();
        input.getChildren().addAll(firstLine, secondLine);
        return input;
    }

    private void searchText(final TextField text) {
        String currentSearchText = text.getText();
        if (currentSearchText == null || currentSearchText.isEmpty()) {
            searchTextLabel.setText("No active search");
        } else {
            searchTextLabel.setText("Searching for: " + currentSearchText);
        }

        List<HBox> toDelete = new ArrayList<>();
        Pattern pattern = null;

        try {
            pattern = Pattern.compile(currentSearchText, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            // Handle invalid regular expression
            e.printStackTrace();
            return;
        }

        for (Node hBox : dialog.getChildren()) {
            for (Node label : ((HBox) hBox).getChildren()) {
                String labelText = ((Label) label).getText();
                boolean matches = (pattern.matcher(labelText).find());
                if (!matches) {
                    toDelete.add((HBox) hBox);
                    break; // No need to check other labels within this HBox
                }
            }
        }

        dialog.getChildren().removeAll(toDelete);
        text.setText("");
    }

    private Pane createInputWidget() {
        final Pane input = new HBox();
        text = new TextField();
        text.setOnAction(e -> {
            sendMessage(text.getText());
            text.setText("");
        });
        final Button send = new Button("Send");
        send.setOnAction(e -> {
            sendMessage(text.getText());
            text.setText("");
        });
        input.getChildren().addAll(text, send);
        return input;
    }
}
