package kr.ac.hansung.game.net.packets;

import kr.ac.hansung.game.net.GameClient;
import kr.ac.hansung.game.net.GameServer;

public class Packet02Move extends Packet {

    private String username;
    private int x, y;

    private int numSteps = 0;
    private boolean isMoving;
    private int movingDir = 1;
    private String whoCatch = null;
    private String whoRelease = null;
    private int speed = 2;

    public Packet02Move(byte[] data) {
        super(02);
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.x = Integer.parseInt(dataArray[1]);
        this.y = Integer.parseInt(dataArray[2]);
        this.numSteps = Integer.parseInt(dataArray[3]);
        this.isMoving = Integer.parseInt(dataArray[4]) == 1;
        this.movingDir = Integer.parseInt(dataArray[5]);
        this.whoCatch = dataArray[6];
        this.whoRelease = dataArray[7];
        this.speed = Integer.parseInt(dataArray[8]);
    }

    public Packet02Move(String username, int x, int y, int numSteps, boolean isMoving, int movingDir, String whoCatch, String whoRelease, int speed) {
        super(02);
        this.username = username;
        this.x = x;
        this.y = y;
        this.numSteps = numSteps;
        this.isMoving = isMoving;
        this.movingDir = movingDir;
        this.whoCatch = whoCatch;
        this.whoRelease = whoRelease;
        this.speed = speed;
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
        return ("02" + this.username + "," + this.x + "," + this.y + "," + this.numSteps + "," + (isMoving ? 1 : 0)
                + "," + this.movingDir + "," + this.whoCatch + "," + this.whoRelease + "," + this.speed).getBytes();

    }

    public String getUsername() {
        return username;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public int getMovingDir() {
        return movingDir;
    }
    
    public String getWhoCatch() {
    	return whoCatch;
    }
    
    public void setWhoCatch(String whoCatch) {
    	this.whoCatch = whoCatch;
    }
 
    public String getWhoRelease() {
    	return whoRelease;
    }
    
    public void setWhoRelease(String whoRelease) {
    	this.whoRelease = whoRelease;
    }
    
    public int getSpeed() {
    	return speed;
    }
    
    public void setSpeed(int speed) {
    	this.speed = speed;
    }
}
