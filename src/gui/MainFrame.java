package gui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import model.management.Simulator;

//TODO all JavaFX if possible ?

public class MainFrame extends JFrame {
	private static final long serialVersionUID = -8026416994513756565L;

	final int DEFAULT_WIDTH = 300;
	final int DEFAULT_HEIGTH = DEFAULT_WIDTH;

	public MainFrame(Simulator simulator) {
		setTitle("Survival simulator");
		int height = simulator.MAP_HEIGHT + MainPanel.ANIMAL_SIZE; 
		int width = simulator.MAP_WIDTH + MainPanel.ANIMAL_SIZE; 
		setSize(width, height + 30);//FIXME +30 is not clean or obvious (is here because of main "menu bar")
		setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setUpComponents(simulator);
		setVisible(true);
	}

	private void setUpComponents(Simulator simulator) {
		this.add(new MainPanel(simulator));		
	}
}
