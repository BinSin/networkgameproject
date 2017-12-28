package kr.ac.hansung.game.net.packets;

import kr.ac.hansung.game.net.GameClient;
import kr.ac.hansung.game.net.GameServer;

public class Packet00Login extends Packet {

    private String username;
    private int x, y;
    private int colour;

    public Packet00Login(byte[] data) {
        super(00);
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.x = Integer.parseInt(dataArray[1]);
        this.y = Integer.parseInt(dataArray[2]);
        this.colour = Integer.parseInt(dataArray[3]);
    }

    public Packet00Login(String username, int x, int y, int colour) {
        super(00);
        this.username = username;
        this.x = x;
        this.y = y;
        this.colour = colour;
    }

	@Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }

    @Override
    public byte[] getData() {
        return ("00" + this.username + "," + getX() + "," + getY() + "," + getColour()).getBytes();
    }

    public String getUsername() {
        return username;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getColour() {
    	return colour;
    }

}
