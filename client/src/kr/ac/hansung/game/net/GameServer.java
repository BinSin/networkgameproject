package kr.ac.hansung.game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import Audio.JukeBox;
import chatViusal.ChatVisual;
import kr.ac.hansung.game.Game;
import kr.ac.hansung.game.entities.PlayerMP;
import kr.ac.hansung.game.net.packets.Packet;
import kr.ac.hansung.game.net.packets.Packet00Login;
import kr.ac.hansung.game.net.packets.Packet01Disconnect;
import kr.ac.hansung.game.net.packets.Packet02Move;
import kr.ac.hansung.game.net.packets.Packet.PacketTypes;

public class GameServer extends Thread {

	private DatagramSocket socket;
	private Game game;
	public List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>(40);

	public GameServer(Game game) {
		this.game = game;
		try {
			this.socket = new DatagramSocket(1331);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		// message에서 앞의 숫자만 가져온다.
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			System.out.println(((Packet00Login) packet).getUsername() + " 님이 접속하였습니다.");
			
			// 케릭터 설정 부분에서 이곳 주의!
			PlayerMP player = new PlayerMP(game.level, 50, 50, ((Packet00Login) packet).getColour(),
					((Packet00Login) packet).getUsername(), address, port);
			this.addConnection(player, (Packet00Login) packet);
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println(((Packet01Disconnect) packet).getUsername() + " 님이 종료하였습니다.");
			this.removeConnection((Packet01Disconnect) packet);
			break;
		case MOVE:
			packet = new Packet02Move(data);

			((Packet02Move) packet).setWhoCatch(null);
			((Packet02Move) packet).setWhoRelease(null);
			int sullraeX = this.connectedPlayers.get(0).x;
			int sullraeY = this.connectedPlayers.get(0).y;
			if (this.connectedPlayers.size() > 0) {
				for (int i = 1; i < this.connectedPlayers.size(); i++) {
					int thisX = this.connectedPlayers.get(i).x;
					int thisY = this.connectedPlayers.get(i).y;
					if (((thisX > sullraeX - 5 && thisX < sullraeX + 5)
							&& (thisY > sullraeY - 5 && thisY < sullraeY + 5))
							|| ((sullraeX > thisX - 5 && sullraeX < thisX + 5))
									&& (sullraeY > thisY - 5 && sullraeY < thisY + 5)) {
						if (connectedPlayers.get(i).getSpeed() != 0) {
							((Packet02Move) packet).setWhoCatch(connectedPlayers.get(i).getUsername());
							((Packet02Move) packet).setSpeed(0);
							connectedPlayers.get(i).setSpeed(0);
							System.out.println(connectedPlayers.get(i).getUsername() + "을 잡았습니다.");
							
						}
					}

					for (int j = 1; j < this.connectedPlayers.size() && i != j; j++) {
						int freezeX = this.connectedPlayers.get(j).x;
						int freezeY = this.connectedPlayers.get(j).y;

						if (connectedPlayers.get(i).getSpeed() > 0) {
							if (((freezeX > thisX - 5 && freezeX < thisX + 5)
									&& (freezeY > thisY - 5 && freezeY < thisY + 5))
									|| ((thisX > freezeX - 5 && thisX < freezeX + 5)
											&& (thisY > freezeY - 5 && thisY < freezeY + 5))) {
								if (connectedPlayers.get(j).getSpeed() == 0) {
									((Packet02Move) packet).setWhoRelease(connectedPlayers.get(j).getUsername());
									((Packet02Move) packet).setSpeed(2);
									connectedPlayers.get(j).setSpeed(2);
									System.out.println(connectedPlayers.get(i).getUsername() + "가 "
											+ connectedPlayers.get(j).getUsername() + "를 풀어줬습니다.");
									
								}
							}
						} else if (connectedPlayers.get(i).getSpeed() == 0) {
							if (((freezeX > thisX - 5 && freezeX < thisX + 5)
									&& (freezeY > thisY - 5 && freezeY < thisY + 5))
									|| ((thisX > freezeX - 5 && thisX < freezeX + 5)
											&& (thisY > freezeY - 5 && thisY < freezeY + 5))) {
								((Packet02Move) packet).setWhoRelease(connectedPlayers.get(i).getUsername());
								((Packet02Move) packet).setSpeed(2);
								connectedPlayers.get(i).setSpeed(2);
								System.out.println(connectedPlayers.get(j).getUsername() + "가 "
										+ connectedPlayers.get(i).getUsername() + "를 풀어줬습니다.");
								JukeBox.load("/playerRelease.wav", "playerRelease");
								JukeBox.play("playerRelease");
							}
						}
					}

				}
			}

			this.handleMove(((Packet02Move) packet));
			game.player.setSpeed(4);
		}

	}

	public void addConnection(PlayerMP player, Packet00Login packet) {
		boolean alreadyConnected = false;
		for (PlayerMP p : this.connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				if (p.ipAddress == null) {
					p.ipAddress = player.ipAddress;
				}
				if (p.port == -1) {
					p.port = player.port;
				}
				alreadyConnected = true;
			} else {
				// relay to the current connected player that there is a new
				// player
				sendData(packet.getData(), p.ipAddress, p.port);

				// relay to the new player that the currently connect player
				// exists
				Packet00Login packetCurrentPlayer = new Packet00Login(p.getUsername(), p.x, p.y, p.colour);
				sendData(packetCurrentPlayer.getData(), player.ipAddress, player.port);

			}
		}
		if (!alreadyConnected) {
			this.connectedPlayers.add(player);
		}
	}

	public void removeConnection(Packet01Disconnect packet) {
		this.connectedPlayers.remove(getPlayerMPIndex(packet.getUsername()));
		packet.writeData(this);
	}

	public PlayerMP getPlayerMP(String username) {
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equals(username)) {
				return player;
			}
		}
		return null;
	}

	public int getPlayerMPIndex(String username) {
		int index = 0;
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equals(username)) {
				break;
			}
			index++;
		}
		return index;
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		if (!game.isApplet) {

			DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
			try {
				this.socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendDataToAllClients(byte[] data) {
		for (PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

	private void handleMove(Packet02Move packet) {
		if (getPlayerMP(packet.getUsername()) != null) {
			int index = getPlayerMPIndex(packet.getUsername());
			PlayerMP player = this.connectedPlayers.get(index);
			player.x = packet.getX();
			player.y = packet.getY();
			player.setMoving(packet.isMoving());
			player.setMovingDir(packet.getMovingDir());
			player.setNumSteps(packet.getNumSteps());
			player.setWhoCatch(packet.getWhoCatch());
			player.setWhoRelease(packet.getWhoRelease());
			player.setSpeed(packet.getSpeed());
			packet.writeData(this);
		}
	}

}
