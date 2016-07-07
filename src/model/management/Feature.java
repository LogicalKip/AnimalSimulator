package model.management;

/**
 * Every animal has several features, such as speed or max fullness.
 * Every one has its own speed feature, same for other features.
 */
public class Feature implements Cloneable {
	public final int UPGRADING_COST;
	
	private int value;
	
	/**
	 * If 12 points were spent where 10 where needed, 2 will remain here for next time
	 */
	private int leftoverPoints;
	
	private Animal animal;

	Feature(int UPGRADING_COST, int startingValue, Animal animal) {
		this.UPGRADING_COST = UPGRADING_COST;
		this.value = startingValue;
		this.animal = animal;
		this.leftoverPoints = 0;
	}
	
	public void upgrade(int adnToSpend) {
		final int nbTimesImproved;
		if (adnToSpend > 0 && adnToSpend <= this.animal.getAdnPoints()) {
			nbTimesImproved = (adnToSpend+leftoverPoints)/UPGRADING_COST;
			leftoverPoints =  (adnToSpend+leftoverPoints)%UPGRADING_COST;
			this.animal.removeAdnPoints(adnToSpend);
			this.value += nbTimesImproved;
		}
	}
	
	/**
	 * Used to deep-copy for newborns
	 */
	Feature getClone(Animal newAnimalRef) {
		Feature clone = new Feature(UPGRADING_COST, value, newAnimalRef);
		clone.leftoverPoints = leftoverPoints;
		return clone;
	}

	public int getValue() {
		return value;
	}
}
