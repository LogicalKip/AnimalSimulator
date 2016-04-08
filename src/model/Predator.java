package model;

import java.util.Random;

import model.management.Animal;
import model.management.Simulator;

/**
 * Animal that is dangerous to user-created animals
 */
public class Predator extends Animal {

	public Predator(int x, int y, Simulator s) throws IllegalArgumentException {
		super(x, y, s);
		this.changeImage("predator.png");
		this.improveAttack(Animal.ATTACK_IMPROVING_COST);// Improve once, with just enough points
		this.improveDetectionDistance(getAdnPoints());   // Improve with all that's left
	}

	@Override
	public void behave() {
		if ((getDirX() == 0 && getDirY() == 0) 
				/*|| 
				this.getSimulator().getTicksElapsed() % 40 == 0*/) {
			Random r = new Random();
			setDirX(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
			setDirY(r.nextBoolean() ? 1 : (r.nextBoolean() ? 0 : -1));
		}
	}

	@Override
	public void onAnimalDetected(Animal a) {
		super.onAnimalDetected(a);
		if (a.isAlive() && !(a instanceof Predator)) {
			this.moveTowards(a.getPosX(), a.getPosY());
			this.attack(a);
		}
	}

	@Override
	protected void chooseInitialDiet() {
		beCarnivore();
	}
}
