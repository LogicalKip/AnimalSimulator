/**
 * 
 */
package model.management;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;


//TODO spend 10 one time (if it costs 10) will improve once. Spend 1 ten times will improve nothing. Should remember "wasted" points and pile up later.

/**
 * Animals, either yours or enemies. Extend to create your animal.
 * Then spend points to improve characteristics, don't forget to choose a diet (herbivorous/carnivorous or both), and implements onFooBarDetected methods to be able to react to the environnement.
 */
public abstract class Animal {
	
	private BufferedImage tile;
	
	private Simulator simulator;

	private boolean isDead;

	private int detectionDistance;

	private int posX;
	private int posY;
	
	private boolean alreadyAttackedThisTick;
	
	private boolean herbivore;
	
	private boolean carnivore;

	/**
	 * Value between -1 and 1 to indicate the direction to go (0 = stop).
	 * Total distance traveled is function of {@link Animal#speed}
	 */
	private double dirX;

	/**
	 * See {@link Animal#dirX}
	 */
	private double dirY;

	private int speed;

	private int fullness;
	
	private int maxFullness;
	
	private int adnPoints;
	
	private int attack;

	public static final int MAX_DISTANCE_TO_ATTACK = 30;

	final static public int DEFAULT_FULLNESS_PER_BITE = 500;
	final static public int DEFAULT_SPEED = 3;
	final static public int DEFAULT_DETECTION_DISTANCE = 200;
	final static public int DEFAULT_MAX_FULLNESS = 200;
	
	final static public int STARTING_ADN_POINTS = 20;
	final static public int SPEED_IMPROVING_COST = 3;
	final static public int DETECTION_DISTANCE_IMPROVING_COST = 2;
	final static public int MAX_FULLNESS_IMPROVING_COST = 1;
	final static public int ATTACK_IMPROVING_COST = 2;
	final static public int HERBIVORE_COST = 10;
	final static public int CARNIVORE_COST = 7;
	

	public Animal(int x, int y, Simulator s) throws IllegalArgumentException {
		if (s == null) {
			throw new IllegalArgumentException("Simulator parameter must not be null");
		}
		
		this.setDead(false);
		this.setPosX(x);
		this.setPosY(y);
		this.setDirX(0);
		this.setDirY(0);
		this.attack = 0;
		this.adnPoints = STARTING_ADN_POINTS;
		this.maxFullness = DEFAULT_MAX_FULLNESS;
		this.detectionDistance = DEFAULT_DETECTION_DISTANCE;
		this.speed = DEFAULT_SPEED;
		this.herbivore = false;
		this.carnivore = false;
		this.simulator = s;
		this.alreadyAttackedThisTick = false;
		this.fullness = (new Random().nextInt(this.maxFullness/2) + this.maxFullness/2);
		
		this.chooseInitialDiet();
	}
	
	synchronized public final void improveSpeed(final int adnToSpend) {
		this.speed += adnImprovement(SPEED_IMPROVING_COST, adnToSpend);
	}
	
	synchronized public final void improveDetectionDistance(final int adnToSpend) {
		this.detectionDistance += adnImprovement(DETECTION_DISTANCE_IMPROVING_COST, adnToSpend);
	}
	
	synchronized public final void improveMaxFullness(final int adnToSpend) {
		this.maxFullness += adnImprovement(MAX_FULLNESS_IMPROVING_COST, adnToSpend);
	}
	
	synchronized public final void improveAttack(final int adnToSpend) {
		this.attack += adnImprovement(ATTACK_IMPROVING_COST, adnToSpend);
	}

	/**
	 * Used as a factorization for all improvement methods
	 * @param adnToSpend How much is wished to be spent on this improvement
	 * @return How many points must be added to the attribute improved
	 */
	private int adnImprovement(final int costPerImprovement, final int adnToSpend) {
		if (adnToSpend > 0 && adnToSpend <= this.adnPoints) {
			final int nbTimesImproved = adnToSpend/costPerImprovement;
			this.adnPoints -= nbTimesImproved * costPerImprovement;
			return nbTimesImproved;
		}
		return 0;
	}
	
