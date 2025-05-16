package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleServer extends AbstractServer {

	private final List<SubscribedClient> clients = new ArrayList<>();
	private ConnectionToClient playerX = null;
	private ConnectionToClient playerO = null;
	private ConnectionToClient currentTurn = null;
	private int readyCount = 0;

	private char[][] board = new char[3][3];

	public SimpleServer(int port) {
		super(port);
		initializeBoard();
	}

	private void initializeBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = ' ';
			}
		}
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String message = msg.toString();

		try {
			if (message.startsWith("#warning")) {
				Warning warning = new Warning("Server Warning!");
				client.sendToClient(warning);
				return;
			}

			if (message.equals("remove client")) {
				clients.removeIf(sc -> sc.getClient().equals(client));
				if (client.equals(playerX)) playerX = null;
				if (client.equals(playerO)) playerO = null;
				return;
			}

			if (message.startsWith("add client")) {
				SubscribedClient newClient = new SubscribedClient(client);
				clients.add(newClient);

				if (playerX == null) {
					playerX = client;
					client.sendToClient("player:X");
					currentTurn = playerX;
					client.sendToClient("your turn");
				} else if (playerO == null) {
					playerO = client;
					client.sendToClient("player:O");
				} else {
					client.sendToClient("spectator");
				}
				return;
			}

			if (message.equals("client ready")) {
				readyCount++;
				if (readyCount == 2) {
					initializeBoard();
					sendToAll("startGame");
				}
				return;
			}

			if (message.startsWith("move:")) {
				if (client != currentTurn) return;

				String[] parts = message.substring(5).split(",");
				int row = Integer.parseInt(parts[0]);
				int col = Integer.parseInt(parts[1]);

				char symbol = client.equals(playerX) ? 'X' : 'O';

				if (board[row][col] != ' ') return;

				board[row][col] = symbol;

				sendToAll("move:" + row + "," + col + "," + symbol);

				if (checkWin(symbol)) {
					sendToAll("Winner " + symbol);
					return;
				}

				if (isDraw()) {
					sendToAll("Draw");
					return;
				}

				if (client.equals(playerX) && playerO != null) {
					currentTurn = playerO;
					playerO.sendToClient("your turn");
				} else if (client.equals(playerO) && playerX != null) {
					currentTurn = playerX;
					playerX.sendToClient("your turn");
				}

				return;
			}

			if (message.equals("restart")) {
				initializeBoard();
				sendToAll("restart");
				currentTurn = playerX;
				if (playerX != null) playerX.sendToClient("your turn");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkWin(char mark) {
		for (int i = 0; i < 3; i++) {
			if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark) return true;
			if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark) return true;
		}

		if (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark) return true;
		if (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark) return true;

		return false;
	}

	private boolean isDraw() {
		for (char[] row : board) {
			for (char c : row) {
				if (c == ' ') return false;
			}
		}
		return true;
	}

	private void sendToAll(Object message) {
		for (SubscribedClient client : clients) {
			try {
				client.getClient().sendToClient(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
