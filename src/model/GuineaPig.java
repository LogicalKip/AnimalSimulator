package model;


import java.util.Random;

import model.management.Animal;
import model.management.Coordinate;
import model.management.DetectedAnimal;
import model.management.DetectedGrass;
import model.management.Simulator;

public class GuineaPig extends Animal {
	private DetectedGrass nearestGrass;

	private final int TIME_BEFORE_LOOKING_ELSEWHERE = 60;

	private boolean fleeing;
	
	public GuineaPig(int x, int y, Simulator s) throws IllegalArgumentException {
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
	
	@Override
	public void onRiverDetected(Coordinate a, Coordinate b) {
		//TODO
	}

	private void processGrass(DetectedGrass g) {
		if (closeEnoughToEat(nearestGrass)) {
			sit();
			if (isHungry()) {
				eat(g);
			}
		} else {
			if (isHungry()) {
				moveTowards(nearestGrass.getPosX(), nearestGrass.getPosY());
				eat(g);
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

	private boolean closeEnoughToEat(DetectedGrass g) {
		return Simulator.euclidianDistance(getPosX(), getPosY(), g.getPosX(), g.getPosY()) <= g.getMaxDistanceToEat();
	}

	private boolean isHungry() {
		return this.getFullness() < this.getMaxFullnessValue()*0.75;
	}

	private void randomMoves() {
		Random r = new Random();
		setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
	}

	@Override
	public void onGrassDetected(DetectedGrass g) {
		if (nearestGrass == null || nearestGrass.getAmount() == 0 || Simulator.euclidianDistance(this.getPosX(), this.getPosY(), g.getPosX(), g.getPosY()) <= Simulator.euclidianDistance(this.getPosX(), this.getPosY(), nearestGrass.getPosX(), nearestGrass.getPosY())) {
			this.nearestGrass = g;
		}
	}

	@Override
	public void onAnimalDetected(DetectedAnimal a) {
		super.onAnimalDetected(a);

		if (a.getDetectedAnimalClass().equals(this.getClass())) {
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
		improveDetectionDistance(this.getAdnPoints());
	}

	private boolean idle() {
		return getDirX() == 0 && getDirY() == 0;
	}
}
