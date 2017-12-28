package kr.ac.hansung.game.entities;

import kr.ac.hansung.game.level.Level;
import kr.ac.hansung.gfx.Screen;

public abstract class Entity {

	// 저장할 케릭터 정보
	public int x, y, colour;
	protected Level level;

	public Entity(Level level) {
		init(level);
	}

	public final void init(Level level) {
		this.level = level;
	}

	public abstract void tick();

	public abstract void render(Screen screen);
}
