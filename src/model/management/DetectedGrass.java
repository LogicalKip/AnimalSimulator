package model.management;


/**
 * The attributes that can be seen of a {@link Grass} when detected.
 * Ex : You may see where it is and how much there's left
 * Think "snapshot".
 * May also contain hidden or false information. Ex : TODO some grass is poisonous, which sickens the animals and also makes them unable to recognize it as poisonous
 */
public class DetectedGrass {
	private final int posX;
	private final int posY;
	
	private Grass actualGrass;
	
	private final int amount;
	
	public DetectedGrass(Grass g) {
		this(g.getPosX(), g.getPosY(), g.getAmount(), g);
	}
	
	private DetectedGrass(int posX, int posY, int amount, Grass g) {
		super();
		this.posX = posX;
		this.posY = posY;
		this.amount = amount;
		this.actualGrass = g;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getAmount() {
		return amount;
	}
	
	public int getMaxDistanceToEat() {
		return actualGrass.getMaxDistanceToEat();
	}
	
	Grass getActualGrass() {
		return actualGrass;
	}
}
