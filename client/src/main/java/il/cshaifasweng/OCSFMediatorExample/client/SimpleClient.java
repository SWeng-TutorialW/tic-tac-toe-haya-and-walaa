package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;

	private String mySymbol = ""; // "X" or "O"
	private boolean myTurn = false;

	private int playerNumber = 0;
	private int currentTurn = 1;

	private char[][] board = new char[3][3];

	private SimpleClient(String host, int port) {
		super(host, port);
		resetBoard();
	}

	private void resetBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = ' ';
			}
		}
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Warning) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
			return;
		}

		String message = msg.toString();
		System.out.println("[Client] Received: " + message);

		if (message.startsWith("player:")) {
			mySymbol = message.split(":")[1];
			System.out.println("You are player: " + mySymbol);
		} else if (message.startsWith("PLAYER")) {
			playerNumber = Integer.parseInt(message.split(" ")[1]);
			System.out.println("Assigned player number: " + playerNumber);
		} else if (message.equals("your turn")) {
			myTurn = true;
		} else if (message.startsWith("move:")) {
			EventBus.getDefault().post(message);
		} else if (message.startsWith("TURN")) {
			currentTurn = Integer.parseInt(message.split(" ")[1]);
			EventBus.getDefault().post("TURN " + currentTurn);
		} else if (message.equals("draw") || message.startsWith("win:") || message.startsWith("Winner")) {
			EventBus.getDefault().post(message);
		} else if (message.startsWith("startGame")) {
			EventBus.getDefault().post("startGame");
		} else {
			EventBus.getDefault().post(message);
		}
	}

	public void sendMoveToServer(int row, int col) {
		try {
			sendToServer("move:" + row + "," + col);
		} catch (IOException e) {
			System.err.println("Failed to send move: " + e.getMessage());
		}
	}


	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	public String getMySymbol() {
		return mySymbol;
	}

	public boolean isMyTurn() {
		return myTurn;
	}

	public void setMyTurn(boolean myTurn) {
		this.myTurn = myTurn;
	}

	public int getPlayerNumber() {
		return playerNumber;
	}

	public void setPlayerNumber(int playerNumber) {
		this.playerNumber = playerNumber;
	}

	public int getCurrentTurn() {
		return currentTurn;
	}
}
