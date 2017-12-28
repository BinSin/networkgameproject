package kr.ac.hansung.game;

import Audio.JukeBox;
import kr.ac.hansung.game.level.Level;

public class GameEvents {

	public static boolean check = false;

	public GameEvents() {
	
		
	}

	public void renderPlayerEvents(Level level) {
		if (check) {
			Game.game.level = new Level("/levels/you_are_dead.png");
			JukeBox.stop("background");
			//JukeBox.load("/gameover.wav", "gameover");
			
		}
		//JukeBox.play("gameover");
	}
}