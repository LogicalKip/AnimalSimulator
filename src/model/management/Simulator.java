/**
 * 
 */
package model.management;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/** TODO gameplay ideas :
 * rivers : not crossable unless payed for the feature, all animals need water to survive (amount can be changed : think of camels). Gives bonus points if can only live in the water ? beware grass growing into/beyond water (is it a problem ?).
 * 
 * rots after dead for too long. Can be eaten only by animals with the corresponding diet (scavenger, which implies carnivorous as well ?). Disappears completely some time after
 * 
 * spawn of grass after some time ?
 */



/**
 * Main class, controls interactions between most other classes.
 */
public class Simulator {
	private List<Animal> allAnimals;
	private List<Grass> allGrass;
	private List<River> allRivers;
	private List<Animal> newborns;
	private Set<Animal> animalsToRemove;

	public final static int MAX_DISTANCE_TO_EAT_PREY = 30;

	public final int MAP_WIDTH;
	public final int MAP_HEIGHT;

	private int ticksElapsed;

	public Simulator(Class<?> race, boolean predators) throws IllegalArgumentException {
		ticksElapsed = 0;
		allAnimals = new LinkedList<Animal>();
		newborns = new LinkedList<Animal>();
		allRivers = new LinkedList<River>();
		animalsToRemove = new HashSet<Animal>();
		allGrass = new LinkedList<Grass>();

		MapGenerator map = new MapGenerator();
		MAP_HEIGHT = map.MAP_HEIGHT;
		MAP_WIDTH = map.MAP_WIDTH;

		map.generateWorld(allAnimals, allGrass, allRivers, race, predators, this);
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
			a.move(MAP_WIDTH, MAP_HEIGHT, allRivers);
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
		for (Animal a : allAnimals) {
			for (River r : allRivers) {
				Coordinate nodes[] = r.getNodes();
				for (int i = 0 ; i < nodes.length-1 ; i++) {
					Coordinate node1 = nodes[i];
					Coordinate node2 = nodes[i+1];
					if (dist(node1.getX(), node1.getY(), node2.getX(), node2.getY(), a.getPosX(), a.getPosY()) <= a.getDetectionDistanceValue()) {
						a.onRiverDetected(node1, node2);
					}
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
					for (Grass g : allGrass) {
						if (a.getGrassesToEatThisTick().contains(g)) {
							double distanceToGrass = euclidianDistance(a.getPosX(), a.getPosY(), g.getPosX(), g.getPosY());
							if (distanceToGrass <= g.getMaxDistanceToEat()) {
								a.eatFrom(g);
								if (g.getAmount() == 0) {
									grassToRemove.add(g);
								}
							}
						}
					}
				} 
				if (a.isCarnivore()) {
					for (Animal a2 : allAnimals) {
						if (! a2.equals(a) && a.getAnimalsToEatThisTick().contains(a2)) {
							double distanceToPrey = euclidianDistance(a.getPosX(), a.getPosY(), a2.getPosX(), a2.getPosY());
							if (distanceToPrey <= MAX_DISTANCE_TO_EAT_PREY && a2.isDead()) {
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
	
	/**
	 * Returns the distance between a segment line 1-2 and a point 3
	 */
	public static double dist(double x1, double y1, double x2, double y2, double x3, double y3) {
		double px = x2-x1;
		double py = y2-y1;
		
		double something = px*px + py*py;
	    double u =  ((x3 - x1) * px + (y3 - y1) * py) / something;
	    
	    if (u > 1)
	        u = 1;
	    else if (u < 0)
	        u = 0;

	    double x = x1 + u * px;
	    double y = y1 + u * py;
	    double dx = x - x3;
	    double dy = y - y3;
	    
	    return Math.sqrt(dx*dx + dy*dy);
	}

	public int getTicksElapsed() {
		return ticksElapsed;
	}

	public List<Grass> getAllFoodSources() {
		return allGrass;
	}

	public List<River> getAllRivers() {
		return allRivers;
	}
}
