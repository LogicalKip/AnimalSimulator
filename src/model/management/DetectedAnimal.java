package model.management;

/**
 * The attributes that can be seen of an animal when detected.
 * Ex : You may see where it is and what species it's from, but not if it's hungry or herbivore.
 * Think "snapshot".
 * May also contain false information if TODO the animal is faking something (ex : death)
 */
public final class DetectedAnimal {
	private Class<?> clazz;
	
	private final int posX;
	private final int posY;
	
	private final boolean dead;	
	
	private final int ageOfDeath;
	
	private final Animal actualAnimal;
	
	DetectedAnimal(Animal actualReference) {
		super();
		this.actualAnimal = actualReference;
		this.clazz = actualReference.getClass();
		this.posX = actualReference.getPosX();
		this.posY = actualReference.getPosY();
		this.dead = actualReference.isDead();
		this.ageOfDeath = actualReference.getAgeOfDeath();
	}

	public String getClassName() {
		return clazz.getName();
	}
	
	public Class<?> getDetectedAnimalClass() {
		return clazz;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public boolean isDead() {
		return dead;
	}
	
	public boolean isAlive() {
		return !isDead();
	}
	
	

	Animal getActualAnimal() {
		return actualAnimal;
	}

	int getAgeOfDeath() {
		return ageOfDeath;
	}
}
