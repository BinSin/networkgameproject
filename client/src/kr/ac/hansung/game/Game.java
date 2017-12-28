package kr.ac.hansung.game;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Audio.JukeBox;
import chatViusal.ChatVisual;
import kr.ac.hansung.game.entities.Player;
import kr.ac.hansung.game.entities.PlayerMP;
import kr.ac.hansung.game.level.Level;
import kr.ac.hansung.game.net.GameClient;
import kr.ac.hansung.game.net.GameServer;
import kr.ac.hansung.game.net.packets.Packet00Login;
import kr.ac.hansung.gfx.Colours;
import kr.ac.hansung.gfx.Screen;
import kr.ac.hansung.gfx.SpriteSheet;

public class Game extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 280;
    public static final int HEIGHT = WIDTH / 12 * 9;
    public static final int SCALE = 3;
    public static final String NAME = "Game";
    public static final Dimension DIMENSIONS = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
    public static Game game;
    public GameEvents gameEvents;

    private int randomX = (int)(Math.random() * 300) + 300;
    private int randomY = (int)(Math.random() * 300) + 300;
    // 케릭터 배경색, 머리/바지 색, 옷 색, 피부 색
    private int randomColour = Colours.get(-1, 111, (int)(Math.random()*1000) + 200, 543);
    
    public JFrame frame;

    private Thread thread;

    public boolean running = false;
    public int tickCount = 0;

    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    private int[] colours = new int[6 * 6 * 6];

    private Screen screen;
    public InputHandler input;
    public WindowHandler windowHandler;
    public Level level;
    public Player player;

    public GameClient socketClient;
    public GameServer socketServer;

    public boolean debug = true;
    public boolean isApplet = false;
    
    public static String name ; 
    public  ChatVisual chatVisual= new ChatVisual(name=JOptionPane.showInputDialog("캐릭터 이름을 입력해 주세요."));//chatServer start 
   
    
    public void init() {
        game = this;
        gameEvents = new GameEvents();
        
        int index = 0;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    int rr = (r * 255 / 5);
                    int gg = (g * 255 / 5);
                    int bb = (b * 255 / 5);

                    colours[index++] = rr << 16 | gg << 8 | bb;
                }
            }
        }
        
        screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/sprite_sheet3.png"));
        input = new InputHandler(this);
        level = new Level("/levels/level_1.png");
        // level, 처음 케릭터 위치 x, y, 케릭터옷 색, input, 사용자 이름, ip, port
        
        // 서버와 동시에 켜질 운영자를 생성
        // player = new PlayerMP(level, randomX, randomY, input, "Master", null, -1);
        
        // 클라이언트는 이 부분 그대로
        player = new PlayerMP(level, randomX, randomY, randomColour, input, name, null, -1);
        
        // entity 추가
        level.addEntity(player);
        if (!isApplet) {
            Packet00Login loginPacket = new Packet00Login(player.getUsername(), player.x, player.y, player.colour);
            if (socketServer != null) {
                socketServer.addConnection((PlayerMP) player, loginPacket);
            }
            loginPacket.writeData(socketClient);
        }
        
      //music
    	JukeBox.init();
    	JukeBox.load("/background.mp3", "background");
    	JukeBox.loop("background", 600, JukeBox.getFrames("background") - 2200);
    }

    public synchronized void start() {
        running = true;

        thread = new Thread(this, NAME + "_main");
        thread.start();
        if (!isApplet) {
           // socketClient = new GameClient(this, new String(JOptionPane.showInputDialog(this, "접속할 서버의 IP를 입력해 주세요.").trim()));
        	socketClient = new GameClient(this, "localhost");
        	socketClient.start();
        }
    }

    public synchronized void stop() {
        running = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        // 속도 조절 가능
        double nsPerTick = 1000000000D / 40D;

        int ticks = 0;
        int frames = 0;

        long lastTimer = System.currentTimeMillis();
        double delta = 0;

        init();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            boolean shouldRender = true;

            while (delta >= 1) {
                ticks++;
                tick();
                delta -= 1;
                shouldRender = true;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (shouldRender) {
                frames++;
                render();
            }

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                debug(DebugLevel.INFO, ticks + " ticks, " + frames + " frames");
                frames = 0;
                ticks = 0;
            }
        }
    }

    public void tick() {
        tickCount++;
        level.tick();
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        // 케릭터를 보여주는 카메라 위치
        int xOffset = player.x - (screen.width / 2);
        int yOffset = player.y - (screen.height / 2);

        level.renderTiles(screen, xOffset, yOffset);
        level.renderEntities(screen);

        gameEvents.renderPlayerEvents(level);
        
        for (int y = 0; y < screen.height; y++) {
            for (int x = 0; x < screen.width; x++) {
                int colourCode = screen.pixels[x + y * screen.width];
                if (colourCode < 255)
                    pixels[x + y * WIDTH] = colours[colourCode];
            }
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();
    }

    public static long fact(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * fact(n - 1);
        }
    }

    public void debug(DebugLevel level, String msg) {
        switch (level) {
        default:
        case INFO:
            if (debug) {
                System.out.println("[" + NAME + "] " + msg);
            }
            break;
        case WARNING:
            System.out.println("[" + NAME + "] [WARNING] " + msg);
            break;
        case SEVERE:
            System.out.println("[" + NAME + "] [SEVERE]" + msg);
            this.stop();
            break;
        }
    }

    public static enum DebugLevel {
        INFO, WARNING, SEVERE;
    }
}
