package model;


import java.util.Random;

import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class Boui extends Animal {
	private Grass nearestGrass;

	private final int TIME_BEFORE_LOOKING_ELSEWHERE = 60;

	private boolean fleeing;

	public Boui(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		changeImage("guinea_pig_icon.gif");
		changeDeadImage("guinea_pig_icon_dead.gif");
		improveSpeed(this.getAdnPoints());
		this.nearestGrass = null;
		this.fleeing = false;
	}


	@Override
	public void behave() {
		if (nearestGrass != null && nearestGrass.getAmount() == 0) {
			nearestGrass = null;
		}
		if (nearestGrass == null) {
			if (idle() || sometimes()) {
				randomMoves();
			}
		} else if (!fleeing){
			processGrass(nearestGrass);
		}

		this.fleeing = false;
		
		this.nearestGrass = null;
	}

	private boolean sometimes() {
		return (this.getSimulator().getTicksElapsed() + 
				new Random().nextInt((int)(TIME_BEFORE_LOOKING_ELSEWHERE*0.2)))
				% TIME_BEFORE_LOOKING_ELSEWHERE 
				== 0;
	}

	private void processGrass(Grass g) {
		if (closeEnoughToEat(nearestGrass)) {
			if (isHungry()) {
				sit();
			} else {
				moveAwayFrom(nearestGrass.getPosX(), nearestGrass.getPosY());
			}
		} else {
			if (isHungry()) {
				moveTowards(nearestGrass.getPosX(), nearestGrass.getPosY());
			} else {
				// not hungry, not in grass
				randomMoves();
			}
		}
	}

	private void sit() {
		this.setDirX(0);
		this.setDirY(0);
	}

	private boolean closeEnoughToEat(Grass g) {
		return Simulator.euclidianDistance(getPosX(), getPosY(), g.getPosX(), g.getPosY()) <= getSimulator().MAX_DISTANCE_TO_EAT;
	}

	private boolean isHungry() {
		return this.getFullness() < this.getMaxFullness()*0.75;
	}

	private void randomMoves() {
		Random r = new Random();
		setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
	}

	@Override
	public void onGrassDetected(Grass g) {
		if (nearestGrass == null || nearestGrass.getAmount() == 0 || Simulator.euclidianDistance(this.getPosX(), this.getPosY(), g.getPosX(), g.getPosY()) <= Simulator.euclidianDistance(this.getPosX(), this.getPosY(), nearestGrass.getPosX(), nearestGrass.getPosY())) {
			this.nearestGrass = g;
		}
	}

	@Override
	public void onAnimalDetected(Animal a) {
		super.onAnimalDetected(a);

		if (a instanceof Boui) {
			mate(a);
		} else if (a.isAlive()) {
			// Flee
			moveAwayFrom(a.getPosX(), a.getPosY());
			this.fleeing = true;
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

	private boolean idle() {
		return getDirX() == 0 && getDirY() == 0;
	}
}
