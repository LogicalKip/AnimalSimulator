package gui;

import javax.swing.SwingUtilities;

import model.management.Simulator;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
					new MainFrame(new Simulator());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
		    }
		});
	}
}
