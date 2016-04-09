package model;

import java.util.Random;

import model.management.Animal;
import model.management.Simulator;

/**
 * Animal that is dangerous to user-created animals
 */
public class Predator extends Animal {
	
	private final int TIME_BEFORE_LOOKING_ELSEWHERE = 40;

	public Predator(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		this.changeImage("predator.png");
		this.improveAttack(Animal.ATTACK_IMPROVING_COST);// Improve once, with just enough points
		this.improveDetectionDistance(getAdnPoints());   // Improve with all that's left
	}

	@Override
	public void behave() {
		if ((getDirX() == 0 && getDirY() == 0) 
				|| 
				this.getSimulator().getTicksElapsed() % TIME_BEFORE_LOOKING_ELSEWHERE == 0) {
			Random r = new Random();
			setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
			setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		}
	}

	@Override
	public void onAnimalDetected(Animal a) {
		super.onAnimalDetected(a);
		if (a.isAlive()) {
			if (a instanceof Predator) {
				this.mate(a);
			} else {
				this.moveTowards(a.getPosX(), a.getPosY());
				this.attack(a);
			}
		}
	}

	@Override
	protected void chooseInitialDiet() {
		beCarnivore();
	}

	@Override
	protected void onBirth(Animal parent1, Animal parent2) {
		this.improveAttack(Animal.ATTACK_IMPROVING_COST);// Improve once, with just enough points
		this.improveSpeed(getAdnPoints());   // Improve with all that's left	
	}
	
	
}