	synchronized public final void attack(Animal a) {
		if (!this.alreadyAttackedThisTick && Simulator.euclidianDistance(a.getPosX(), a.getPosY(), this.getPosX(), this.getPosY()) <= MAX_DISTANCE_TO_ATTACK) {
			this.alreadyAttackedThisTick = true;
			if (this.attack > 0) {
				try {
					a.die();
				} catch (IllegalStateException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	
	/**
	 * Spend points to be herbivorous or carnivorous.
	 */
	protected abstract void chooseInitialDiet();
	
	synchronized public final void beHerbivore() {
		if (this.adnPoints >= HERBIVORE_COST && !this.herbivore) {
			this.herbivore = true;
			this.adnPoints -= HERBIVORE_COST;
		}
	}
	
	synchronized public final void beCarnivore() {
		if (this.adnPoints >= CARNIVORE_COST && !this.carnivore) {
			this.carnivore = true;
			this.adnPoints -= CARNIVORE_COST;
		}
	}
	
	final void nextTick() {
		this.alreadyAttackedThisTick = false;
	}

	/**
	 * Called every tick. Must make the animal move or whatever.
	 */
	public abstract void behave();

	public final int getPosX() {
		return posX;
	}

	synchronized private final void setPosX(int posX) {
		this.posX = posX;
	}

	public final int getPosY() {
		return posY;
	}

	private final void setPosY(int posY) {
		this.posY = posY;
	}

	public final int getSpeed() {
		return speed;
	}

	public final double getDirX() {
		return dirX;
	}

	public final void setDirX(double dirX) {
		if (dirX >= -1. && dirX <= 1.) {
			this.dirX = dirX;			
		} else {
			throw new IllegalArgumentException(dirX + " is not normalized");
		}
	}

	public final double getDirY() {
		return dirY;
	}

	public final void setDirY(double dirY) {
		if (dirY >= -1 && dirY <= 1) {
			this.dirY = dirY;			
		} else {
			throw new IllegalArgumentException(dirY + " is not normalized");
		}
	}

	public final int getFullness() {
		return fullness;
	}

	final void gettingHungry() {
		if (this.isAlive()) {
			if (this.fullness > 0) {
				this.fullness--;
			} else {
				this.setDead(true);
			}
		}
	}

	final void eatFrom(Grass f) {
		if (this.isAlive() && this.isHerbivore()) {
			this.fullness += DEFAULT_FULLNESS_PER_BITE;
			if (this.fullness > this.maxFullness) {
				this.fullness = this.maxFullness;
			}	
		}
	}
	
	final void eatFrom(Animal a) {
		if (this.isAlive() && a.isDead() && this.isCarnivore()) {
			this.fullness += DEFAULT_FULLNESS_PER_BITE;
			if (this.fullness > this.maxFullness) {
				this.fullness = this.maxFullness;
			}	
		}
	}

	private final void die() throws IllegalStateException {
		if (this.isDead) {
			throw new IllegalStateException("What is dead can't die :/");
		} else {
			this.isDead = true;
			this.onDeath();
		}
	}

	/**
	 * Moves the animal, if alive, according to previously set directions and speed.
	 * Will not go under 0 or above given integers
	 */
	final void move(final int MAX_X, final int MAX_Y) {
		if (this.isAlive()) {
			int newX = getPosX() + (int)Math.ceil(getSpeed() * getDirX());
			int newY = getPosY() + (int)Math.ceil(getSpeed() * getDirY());

			if (newX >= 0 && newX <= MAX_X) {
				setPosX(newX);
			}
			if (newY >= 0 && newY <= MAX_Y) {
				setPosY(newY);
			}
		}
	}

	public final boolean isDead() {
		return isDead;
	}

	public final boolean isAlive() {
		return !this.isDead();
	}

	private final void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public final int getDetectionDistance() {
		return detectionDistance;
	}

	/**
	 * Called every tick, for every food near the animal (near means less than {@link Animal#detectionDistance})
	 * Must be overridden to be used
	 * @param f
	 */
	public void onFoodDetected(Grass f) {
	}

	/**
	 * Called every tick, for every other animal near the animal (near means less than {@link Animal#detectionDistance})
	 * Must be overridden to be used
	 * @param a
	 */
	public void onAnimalDetected(Animal a) {
	}
	
	/**
	 * Can be overridden to do one last thing when the animal dies
	 */
	public void onDeath() {
		
	}

	/**
	 * Utility method. Calls {@link #setDirX(double)} and {@link #setDirY(double)} such that the animal's direction is towards given point.
	 * @param x
	 * @param y
	 */
	protected final void moveTowards(final int x, final int y) {
		double distXToDest = x - this.getPosX();
		double distYToDest = y - this.getPosY();
		double xAbs = Math.abs(distXToDest);
		double yAbs = Math.abs(distYToDest);
		double signX = Math.signum(distXToDest);
		double signY = Math.signum(distYToDest);

		double resX, resY;

		if (yAbs < xAbs) {
			resX = signX;
			resY = (distXToDest == 0) ? 0 :
				(yAbs / xAbs) * signY;
		} else {
			resX = (distYToDest == 0) ? 0 :
				(xAbs / yAbs) * signX;
			resY = signY;
		}

		setDirX(resX);
		setDirY(resY);
	}

	public final void changeImage(String tileFileName) {
		try {
			this.tile = ImageIO.read(new File(tileFileName));
		} catch (IOException e) {
			System.err.println(tileFileName + " couldn't be loaded. Is it at the root of the app ?");
		}
	}

	public final BufferedImage getTile() {
		return tile;
	}

	public final int getMaxFullness() {
		return maxFullness;
	}

	public final Simulator getSimulator() {
		return simulator;
	}

	public final int getAdnPoints() {
		return adnPoints;
	}

	public final int getAttack() {
		return attack;
	}

	public boolean isHerbivore() {
		return herbivore;
	}

	public boolean isCarnivore() {
		return carnivore;
	}
}
