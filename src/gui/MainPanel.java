package gui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import model.Predator;
import model.management.Animal;
import model.management.Grass;
import model.management.Simulator;

public class MainPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -8438576029794021570L;

	public static final int DELAY = 100;
	public static final int ANIMAL_SIZE = 10;

	private Timer timer;

	private Simulator simulator;

	public MainPanel(Simulator simul) {
		this.simulator = simul;
		this.timer = new Timer(DELAY, this);
		timer.start();
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D g = (Graphics2D) graphics;

		//DRAW DETECTION DISTANCES
		for (Animal a : this.simulator.getAllAnimals()) {
			if (a.isDead()) continue;

			if (a instanceof Predator) {
				g.setPaint(Color.pink);
			}
			else {
				g.setPaint(Color.cyan);
			}
			
			
			int radius = a.getDetectionDistance();
			g.fillOval(	a.getPosX() - radius, 
					a.getPosY() - radius, 
					radius*2, radius*2);	
		}

		//DRAW GRASS
		g.setPaint(Color.green);
		for (Grass f : this.simulator.getAllFoodSources()) {
			int radius = simulator.MAX_DISTANCE_TO_EAT;
			g.fillOval(	f.getPosX() - radius, 
					f.getPosY() - radius, 
					radius*2, radius*2);	
			g.drawString(Integer.toString(f.getAmount()), f.getPosX(), f.getPosY() - radius);
		}

		//DRAW ANIMALS
		for (Animal a : this.simulator.getAllAnimals()) {
			int animalRepresentationHeight = ANIMAL_SIZE;

			g.setPaint(a.isDead() ? Color.red : Color.blue);
			BufferedImage tile = (a.isDead() ? a.getDeadTile() : a.getTile());
			if (tile == null) {
				g.fillOval(a.getPosX() - ANIMAL_SIZE/2, a.getPosY() - ANIMAL_SIZE/2, 
						ANIMAL_SIZE, ANIMAL_SIZE);
			} else {
				g.drawImage(tile, 
						a.getPosX() - tile.getWidth()/2, 
						a.getPosY() - tile.getHeight()/2, null);
				animalRepresentationHeight = tile.getHeight();
			}

			g.drawString("[" + a.getGeneration() + "] " + Integer.toString(a.getFullness()), a.getPosX(), a.getPosY() - animalRepresentationHeight/2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		simulator.nextTick();
		repaint();
	}

	public Timer getTimer() {
		return timer;
	}
}
