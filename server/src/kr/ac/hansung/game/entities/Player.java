package kr.ac.hansung.game.entities;

import kr.ac.hansung.game.Game;
import kr.ac.hansung.game.InputHandler;
import kr.ac.hansung.game.level.Level;
import kr.ac.hansung.game.net.packets.Packet02Move;
import kr.ac.hansung.gfx.Colours;
import kr.ac.hansung.gfx.Font;
import kr.ac.hansung.gfx.Screen;

public class Player extends Mob {
	
    private InputHandler input;

    // 케릭터 배경색, 머리/바지 색, 옷 색, 피부 색
    // private int colour = Colours.get(-1, 111, 200, 543);
    // 케릭터 크기
    private int scale = 1;
    public boolean isSwimming = false;
    private int tickCount = 0;
    private String username;

    public Player(Level level, int x, int y, int colour, InputHandler input, String username) {
        super(level, "Player", x, y, colour, 4);
        this.input = input;
        this.username = username;
    }

    public void tick() {
        int xa = 0;
        int ya = 0;
        if (input != null) {
        	// 케릭터 속도 조절 가능
            if (input.up.isPressed()) {
                ya--;
            }
            if (input.down.isPressed()) {
                ya++;
            }
            if (input.left.isPressed()) {
                xa--;
            }
            if (input.right.isPressed()) {
                xa++;
            }
        }
        if (xa != 0 || ya != 0) {
            move(xa, ya);
            isMoving = true;
            // 0이 아닐 대 움직이는 것이므로 MOVE 패킷 보냄
            Packet02Move packet = new Packet02Move(this.getUsername(), this.x, this.y, this.numSteps, this.isMoving,
                    this.movingDir, this.whoCatch, this.whoRelease, this.speed);
            packet.writeData(Game.game.socketClient);
        } else {
            isMoving = false;
        }
        if (level.getTile(this.x >> 3, this.y >> 3).getId() == 3) {
            isSwimming = true;
        }
        if (isSwimming && level.getTile(this.x >> 3, this.y >> 3).getId() != 3) {
            isSwimming = false;
        }
        tickCount++;
    }

    public void render(Screen screen) {
        int xTile = 0;
        int yTile = 28;
        // 숫자 작을 수록 움직임 표현하는 동작 빨라짐
        int walkingSpeed = 2;
        int flipTop = (numSteps >> walkingSpeed) & 1;
        int flipBottom = (numSteps >> walkingSpeed) & 1;

        if (movingDir == 1) {
            xTile += 2;
        } else if (movingDir > 1) {
            xTile += 4 + ((numSteps >> walkingSpeed) & 1) * 2;
            flipTop = (movingDir - 1) % 2;
        }

        int modifier = 8 * scale;
        int xOffset = x - modifier / 2;
        int yOffset = y - modifier / 2 - 4;
        if (isSwimming) {
        	if(speed != 0)
        		speed = 1;
        	
            int waterColour = 0;
            yOffset += 4;
            if (tickCount % 60 < 15) {
                waterColour = Colours.get(-1, -1, 225, -1);
            } else if (15 <= tickCount % 60 && tickCount % 60 < 30) {
                yOffset -= 1;
                waterColour = Colours.get(-1, 225, 115, -1);
            } else if (30 <= tickCount % 60 && tickCount % 60 < 45) {
                waterColour = Colours.get(-1, 115, -1, 225);
            } else {
                yOffset -= 1;
                waterColour = Colours.get(-1, 225, 115, -1);
            }
            screen.render(xOffset, yOffset + 3, 0 + 27 * 32, waterColour, 0x00, 1);
            screen.render(xOffset + 8, yOffset + 3, 0 + 27 * 32, waterColour, 0x01, 1);
        }
        screen.render(xOffset + (modifier * flipTop), yOffset, xTile + yTile * 32, colour, flipTop, scale);
        screen.render(xOffset + modifier - (modifier * flipTop), yOffset, (xTile + 1) + yTile * 32, colour, flipTop,
                scale);

        if (!isSwimming) {
        	if(speed != 0)
        		speed = 4;
        	
            screen.render(xOffset + (modifier * flipBottom), yOffset + modifier, xTile + (yTile + 1) * 32, colour,
                    flipBottom, scale);
            screen.render(xOffset + modifier - (modifier * flipBottom), yOffset + modifier, (xTile + 1) + (yTile + 1)
                    * 32, colour, flipBottom, scale);
        }
        if (username != null) {
            Font.render(username, screen, xOffset - ((username.length() - 1) / 2 * 8), yOffset - 10,
                    Colours.get(-1, -1, -1, 555), 1);
        }
    }

    public boolean hasCollided(int xa, int ya) {
        int xMin = 0;
        int xMax = 7;
        int yMin = 3;
        int yMax = 7;
        for (int x = xMin; x < xMax; x++) {
            if (isSolidTile(xa, ya, x, yMin)) {
                return true;
            }
        }
        for (int x = xMin; x < xMax; x++) {
            if (isSolidTile(xa, ya, x, yMax)) {
                return true;
            }
        }
        for (int y = yMin; y < yMax; y++) {
            if (isSolidTile(xa, ya, xMin, y)) {
                return true;
            }
        }
        for (int y = yMin; y < yMax; y++) {
            if (isSolidTile(xa, ya, xMax, y)) {
                return true;
            }
        }
        return false;
    }
    
    public void setUsername(String username) {
    	this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
