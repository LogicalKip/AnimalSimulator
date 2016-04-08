/**
 * 
 */
package model.management;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import model.Boui;
import model.Predator;

/**
 * Main class, controls interactions between most other classes.
 */
public class Simulator {
	List<Animal> allAnimals;
	List<Grass> allGrass;
	
	public final int MAP_SIZE = 700;
	
	public final int MAX_DISTANCE_TO_EAT = 30;
	

	public final int STARTING_FOOD_SOURCES = MAP_SIZE/50;
	public final int STARTING_ANIMALS = STARTING_FOOD_SOURCES;
	public final int STARTING_PREDATORS = STARTING_ANIMALS/3;
	
	
	private int ticksElapsed;
	
	public Simulator() throws IllegalArgumentException {
		ticksElapsed = 0;
		allAnimals = new LinkedList<Animal>();
		allGrass = new LinkedList<Grass>();
		
		Random r = new Random();
		for (int i = 0 ; i < STARTING_ANIMALS ; i++) {
			int x = r.nextInt(MAP_SIZE);
			int y = r.nextInt(MAP_SIZE);
			allAnimals.add(new Boui(x, y, this));
		}
		for (int i = 0 ; i < STARTING_PREDATORS ; i++) {
			int x = r.nextInt(MAP_SIZE);
			int y = r.nextInt(MAP_SIZE);
			allAnimals.add(new Predator(x, y, this));
		}
		
		
		for (int i = 0 ; i < STARTING_FOOD_SOURCES ; i++) {
			int x = r.nextInt(MAP_SIZE);
			int y = r.nextInt(MAP_SIZE);
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
			a.move(MAP_SIZE, MAP_SIZE);
			a.gettingHungry();
		}
		

		makeAnimalsEat();
		
		for (Animal a : allAnimals) {
			a.nextTick();
		}
		
		ticksElapsed++;
	}
	
	private void callDetectionMethods() {
		for (Animal a : allAnimals) {
			for (Grass f : allGrass) {
				double distanceToFood = euclidianDistance(a.getPosX(), a.getPosY(), f.getPosX(), f.getPosY());
				if (Math.ceil(distanceToFood) < a.getDetectionDistance()) {
					a.onFoodDetected(f);
				}
			}
		}
		for (Animal a1 : allAnimals) {
			for (Animal a2 : allAnimals) {
				double distance = euclidianDistance(a1.getPosX(), a1.getPosY(), a2.getPosX(), a2.getPosY());
				if (!a1.equals(a2) && Math.ceil(distance) < a1.getDetectionDistance()) {
					a1.onAnimalDetected(a2);
				}
			}
		}
	}
	
	private void makeAnimalsEat() {
		List<Animal> animalsToRemove = new LinkedList<Animal>();
		for (Animal a : allAnimals) {
			if (a.isHerbivore()) {
				for (Grass f : allGrass) {
					double distanceToGrass = euclidianDistance(a.getPosX(), a.getPosY(), f.getPosX(), f.getPosY());
					if (distanceToGrass <= MAX_DISTANCE_TO_EAT) {
						a.eatFrom(f);
					}
				}
			} 
			if (a.isCarnivore()) {
				for (Animal a2 : allAnimals) {
					if (! a2.equals(a)) {
						double distanceToPrey = euclidianDistance(a.getPosX(), a.getPosY(), a2.getPosX(), a2.getPosY());
						if (distanceToPrey <= MAX_DISTANCE_TO_EAT && a2.isDead()) {
							a.eatFrom(a2);
							animalsToRemove.add(a2);
						}
					}
				}
			}
		}
		
		allAnimals.removeAll(animalsToRemove);
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
