package kr.ac.hansung.game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Audio.JukeBox;
import chatViusal.ChatVisual;
import kr.ac.hansung.game.Game;
import kr.ac.hansung.game.GameEvents;
import kr.ac.hansung.game.entities.PlayerMP;
import kr.ac.hansung.game.net.packets.Packet;
import kr.ac.hansung.game.net.packets.Packet00Login;
import kr.ac.hansung.game.net.packets.Packet01Disconnect;
import kr.ac.hansung.game.net.packets.Packet02Move;
import kr.ac.hansung.game.net.packets.Packet.PacketTypes;

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;

	public GameClient(Game game, String ipAddress) {
		this.game = game;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
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
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			handleLogin((Packet00Login) packet, address, port);
			//ChatVisual.chatScreen.append(((Packet00Login) packet).getUsername()+"이 접속했습니다");
			//ChatVisual.send_Message(((Packet00Login) packet).getUsername()+"이 접속했습니다");
			// System.out.println(((Packet00Login) packet).getUsername()+"이 접속했습니다");
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println(((Packet01Disconnect) packet).getUsername() + " 가 게임을 종료하였습니다.");
			// ChatVisual.chatScreen.append(((Packet01Disconnect) packet).getUsername() + " 가 게임을 종료하였습니다.");
			game.level.removePlayerMP(((Packet01Disconnect) packet).getUsername());
			break;
		case MOVE:
			packet = new Packet02Move(data);
			
			
			// 잡혔을 때 스피드 0 으로
			if (((Packet02Move) packet).getWhoCatch().equals(Game.name) && game.player.getSpeed() != 0) {
				((Packet02Move) packet).setSpeed(((Packet02Move) packet).getSpeed());
				game.player.setSpeed(0);
				System.out.println("당신은 잡혔습니다.");
				
				JukeBox.load("/playercatch2.mp3", "playercatch");
				JukeBox.play("playercatch");
			}
			else if (((Packet02Move) packet).getWhoRelease().equals(Game.name) && game.player.getSpeed() == 0) {
				((Packet02Move) packet).setSpeed(((Packet02Move) packet).getSpeed());
				game.player.setSpeed(3);
				System.out.println("당신은 풀려났습니다.");
				JukeBox.load("/playerRelease.wav", "playerRelease");
				JukeBox.play("playerRelease");
			}
			
			if (((Packet02Move) packet).getSpeed() == 10) {
				GameEvents.check = true;
				break;
			}
			
			handleMove((Packet02Move) packet);
			
		}
		
	}

	public void sendData(byte[] data) {
		if (!game.isApplet) {
			DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleLogin(Packet00Login packet, InetAddress address, int port) {
		PlayerMP player = new PlayerMP(game.level, packet.getX(), packet.getY(), packet.getColour(),
				packet.getUsername(), address, port);
		game.level.addEntity(player);
	}

	private void handleMove(Packet02Move packet) {
		this.game.level.movePlayer(packet.getUsername(), packet.getX(), packet.getY(), packet.getNumSteps(),
				packet.isMoving(), packet.getMovingDir(), packet.getWhoCatch(), packet.getWhoRelease(), packet.getSpeed());
	}
}
