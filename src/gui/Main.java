package gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Predator;
import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class Main extends Application {

	private Simulator simulator;

	private GraphicsContext gc;

	private boolean drawDetectionDistance = true;

	public static final int MS_DELAY = 50;
	public static final int ANIMAL_SIZE = 10;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage theStage) {
		this.simulator = new Simulator();

		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

		Canvas canvas = new Canvas(simulator.MAP_WIDTH, simulator.MAP_HEIGHT);
		root.getChildren().add(canvas);

		gc = canvas.getGraphicsContext2D();

		new AnimationTimer()
		{
			private long lastUpdate = 0;
			public void handle(long now)
			{
				if (now - lastUpdate >= MS_DELAY*1_000_000) {
					lastUpdate = now;

					simulator.nextTick();
					repaint();					
				}
			}
		}.start();

		theStage.show();
	}

	private void repaint() {
		gc.clearRect(0, 0, simulator.MAP_WIDTH, simulator.MAP_HEIGHT);

		//DRAW DETECTION DISTANCE
		if (drawDetectionDistance) {
			for (Animal a : simulator.getAllAnimals()) {
				gc.setFill(a instanceof Predator ? Color.PINK : Color.CYAN);
				if (a.isAlive()) {
					final int radius = a.getDetectionDistance();
					gc.fillOval(a.getPosX() - radius, 
							a.getPosY() - radius, 
							radius*2, radius*2);
				}
			}
		}

		//DRAW GRASS
		for (Grass g : simulator.getAllFoodSources()) {
			gc.setFill(Color.GREEN);
			int grassRadius = simulator.MAX_DISTANCE_TO_EAT;
			gc.fillOval(g.getPosX() - grassRadius, g.getPosY() - grassRadius, grassRadius*2, grassRadius*2);
			gc.fillText(Integer.toString(g.getAmount()),g.getPosX(), g.getPosY() - grassRadius);
		}

		//DRAW ANIMALS
		for (Animal a : simulator.getAllAnimals()) {
			gc.setFill(a.isDead() ? Color.RED : Color.BLUE);
			Image img = new Image("file:" + (a.isDead() ? a.getDeadTilePath() : a.getTilePath()));
			final int animalRepresentationSize;

			if (img.isError()) {
				animalRepresentationSize = ANIMAL_SIZE;
				gc.fillOval(
						a.getPosX()-animalRepresentationSize/2, 
						a.getPosY()-animalRepresentationSize/2, animalRepresentationSize, animalRepresentationSize);
			} else {
				animalRepresentationSize = (int) Math.ceil(img.getHeight());
				gc.drawImage(img, 
						a.getPosX()-img.getWidth()/2, 
						a.getPosY()-img.getHeight()/2);
			}
			gc.fillText("[" + a.getGeneration() + "] " + Integer.toString(a.getFullness()), a.getPosX(), a.getPosY() - animalRepresentationSize/2);
		}
	}


}