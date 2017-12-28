package kr.ac.hansung.game.entities;

import java.net.InetAddress;

import kr.ac.hansung.game.InputHandler;
import kr.ac.hansung.game.level.Level;

public class PlayerMP extends Player {

    public InetAddress ipAddress;
    public int port;

    public PlayerMP(Level level, int x, int y, int colour, InputHandler input, String username, InetAddress ipAddress, int port) {
        super(level, x, y, colour, input, username);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public PlayerMP(Level level, int x, int y, int colour, String username, InetAddress ipAddress, int port) {
        super(level, x, y, colour, null, username);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public void tick() {
        super.tick();
    }
}
