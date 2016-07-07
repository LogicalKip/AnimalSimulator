/**
 * 
 */
package model.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import model.Predator;

/** TODO gameplay ideas :
 * rivers : random patterns, not crossable unless payed for the feature, all animals need water to survive (amount can be changed : think of camels). Gives bonus points if can only live in the water ? beware bug of grass growing into/beyond water
 * 
 * being able to eat the further away from the grass the bigger it is
 * 
 * rots after dead for too long. Can be eaten only by animals with the corresponding diet (scavenger, which implies carnivorous as well ?). Disappears completely some time after
 */



/**
 * Main class, controls interactions between most other classes.
 */
public class Simulator {
	List<Animal> allAnimals;
	List<Grass> allGrass;
	List<Animal> newborns;
	Set<Animal> animalsToRemove;
	
	public final int MAP_WIDTH = 1600;
	public final int MAP_HEIGHT = 900;
	
	
	public final int MAX_DISTANCE_TO_EAT = 30;

	public final int STARTING_VEGETATION = (MAP_WIDTH+MAP_HEIGHT)/100;
	public final int STARTING_ANIMALS = STARTING_VEGETATION;
	public final int STARTING_PREDATORS = STARTING_ANIMALS/2;
	
	
	private int ticksElapsed;
	
	public Simulator(Class<?> race) throws IllegalArgumentException {
		ticksElapsed = 0;
		allAnimals = new LinkedList<Animal>();
		newborns = new LinkedList<Animal>();
		animalsToRemove = new HashSet<Animal>();
		allGrass = new LinkedList<Grass>();
		
		Random r = new Random();
		for (int i = 0 ; i < STARTING_ANIMALS ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);

			try {
				Constructor<?> constructor = race.getConstructor(int.class, int.class, Simulator.class);
				Animal animalInstance = (Animal) constructor.newInstance(x, y, this);
				allAnimals.add(animalInstance);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0 ; i < STARTING_PREDATORS ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);
			allAnimals.add(new Predator(x, y, this));
		}
		for (int i = 0 ; i < STARTING_VEGETATION ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);
			allGrass.add(new Grass(x, y));
		}
	}

	public List<Animal> getAllAnimals() {
		return allAnimals;
	}
	
	public void nextTick() {
		callDetectionMethods();
		
		for (Animal a : allAnimals) {
			a.behave();
		}
		for (Animal a : allAnimals) {
			a.move(MAP_WIDTH, MAP_HEIGHT);
			a.gettingHungry();
		}
		
		makeAnimalsAttack();
		
		makeAnimalsEat();
		
		makeAnimalsMate();
		
		for (Animal a : allAnimals) {
			a.nextTick();
		}
		for (Grass g : allGrass) {
			g.nextTick();
		}
		
		allAnimals.addAll(newborns);
		newborns.clear();
		
		for (Animal a : animalsToRemove) {
			if (allAnimals.contains(a)) {
				allAnimals.remove(a);
			} else {
				throw new NoSuchElementException();
			}
		}
		animalsToRemove.clear();
		
		ticksElapsed++;
	}
	
	void addBabyToWorld(Animal baby) {
		newborns.add(baby);
	}
	
	void removeAnimalFromWorld(Animal animal) {
		animalsToRemove.add(animal);
	}
	
	private void callDetectionMethods() {
		for (Animal a : allAnimals) {
			for (Grass g : allGrass) {
				double distanceToFood = euclidianDistance(a.getPosX(), a.getPosY(), g.getPosX(), g.getPosY());
				if (Math.ceil(distanceToFood) < a.getDetectionDistanceValue()) {
					a.onGrassDetected(new DetectedGrass(g));
				}
			}
		}
		for (Animal a1 : allAnimals) {
			for (Animal a2 : allAnimals) {
				double distance = euclidianDistance(a1.getPosX(), a1.getPosY(), a2.getPosX(), a2.getPosY());
				if (!a1.equals(a2) && Math.ceil(distance) < a1.getDetectionDistanceValue()) {
					a1.onAnimalDetected(new DetectedAnimal(a2));
				}
			}
		}
	}
	
	private void makeAnimalsAttack() {
		for (Animal a1 : allAnimals) {
			for (Animal a2 : allAnimals) {
				if (a1.getAnimalToAttackThisTick() != null && a1.getAnimalToAttackThisTick().equals(a2)) {
					a1.attack(a2);
				}
			}
		}
	}
	
	
	private void makeAnimalsMate() {
		for (Animal a1 : allAnimals) {
			for (Animal a2 : allAnimals) {
				if (a1.getAnimalToMateWithThisTick() != null && a1.getAnimalToMateWithThisTick().equals(a2)) {
					a1.mate(a2);
				}
			}
		}
	}
	
	private void makeAnimalsEat() {
		List<Animal> animalsToRemove = new LinkedList<Animal>();
		List<Grass> grassToRemove = new LinkedList<Grass>();
		for (Animal a : allAnimals) {
			if (a.isAlive()) {
				if (a.isHerbivore()) {
					for (Grass f : allGrass) {
						if (a.getGrassesToEatThisTick().contains(f)) {
							double distanceToGrass = euclidianDistance(a.getPosX(), a.getPosY(), f.getPosX(), f.getPosY());
							if (distanceToGrass <= MAX_DISTANCE_TO_EAT) {
								a.eatFrom(f);
								if (f.getAmount() == 0) {
									grassToRemove.add(f);
								}
							}
						}
					}
				} 
				if (a.isCarnivore()) {
					for (Animal a2 : allAnimals) {
						if (! a2.equals(a) && a.getAnimalsToEatThisTick().contains(a2)) {
							double distanceToPrey = euclidianDistance(a.getPosX(), a.getPosY(), a2.getPosX(), a2.getPosY());
							if (distanceToPrey <= MAX_DISTANCE_TO_EAT && a2.isDead()) {
								a.eatFrom(a2);
								animalsToRemove.add(a2);
							}
						}
					}
				}
			}
		}
		
		allAnimals.removeAll(animalsToRemove);
		allGrass.removeAll(grassToRemove);
	}
	
	public static final double euclidianDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(
				Math.pow(x1 - x2, 2)
					+
				Math.pow(y1 - y2, 2)
				);
	}

	public int getTicksElapsed() {
		return ticksElapsed;
	}

	public List<Grass> getAllFoodSources() {
		return allGrass;
	}
}
