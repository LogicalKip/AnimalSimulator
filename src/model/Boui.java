package model;

import java.util.Random;

import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class Boui extends Animal {
	private Grass nearestGrass;

	public Boui(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		changeImage("guinea_pig_icon.gif");
		improveSpeed(this.getAdnPoints());
		this.nearestGrass = null;
	}


	@Override
	public void behave() {
		if (nearestGrass != null && nearestGrass.getAmount() == 0) {
			nearestGrass = null;
		}
		if (nearestGrass == null) {
			randomMoves();			
		} else {
			moveTowards(nearestGrass.getPosX(), nearestGrass.getPosY());
		}
	}

	private void randomMoves() {
		if (getDirX() == 0 && getDirY() == 0) {
			Random r = new Random();
			setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
			setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		}
	}

	@Override
	public void onGrassDetected(Grass g) {
		if (nearestGrass == null || Simulator.euclidianDistance(this.getPosX(), this.getPosY(), g.getPosX(), g.getPosY()) <= 
				Simulator.euclidianDistance(this.getPosX(), this.getPosY(), nearestGrass.getPosX(), nearestGrass.getPosY())) {
			this.nearestGrass = g;
		}
	}

	@Override
	protected void chooseInitialDiet() {
		beHerbivore();
	}


	@Override
	protected void onBirth(Animal parent1, Animal parent2) {
		improveSpeed(this.getAdnPoints());
	}
}
