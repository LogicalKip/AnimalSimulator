package model;

import java.util.Random;

import model.management.Animal;
import model.management.DetectedAnimal;
import model.management.Simulator;

/**
 * Animal that is dangerous to user-created animals
 */
public class Predator extends Animal {

	private final int TIME_BEFORE_LOOKING_ELSEWHERE = 40;

	private DetectedAnimal target;

	private Random r;

	public Predator(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		r = new Random();
		this.changeImage("predator.png");
		this.improveAttack(Animal.ATTACK_IMPROVING_COST);// Improve once, with just enough points
		this.improveDetectionDistance(getAdnPoints());   // Improve with all that's left
		target = null;
	}

	@Override
	public void behave() {
		if (target != null) {
			this.moveTowards(target.getPosX(), target.getPosY());
			this.attack(target);
			this.eat(target);
		} else if ((getDirX() == 0 && getDirY() == 0) 
				|| 
				this.getSimulator().getTicksElapsed() % TIME_BEFORE_LOOKING_ELSEWHERE == 0) {

			setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
			setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		}
		target = null;
	}

	private boolean isHungry() {
		return this.getFullness() < this.getMaxFullnessValue()*0.75;
	}

	@Override
	public void onAnimalDetected(DetectedAnimal a) {
		super.onAnimalDetected(a);

		if (a.getDetectedAnimalClass().equals(Predator.class) && a.isAlive()) {
			this.mate(a);
		} 
		if (isHungry()) {
			if (a.isDead()) {
				target = a;
			}
			if (a.isAlive() && !a.getDetectedAnimalClass().equals(Predator.class) && target == null) {
				target = a;
			}
		}
	}

	@Override
	protected void chooseInitialDiet() {
		beCarnivore();
	}

	@Override
	protected void onBirth(Animal parent1, Animal parent2) {
		this.improveSpeed(getAdnPoints());   // Improve with all that's left	
	}
}
