package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SecondaryController {

    @FXML private Button btn00, btn01, btn02;
    @FXML private Button btn10, btn11, btn12;
    @FXML private Button btn20, btn21, btn22;
    @FXML private Label resultLabel;

    private SimpleClient client;
    private int currentTurn;
    private Button[][] board;

    @FXML
    public void initialize() {
        client = SimpleClient.getClient();
        EventBus.getDefault().register(this);

        // إعداد المصفوفة
        board = new Button[][] {
                { btn00, btn01, btn02 },
                { btn10, btn11, btn12 },
                { btn20, btn21, btn22 }
        };

        int playerNum = client.getPlayerNumber();
        currentTurn = client.getCurrentTurn();
        System.out.println("Player #" + playerNum + " initialized game screen.");
    }

    @FXML
    private void handleCellClick(ActionEvent event) {
        if (client.getPlayerNumber() != currentTurn) {
            System.out.println("Not your turn.");
            return;
        }

        Button clickedButton = (Button) event.getSource();
        if (!clickedButton.getText().isEmpty()) return;

        clickedButton.setText(client.getMySymbol());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == clickedButton) {
                    try {
                        client.sendToServer("move:" + i + "," + j);
                        client.setMyTurn(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    @FXML
    private void restartGame(ActionEvent event) {
        restartBoard();
        client.setMyTurn(false);
        try {
            client.sendToServer("restart");
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultLabel.setText("Game restarted.");
    }

    private void restartBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j].setText("");
                board[i][j].setDisable(false);
            }
        }
    }

    @Subscribe
    public void onTurnChange(String message) {
        if (message.startsWith("TURN")) {
            currentTurn = Integer.parseInt(message.split(" ")[1]);
            System.out.println("Turn updated to player: " + currentTurn);
        }
    }

    @Subscribe
    public void onMessageReceived(String message) {
        if (message.startsWith("move:")) {
            String[] parts = message.substring(5).split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            String symbol = parts[2];

            Button btn = getButton(row, col);
            if (btn != null && btn.getText().isEmpty()) {
                Platform.runLater(() -> {
                    btn.setText(symbol);
                    btn.setDisable(true);
                });
            }
        }
    }


    @Subscribe
    public void onGameResult(String result) {
        Platform.runLater(() -> {
            if (result.startsWith("Winner")) {
                disableBoard();
                boolean won = (result.contains("X") && client.getPlayerNumber() == 1) ||
                        (result.contains("O") && client.getPlayerNumber() == 2);
                resultLabel.setText(won ? "You Win!" : "You Lose.");
            } else if (result.equals("Draw")) {
                disableBoard();
                resultLabel.setText("It's a Draw!");
            }
        });
    }

    private Button getButton(int row, int col) {
        if (row == 0 && col == 0) return btn00;
        if (row == 0 && col == 1) return btn01;
        if (row == 0 && col == 2) return btn02;
        if (row == 1 && col == 0) return btn10;
        if (row == 1 && col == 1) return btn11;
        if (row == 1 && col == 2) return btn12;
        if (row == 2 && col == 0) return btn20;
        if (row == 2 && col == 1) return btn21;
        if (row == 2 && col == 2) return btn22;
        return null;
    }

    private void disableBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j].setDisable(true);
            }
        }
    }
}
