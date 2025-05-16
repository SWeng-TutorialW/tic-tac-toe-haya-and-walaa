package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class PrimaryController {

	@FXML private AnchorPane anchor1;
	@FXML private Label searchLabel;
	@FXML private Button startGameBtn;

	@FXML
	void initialize() {
		EventBus.getDefault().register(this);

		try {
			SimpleClient.getClient().sendToServer("add client");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void onClickStartGame(ActionEvent event) {
		try {
			SimpleClient.getClient().sendToServer("client ready");
			startGameBtn.setVisible(false);
			if (searchLabel != null) {
				searchLabel.setText("Searching for another player...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void sendWarning(ActionEvent event) {
		System.out.println("Send warning clicked!");
	}

	@Subscribe
	public void onWarningEvent(WarningEvent event) {
		System.out.println("[Warning] " + event.getWarning().getMessage());
	}

	@Subscribe
	public void onMessageFromServer(String msg) {
		if (msg.equals("startGame")) {
			try {
				System.out.println("[Client] Switching to game screen...");
				App.setRoot("secondary");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void showGameOverAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Game Over");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
