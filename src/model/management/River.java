package model.management;

public class River {
	private Coordinate nodes[];

	public River(Coordinate nodes[]) {
		super();
		this.nodes = nodes;
	}

	public Coordinate[] getNodes() {
		return nodes;
	}
}
