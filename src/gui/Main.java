package gui;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Boui;
import model.Predator;
import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class Main extends Application {

	private Simulator simulator;

	private GraphicsContext canvasGraphics;

	private boolean drawDetectionDistance = true;

	private AnimationTimer timer;

	private Stage theStage;

	public static final int MS_DELAY = 50;
	public static final int ANIMAL_SIZE = 10;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.simulator = new Simulator(Boui.class); 
		this.theStage = stage;

		setup();

		theStage.show();
	}

	private void setup() {
		initComponents();
		
		timer = new AnimationTimer()
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
		};
		timer.start();
	}

	private void initComponents() {
		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

		VBox vbox = new VBox(5);

		Canvas canvas = new Canvas(simulator.MAP_WIDTH, simulator.MAP_HEIGHT);

		Button button = new Button("Change animal AI");
		final Stage s = theStage;
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				onRestartButtonClick(s);
			}
		});
		vbox.getChildren().add(button);
		vbox.getChildren().add(canvas);

		root.getChildren().add(vbox);

		canvasGraphics = canvas.getGraphicsContext2D();
		
		theStage.centerOnScreen();
	}

	private void onRestartButtonClick(Stage s) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select the .class of your not packaged animal class");
		File file = chooser.showOpenDialog(s);
		if (file != null) {
			String[] splits = file.getName().split("\\.");
			if (splits.length == 2 && splits[1].equals("class")) {
				String className = splits[0];
				try {
					Class<?> race = Class.forName(className);
					Constructor<?> constructor = race.getConstructor(int.class, int.class, Simulator.class);
					Object instance = constructor.newInstance(10, 20, simulator);

					if (instance instanceof Animal) {
						this.simulator = new Simulator(race);
					} else {
						System.err.println("It should extend Animal !");//TODO error msg in GUI instead of console
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} else {
				//TODO error msg in GUI instead of console
				System.err.println("Pick a file like my_own_subclass_of_Animal.class. Make sure it wasn't in a package when it was compiled.");
			}
		}
	}

	private void repaint() {
		canvasGraphics.clearRect(0, 0, simulator.MAP_WIDTH, simulator.MAP_HEIGHT);

		//DRAW DETECTION DISTANCE
		if (drawDetectionDistance) {
			for (Animal a : simulator.getAllAnimals()) {
				canvasGraphics.setFill(a instanceof Predator ? Color.PINK : Color.CYAN);
				if (a.isAlive()) {
					final int radius = a.getDetectionDistance();
					canvasGraphics.fillOval(a.getPosX() - radius, 
							a.getPosY() - radius, 
							radius*2, radius*2);
				}
			}
		}

		//DRAW GRASS
		for (Grass g : simulator.getAllFoodSources()) {
			canvasGraphics.setFill(Color.GREEN);
			int grassRadius = simulator.MAX_DISTANCE_TO_EAT;
			canvasGraphics.fillOval(g.getPosX() - grassRadius, g.getPosY() - grassRadius, grassRadius*2, grassRadius*2);
			canvasGraphics.fillText(Integer.toString(g.getAmount()),g.getPosX(), g.getPosY() - grassRadius);
		}

		//DRAW ANIMALS
		for (Animal a : simulator.getAllAnimals()) {
			canvasGraphics.setFill(a.isDead() ? Color.RED : Color.BLUE);
			Image img = new Image("file:" + (a.isDead() ? a.getDeadTilePath() : a.getTilePath()));
			final int animalRepresentationSize;

			if (img.isError()) {
				animalRepresentationSize = ANIMAL_SIZE;
				canvasGraphics.fillOval(
						a.getPosX()-animalRepresentationSize/2, 
						a.getPosY()-animalRepresentationSize/2, animalRepresentationSize, animalRepresentationSize);
			} else {
				animalRepresentationSize = (int) Math.ceil(img.getHeight());
				canvasGraphics.drawImage(img, 
						a.getPosX()-img.getWidth()/2, 
						a.getPosY()-img.getHeight()/2);
			}
			canvasGraphics.fillText("[" + a.getGeneration() + "] " + Integer.toString(a.getFullness()), a.getPosX(), a.getPosY() - animalRepresentationSize/2);
		}
	}
}