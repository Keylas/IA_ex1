import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {

	//The simulation space has two space to handle: grass ("background") and rabbits ("foreground")
	private Object2DGrid grassSpace;
	private Object2DGrid rabbitSpace;
	
	private int amountGrass;


	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		grassSpace = new Object2DGrid(xSize, ySize);
		rabbitSpace = new Object2DGrid(xSize, ySize);

		//Fill grass space with tiles with no grass (so that the display shows "0 grass" and not "null grass"
		for(int i=0; i<xSize; i++) {
			for(int j=0; j<ySize; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	public int getGrassAt(int x, int y) {
		if(grassSpace.getObjectAt(x, y)!=null) { //Just in case, should not happen due to initialization
			return ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			return 0;
		}
	}

	//check if a rabbit is in the given cell
	public boolean isCellOccupied(int x, int y) {
		return (rabbitSpace.getObjectAt(x, y)!=null);
	}


	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit) {
		/* Try adding a rabbit at a random point of the space
		 * (stops after it encounters 10*totalNumberOfCells occupied cells)
		 */

		boolean tryAdd = true;
		//number of tries before giving up and returning false
		int count = 10*rabbitSpace.getSizeX()*rabbitSpace.getSizeY();

		while(tryAdd && count>0) {
			int x = (int)(Math.random()*rabbitSpace.getSizeX());
			int y = (int)(Math.random()*rabbitSpace.getSizeY());
			if(!isCellOccupied(x, y)) { //if cell is free
				//put rabbit and set its position
				rabbitSpace.putObjectAt(x, y, rabbit);
				rabbit.setXY(x, y);
				rabbit.setSimulationSpace(this);
				tryAdd = false;
			}
			count--;
		}
		return !tryAdd; //rabbit was placed -> tryAdd=false -> return true
	}

	//Try to move the rabbit in (x,y) to (newX,newY). Returns true if successful, false if not.
	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		if(isCellOccupied(newX, newY)) {
			return false;
		} else {
			//Get the rabbit
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
			//Put it at destination
			rabbitSpace.putObjectAt(newX, newY, rabbit);
			//Remove it from origin
			removeRabbitAt(x, y);
			//Update its coordinate
			rabbit.setXY(newX, newY);
			return true;
		}
	}

	/*
	 * Add grass at a random place. [There can be multiple stacks of grass on a cell]
	 * grassValue is passed by the simulation. So the Integers on the space represent the energy value of the stacks of grass
	 */
	public void addGrass(int grassValue) {
		int x = (int)(Math.random()*(grassSpace.getSizeX()));
		int y = (int)(Math.random()*(grassSpace.getSizeY()));

		//Add grass to the cell (addition the energy values if there were already some grass)
		grassSpace.putObjectAt(x, y, new Integer(grassValue+((Integer)grassSpace.getObjectAt(x, y)).intValue()));
		
		amountGrass+=grassValue;
	}

	//Remove rabbit by putting null
	public void removeRabbitAt(int x, int y) {
		rabbitSpace.putObjectAt(x, y, null);

	}

	//reset the energy value on the given cell to 0 and return the former value
	public int eatGrassAt(int x, int y) {
		int energy = getGrassAt(x, y);
		amountGrass-=energy;
		grassSpace.putObjectAt(x, y, new Integer(0));
		return energy;
	}
	
	public double getAmountGrass() {
		// TODO Auto-generated method stub
		return amountGrass;
	}


	//Getters
	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}


	public Object2DGrid getCurrentRabbitSpace() {
		return rabbitSpace;
	}


}
