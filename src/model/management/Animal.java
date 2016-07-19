/**
 * 
 */
package model.management;

import java.awt.geom.Line2D;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Animals, either yours or enemies. Extend to create your animal.
 * Then spend points to improve characteristics, don't forget to choose a diet (herbivorous/carnivorous or both), and implements onFooBarDetected methods to be able to react to the environnement.
 */
public abstract class Animal implements Cloneable {

	private String tilePath;

	private String deadTilePath;

	private Simulator simulator;

	private boolean isDead;

	private Coordinate pos;

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

	private Feature speed;

	private Feature pubertyAge;

	private Feature maxFullness;

	private Feature attack;
	
	private Feature detectionDistance;
	
	private int fullness;

	private int adnPoints;

	private int ticksLeftBeforeNextMating;

	private int age;

	private int generation;
	
	/**
	 * How old the animal was when it died, not about dying of old age
	 */
	private int ageOfDeath;

	private Set<Animal> animalsToEatThisTick;
	
	private Set<Grass> grassesToEatThisTick;

	private Animal animalToAttackThisTick;
	
	private Animal animalToMateWithThisTick;



	final static public int TIME_TO_ROT = 200;

	final static public int MAX_DISTANCE_TO_ATTACK = 30;
	final static public int MAX_DISTANCE_TO_MATE = 50;

	final static public int DEFAULT_FULLNESS_PER_CARNIVOROUS_BITE = 500;
	final static public int DEFAULT_SPEED = 2;
	final static public int DEFAULT_DETECTION_DISTANCE = 130;
	final static public int DEFAULT_MAX_FULLNESS = 500;
	final static public int DEFAULT_TIME_BETWEEN_MATING = 150; // TODO being able to improve mating attributes
	final static public int DEFAULT_PUBERTY_AGE = 20;

	
	/** Evolution-related constants **/
	final static public int ADN_GAIN_TO_NEWBORN = 2;
	final static public int STARTING_ADN_POINTS = 20;
	final static public int SPEED_IMPROVING_COST = 3;
	final static public int DETECTION_DISTANCE_IMPROVING_COST = 1;
	final static public int MAX_FULLNESS_IMPROVING_COST = 1;
	final static public int ATTACK_IMPROVING_COST = 5;
	final static public int HERBIVORE_COST = 10;
	final static public int CARNIVORE_COST = 7;
	final static public int PUBERTY_IMPROVING_COST = 2;

	/**
	 * Constructor for animals already in the game when it starts.
	 * For newborns, see {@link #mate(Animal)} and {@link #onBirth(Animal, Animal)}
	 * Calls {@link #chooseInitialDiet()}
	 * @param x Starting X position
	 * @param y Starting Y position
	 * @param s the simulator in which the animal will start
	 * @throws IllegalArgumentException
	 */
	public Animal(int x, int y, Simulator s) throws IllegalArgumentException {
		if (s == null) {
			throw new IllegalArgumentException("Simulator parameter must not be null");
		}

		this.setDead(false);
		this.setDirX(0);
		this.setDirY(0);
		this.ticksLeftBeforeNextMating = 0;
		this.alreadyAttackedThisTick = false;
		this.attack = new Feature(ATTACK_IMPROVING_COST, 0, this);
		this.detectionDistance = new Feature(DETECTION_DISTANCE_IMPROVING_COST, DEFAULT_DETECTION_DISTANCE, this);
		this.speed = new Feature(SPEED_IMPROVING_COST, DEFAULT_SPEED, this);
		this.maxFullness = new Feature(MAX_FULLNESS_IMPROVING_COST, DEFAULT_MAX_FULLNESS, this);
		this.pubertyAge = new Feature(PUBERTY_IMPROVING_COST, DEFAULT_PUBERTY_AGE, this);
		this.pos = new Coordinate(x, y);
		this.age = this.pubertyAge.getValue();
		this.adnPoints = STARTING_ADN_POINTS;
		this.herbivore = false;
		this.carnivore = false;
		this.simulator = s;
		this.fullness = (new Random().nextInt(this.maxFullness.getValue()/2) + this.maxFullness.getValue()/2);
		this.generation = 1;
		this.ageOfDeath = -1;
		this.animalsToEatThisTick = new HashSet<Animal>();
		this.grassesToEatThisTick = new HashSet<Grass>();

		this.chooseInitialDiet();
	}

