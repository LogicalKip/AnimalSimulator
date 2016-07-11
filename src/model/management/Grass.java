package model.management;

import java.util.Random;

public class Grass implements Cloneable {
	private final Coordinate pos;
	
	private int amount;
	private int age;

	public final static int GROWTH_TIME = 5;
	public final static int MIN_INITIAL_AMOUNT = 500;
	public final static int FULLNESS_AMOUNT_PER_BITE = 8;
	
	
	public Grass(int x, int y) {
		this.pos = new Coordinate(x, y);
		this.age = 0;
		this.amount = MIN_INITIAL_AMOUNT + new Random().nextInt(MIN_INITIAL_AMOUNT/2);
	}
	
	public Grass getClone() {
		try {
			return (Grass) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getPosX() {
		return pos.getX();
	}

	public int getPosY() {
		return pos.getY();
	}

	public int getAmount() {
		return amount;
	}
	
	/**
	 * Takes a bite a the grass.
	 * @return the amount of fullness gained by consuming part of the plant.
	 */
	int beingEaten() {
		int fullnessGain = FULLNESS_AMOUNT_PER_BITE;
		
		if (this.amount - FULLNESS_AMOUNT_PER_BITE < 0) {
			fullnessGain = this.amount;
		}
		this.amount -= fullnessGain;
		
		return fullnessGain;
	}

	void nextTick() {
		this.age++;
		if (this.age % GROWTH_TIME == 0) {
			this.amount++;
		}
	}
	
	public int getMaxDistanceToEat() {
		int res = getWidth();
		if (res < Simulator.MAX_DISTANCE_TO_EAT_PREY) {
			res = Simulator.MAX_DISTANCE_TO_EAT_PREY;
		}
		return res;
	}
	
	public int getWidth() {
		return amount/10;
	}

	Coordinate getPos() {
		return pos;
	}
}
