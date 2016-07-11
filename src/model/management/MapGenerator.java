package model.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import model.Predator;

public class MapGenerator {
	public final int MAP_WIDTH = 1600;
	public final int MAP_HEIGHT = 900;

	public final int MAX_NB_RIVERS = (MAP_WIDTH+MAP_HEIGHT)/600;
	public final int MIN_NB_NODE_IN_RIVERS = 3;
	public final int MAX_ADDITIONNAL_NB_NODE_IN_RIVERS = 3;
	
	/**
	 * How much distance there must be at minimum between 2 nodes of a single river.
	 * This creates a minimum square around the node, not a circle.
	 */
	public final int RIVER_MIN_DISTANCE = MAX_NB_RIVERS*12;
	/**
	 * Distance between the edge of the minimum square (see {@link MapGenerator#RIVER_MIN_DISTANCE}) and the outer edge (maximum square)
	 */
	public final int RIVER_MAX_DISTANCE = RIVER_MIN_DISTANCE*4;

	public final int STARTING_VEGETATION = (MAP_WIDTH+MAP_HEIGHT)/100;
	public final int STARTING_ANIMALS = STARTING_VEGETATION;
	public final int STARTING_PREDATORS = STARTING_ANIMALS/2;

	public MapGenerator() {
	}

	public void generateWorld(List<Animal> allAnimals, List<Grass> allGrass, List<River> allRivers, Class<?> race, Simulator simulator) {
		Random r = new Random();
		for (int i = 0 ; i < STARTING_ANIMALS ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);

			try {
				Constructor<?> constructor = race.getConstructor(int.class, int.class, Simulator.class);
				Animal animalInstance = (Animal) constructor.newInstance(x, y, simulator);
				allAnimals.add(animalInstance);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0 ; i < STARTING_PREDATORS ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);
			allAnimals.add(new Predator(x, y, simulator));
		}
		for (int i = 0 ; i < STARTING_VEGETATION ; i++) {
			int x = r.nextInt(MAP_WIDTH);
			int y = r.nextInt(MAP_HEIGHT);
			allGrass.add(new Grass(x, y));
		}

		for (int currRiver = 0 ; currRiver <= r.nextInt(MAX_NB_RIVERS) ; currRiver++) {
			Coordinate riverNodes[];//TODO random rivers : number of rivers, number of nodes, directions
			riverNodes = new Coordinate[MIN_NB_NODE_IN_RIVERS + r.nextInt(MAX_ADDITIONNAL_NB_NODE_IN_RIVERS + 1)];
			

			//Centering river start
			int previousX = MAP_WIDTH/4 + r.nextInt(MAP_WIDTH/2);
			int previousY = MAP_HEIGHT/4 + r.nextInt(MAP_HEIGHT/2);
			riverNodes[0] = new Coordinate(previousX, previousY);
			for (int i = 1 ; i < riverNodes.length ; i++) {
				int xOffset = RIVER_MIN_DISTANCE + r.nextInt(RIVER_MAX_DISTANCE);
				int yOffset = RIVER_MIN_DISTANCE + r.nextInt(RIVER_MAX_DISTANCE);
				int currentX = r.nextBoolean() ? previousX + xOffset : previousX - xOffset;
				int currentY = r.nextBoolean() ? previousY + yOffset : previousY - yOffset;
				
				riverNodes[i] = new Coordinate(currentX, currentY);
				
				previousX = currentX;
				previousY = currentY;
			}
			allRivers.add(new River(riverNodes));
		}
	}
}
