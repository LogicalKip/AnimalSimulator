package model;

import java.util.Random;

import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class Boui extends Animal {
	public Boui(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		changeImage("guinea_pig_icon.gif");
		improveSpeed(this.getAdnPoints());
	}

	@Override
	public void behave() {
		randomMoves();
	}

	private void randomMoves() {
		if (getDirX() == 0 && getDirY() == 0) {
			Random r = new Random();
			setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
			setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		}
	}

	@Override
	public void onFoodDetected(Grass f) {
		moveTowards(f.getPosX(), f.getPosY());
	}

	@Override
	protected void chooseInitialDiet() {
		beHerbivore();
	}
}
