package gui;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.GuineaPig;
import model.Predator;
import model.management.Animal;
import model.management.Coordinate;
import model.management.Grass;
import model.management.River;
import model.management.Simulator;

/** TODO GUI-based ideas :
 * can speed up/slow down/pause the game
 * 
 * restart with several .class, with or without CPU predators
 * 
 * bigger maps (according to resolution ?)
 * 
 * better graphics(rivers)/error message boxes
 * 
 * configurable numbers of starting entities
 * 
 * animated gifs with random starting frame
 * 
 * allow packaged classes to be loaded
 * 
 * reduce tile size if too big
 * 
 * restart button with the same animals/settings, different random
 */


/**
 * The main GUI class
 */
public class Main extends Application {

	private Simulator simulator;

	private GraphicsContext canvasGraphics;

	private boolean drawDetectionDistance = true;

	private AnimationTimer timer;

	private Stage stage;

	Rectangle rect = new Rectangle(100, 100, 500, 200);

	public static final int MS_DELAY = 30;
	public static final int ANIMAL_SIZE = 10;
	public static final int LETTER_WIDTH = 3;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage s) {
		this.simulator = new Simulator(GuineaPig.class); 
		this.stage = s;

		setup();

		int x = 180;
		rect.setArcWidth(x);
		rect.setArcHeight(x);
		rect.setFill(Color.DARKKHAKI);
		RotateTransition trans = new RotateTransition(Duration.millis(3000), rect);
		trans.setToAngle(45);
		trans.play();

		stage.show();
	}

	private void setup() {
		initComponents();
		
		timer = new AnimationTimer() {
			private long lastUpdate = 0;
			public void handle(long now) {
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
		stage.setScene(theScene);

		VBox vbox = new VBox(5);

		Canvas canvas = new Canvas(simulator.MAP_WIDTH, simulator.MAP_HEIGHT);

		Button button = new Button("Change animal AI");
		final Stage s = stage;
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
	}

	private void onRestartButtonClick(Stage s) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select the .class of your (not packaged) animal class");
		File file = chooser.showOpenDialog(s);
		if (file != null) {
			String[] splits = file.getName().split("\\.");
			if (splits.length == 2 && splits[1].equals("class") && !splits[0].equals("")) {
				String className = splits[0];
				try {
					Class<?> race = Class.forName(className);
					Constructor<?> constructor = race.getConstructor(int.class, int.class, Simulator.class);
					Object instance = constructor.newInstance(10, 20, simulator);

					if (instance instanceof Animal) {
						this.simulator = new Simulator(race);
					} else {
						errorMsg("It should extend Animal !");
					}
				} catch (ClassNotFoundException e) {
					errorMsg("Error while loading class. Are you sure it was not in any package (i.e must be in default package) when it was compiled ? allmyAIs.BestAI.class would not work.");
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					errorMsg("Class should have a public constructor with the same signature as Animal, and it should call super() with those parameters");
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} else {
				errorMsg("Pick a file like my_own_subclass_of_Animal.class. Make sure it wasn't in a package when it was compiled.");
			}
		}
	}
	
	
	private void errorMsg(String msg) {
		final Popup popup = new Popup();
		popup.getContent().add(new Text());
		Button okButton = new Button("Ok");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				popup.hide();
			}
		});
		popup.getContent().add(new Text(msg));
		popup.getContent().add(okButton);
		popup.show(stage);
	}

	private void repaint() {
		canvasGraphics.clearRect(0, 0, simulator.MAP_WIDTH, simulator.MAP_HEIGHT);

		//DRAW DETECTION DISTANCE
		if (drawDetectionDistance) {
			canvasGraphics.setGlobalAlpha(0.3);
			for (Animal a : simulator.getAllAnimals()) {
				canvasGraphics.setFill(a instanceof Predator ? Color.DARKORCHID : Color.CYAN);
				if (a.isAlive()) {
					final int radius = a.getDetectionDistanceValue();
					canvasGraphics.fillOval(a.getPosX() - radius, 
							a.getPosY() - radius, 
							radius*2, radius*2);
				}
			}
			canvasGraphics.setGlobalAlpha(1);
		}

		//DRAW GRASS
		canvasGraphics.setFill(Color.GREEN);
		for (Grass g : simulator.getAllFoodSources()) {
			int grassRadius = g.getWidth();
			canvasGraphics.fillOval(g.getPosX() - grassRadius, g.getPosY() - grassRadius, grassRadius*2, grassRadius*2);
			String info = Integer.toString(g.getAmount());
			canvasGraphics.fillText(info, g.getPosX() - (LETTER_WIDTH * info.length()), g.getPosY() - grassRadius);
		}
		
		//DRAW RIVERS
		for (River r : simulator.getAllRivers()) {
			Coordinate nodes[] = r.getNodes();
			for (int i = 0 ; i < nodes.length-1 ; i++) {
				Coordinate node1 = nodes[i];
				Coordinate node2 = nodes[i+1];
				canvasGraphics.setStroke(Color.BLUE);
				canvasGraphics.strokeLine(node1.getX(), node1.getY(), node2.getX(), node2.getY());
			}
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
			String info = "[" + a.getGeneration() + "] " + Integer.toString(a.getFullness());
			canvasGraphics.fillText(info, a.getPosX() - (LETTER_WIDTH * info.length()), a.getPosY() - animalRepresentationSize/2);
		}
		
	}

	public boolean isDrawDetectionDistance() {
		return drawDetectionDistance;
	}

	public void setDrawDetectionDistance(boolean drawDetectionDistance) {
		this.drawDetectionDistance = drawDetectionDistance;
	}
}