	synchronized public final void improveSpeed(final int adnToSpend) {
		this.speed.upgrade(adnToSpend);
	}

	synchronized public final void improveDetectionDistance(final int adnToSpend) {
		this.detectionDistance.upgrade(adnToSpend);
	}

	synchronized public final void improveMaxFullness(final int adnToSpend) {
		this.maxFullness.upgrade(adnToSpend);
	}

	synchronized public final void improveAttack(final int adnToSpend) {
		this.attack.upgrade(adnToSpend);
	}

	synchronized final void attack(Animal a) {
		if (!this.alreadyAttackedThisTick && Simulator.euclidianDistance(a.getPosX(), a.getPosY(), this.getPosX(), this.getPosY()) <= MAX_DISTANCE_TO_ATTACK &&
				this.isAlive() && a.isAlive()) {
			this.alreadyAttackedThisTick = true;
			if (this.attack.getValue() > 0) {
				try {
					a.onAttacked(new DetectedAnimal(this), this.attack.getValue());
					a.die();
				} catch (IllegalStateException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	synchronized public final void attack(DetectedAnimal a) {
		this.animalToAttackThisTick = a.getActualAnimal();
	}


	/**
	 * See {@link Animal#mate(DetectedAnimal)}
	 */
	synchronized final void mate(Animal mate) {
		if (this.isAlive() && mate.isAlive()) {
			if (this.age >= this.pubertyAge.getValue() && mate.age >= mate.pubertyAge.getValue()) {
				if (this.ticksLeftBeforeNextMating == 0 && mate.ticksLeftBeforeNextMating == 0) {
					if (Simulator.euclidianDistance(mate.pos.getX(), mate.pos.getY(), this.pos.getX(), this.pos.getY()) <= MAX_DISTANCE_TO_MATE) {
						if (this.sameSpeciesAs(mate)) {
							this.ticksLeftBeforeNextMating = DEFAULT_TIME_BETWEEN_MATING;
							mate.ticksLeftBeforeNextMating = DEFAULT_TIME_BETWEEN_MATING;

							try {
								Animal baby = (Animal) ((this.getGeneration() > mate.getGeneration()) ? this.clone() : mate.clone()); // The most evolved parent is considered the basis

								baby.pos = new Coordinate(
										this.pos.getX() + mate.pos.getX()/2, 
										this.pos.getY() + mate.pos.getY()/2);//TODO If pregnancy is ever done : when delivering the baby after carrying, should be next to mom and nothing to do with the dad
								baby.age = 0;
								baby.adnPoints = this.getAdnPoints() + mate.getAdnPoints() + ADN_GAIN_TO_NEWBORN;
								baby.fullness = Math.max(this.fullness, mate.fullness);
								baby.generation++;
								baby.speed = baby.speed.getClone(baby);
								baby.pubertyAge = baby.pubertyAge.getClone(baby);
								baby.maxFullness = baby.maxFullness.getClone(baby);
								baby.attack = baby.attack.getClone(baby);
								baby.detectionDistance = baby.detectionDistance.getClone(baby);
								baby.onBirth(this, mate);

								simulator.addBabyToWorld(baby);
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * /!\ The baby will be created using the the Object#clone() method.
	 * Tries to create an offspring, if all requirements, such as being old enough, and not being too soon after a previous mating, are fulfilled.
	 * Some attributes will be copied from the parents.
	 * @param mate the other parent
	 */
	synchronized public final void mate(DetectedAnimal mate) {
		animalToMateWithThisTick = mate.getActualAnimal();
	}

	/**
	 * Called in the baby object once he's born.
	 * Use it to spend the baby's (few) new added adn points and/or giving him some knowledge, possibly using the parents' attributes.
	 */
	protected abstract void onBirth(Animal parent1, Animal parent2);


	/**
	 * @param a the animal compared to
	 * @return true if they are the very same species (not a subclass), therefore mate potential
	 */
	public final boolean sameSpeciesAs(Animal a) {
		return this.getClass().equals(a.getClass());
	}


	/**
	 * Spend points to be herbivorous or carnivorous with {@link Animal#beHerbivore()} and/or {@link Animal#beCarnivore()}
 	 */
	protected abstract void chooseInitialDiet();


	/**
	 * Spends {@link Animal#HERBIVORE_COST} points to allow the animal to eat plants.
	 * Does not exclude the possibility to {@link Animal#beCarnivore()}
	 */
	synchronized public final void beHerbivore() {
		if (this.adnPoints >= HERBIVORE_COST && !this.herbivore) {
			this.herbivore = true;
			this.adnPoints -= HERBIVORE_COST;
		}
	}

	/**
	 * Spends {@link Animal#CARNIVORE_COST} points to allow the animal to eat dead animals
	 * Does not exclude the possibility to {@link Animal#beHerbivore()}
	 */
	synchronized public final void beCarnivore() {
		if (this.adnPoints >= CARNIVORE_COST && !this.carnivore) {
			this.carnivore = true;
			this.adnPoints -= CARNIVORE_COST;
		}
	}

	final void nextTick() {
		this.alreadyAttackedThisTick = false;
		if (this.ticksLeftBeforeNextMating > 0) {
			this.ticksLeftBeforeNextMating--;
		}
		this.age++;
		this.animalsToEatThisTick.clear();
		this.grassesToEatThisTick.clear();
		if (this.isDead() && this.age - this.ageOfDeath >= TIME_TO_ROT) {
			simulator.removeAnimalFromWorld(this);
		}
	}

	/**
	 * Called every tick. Must make the animal move or whatever.
	 */
	public abstract void behave();

	public final int getPosX() {
		return pos.getX();
	}

	synchronized private final void setPosX(int posX) {
		this.pos.setX(posX);
	}

	public final int getPosY() {
		return pos.getY();
	}

	private final void setPosY(int posY) {
		this.pos.setY(posY);
	}

	public final int getSpeedValue() {
		return speed.getValue();
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
				this.die();
			}
		}
	}

	final void eatFrom(Grass f) {
		if (this.isAlive() && this.isHerbivore() && this.fullness < this.maxFullness.getValue()) {
			this.fullness += f.beingEaten();
			if (this.fullness > this.maxFullness.getValue()) {
				this.fullness = this.maxFullness.getValue();
			}	
		}
	}

	final void eatFrom(Animal a) {
		if (this.isAlive() && a.isDead() && this.isCarnivore()) {
			this.fullness += DEFAULT_FULLNESS_PER_CARNIVOROUS_BITE;
			if (this.fullness > this.maxFullness.getValue()) {
				this.fullness = this.maxFullness.getValue();
			}	
		}
	}

	private final void die() throws IllegalStateException {
		if (this.isDead) {
			throw new IllegalStateException("What is dead can't die :/");
		} else {
			this.isDead = true;
			this.ageOfDeath = this.age;
			this.onDeath();
		}
	}
	
	/**
	 * Moves the animal, if alive, according to previously set directions and speed.
	 * Will not go under 0, above given integers or through rivers
	 */
	final void move(final int MAX_X, final int MAX_Y, List<River> rivers) {
		if (this.isAlive()) {
			int newX = getPosX() + (int)Math.ceil(getSpeedValue() * getDirX());
			int newY = getPosY() + (int)Math.ceil(getSpeedValue() * getDirY());

			boolean willCrossRiver = false;
			Line2D animalLine = new Line2D.Float(getPosX(), getPosY(), newX, newY);
			for (River r : rivers) {
				if (willCrossRiver) {
					break;
				}
				Coordinate nodes[] = r.getNodes();
				for (int i = 0 ; i < nodes.length-1 ; i++) {
					Coordinate node1 = nodes[i];
					Coordinate node2 = nodes[i+1];
					Line2D currentRiverLine = new Line2D.Float(node1.getX(), node1.getY(), node2.getX(), node2.getY());
					if (animalLine.intersectsLine(currentRiverLine)) {
						willCrossRiver = true;
						break;
					}
				}
			}
			if (!willCrossRiver && newX >= 0 && newX <= MAX_X) {
				setPosX(newX);
			}
			if (!willCrossRiver && newY >= 0 && newY <= MAX_Y) {
				setPosY(newY);
			}
		}
	}
	
	/**
	 * Tries to eat the animal this tick. Must be called again if necessary  
	 */
	public final void eat(DetectedAnimal a) {
		this.animalsToEatThisTick.add(a.getActualAnimal());
	}
	
	/**
	 * Tries to eat the grass this tick. Must be called again if necessary  
	 */
	public final void eat(DetectedGrass g) {
		this.grassesToEatThisTick.add(g.getActualGrass());
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

	public final int getDetectionDistanceValue() {
		return detectionDistance.getValue();
	}

	/**
	 * Called every tick, for every grass near the animal (near means less than {@link Animal#detectionDistance})
	 * Must be overridden to be used
	 * @param g the grass detected, as it was when detected. This is not the real reference and won't be updated. Think 'snapshot'
	 */
	public void onGrassDetected(DetectedGrass g) {
	}

	/**
	 * Called every tick, for every other animal near the animal (near means less than {@link Animal#detectionDistance})
	 * Must be overridden to be used
	 * @param a the animal detected, as it was when detected. This is not the real reference and won't be updated. Think 'snapshot'
	 */
	public void onAnimalDetected(DetectedAnimal a) {
	}
	
	/**
	 * Called every tick, for every part of rivers near the animal (near means less than {@link Animal#detectionDistance})
	 * The whole river won't be given, but the two extremities of that part of the river are given even if they can't be detected. 
	 * It's possible to map the whole river with a bit of exploring and memory (2 equal coordinates means the same river).
	 * Must be overridden to be used
	 */
	public void onRiverDetected(Coordinate a, Coordinate b) {
	}

	/**
	 * Can be overridden to do one last thing when the animal dies
	 */
	public void onDeath() {
	}
	
	/**
	 * Called when attacked by another animal
	 */
	public void onAttacked(DetectedAnimal attacker, int damage) {
	}

	/**
	 * Utility method. Calls {@link #setDirX(double)} and {@link #setDirY(double)} such that the animal's direction is towards given point, at full speed.
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
	
	/**
	 * Utility method. Calls {@link #setDirX(double)} and {@link #setDirY(double)} such that the animal's direction is away from given point, at full speed.
	 */
	protected final void moveAwayFrom(final int x, final int y) {
		moveTowards(x, y);
		setDirX(-getDirX());
		setDirY(-getDirY());
	}

	public final void changeImage(final String tileFileName) {
		if (new File(tileFileName).exists()) {
			this.tilePath = tileFileName;
		} else {
			System.err.println(tileFileName + " couldn't be loaded. Is it at the root of the app ?");
		}
	}
	
	public final void changeDeadImage(final String tileFileName) {
		if (new File(tileFileName).exists()) {
			this.deadTilePath = tileFileName;
		} else {
			System.err.println(tileFileName + " couldn't be loaded. Is it at the root of the app ?");
		}
	}

	public final String getTilePath() {
		return tilePath;
	}

	public final int getMaxFullnessValue() {
		return maxFullness.getValue();
	}

	public final Simulator getSimulator() {
		return simulator;
	}

	public final int getAdnPoints() {
		return adnPoints;
	}

	public final int getAttackValue() {
		return attack.getValue();
	}

	public final boolean isHerbivore() {
		return herbivore;
	}

	public final boolean isCarnivore() {
		return carnivore;
	}

	public final int getGeneration() {
		return generation;
	}

	public final String getDeadTilePath() {
		return deadTilePath;
	}

	 final void removeAdnPoints(int adnPointsToRemove) {
		if (adnPoints >= adnPointsToRemove) {
			this.adnPoints -= adnPointsToRemove;
		} else {
			throw new IllegalStateException();
		}
	}

	public final Feature getSpeed() {
		return speed;
	}

	public final Feature getPubertyAge() {
		return pubertyAge;
	}

	public final Feature getAttack() {
		return attack;
	}

	public final Feature getMaxFullness() {
		return maxFullness;
	}

	public final Feature getDetectionDistance() {
		return detectionDistance;
	}

	public final Set<Animal> getAnimalsToEatThisTick() {
		return animalsToEatThisTick;
	}

	public final Set<Grass> getGrassesToEatThisTick() {
		return grassesToEatThisTick;
	}

	public final Animal getAnimalToAttackThisTick() {
		return animalToAttackThisTick;
	}

	public Animal getAnimalToMateWithThisTick() {
		return animalToMateWithThisTick;
	}

	int getAgeOfDeath() {
		return ageOfDeath;
	}
}
