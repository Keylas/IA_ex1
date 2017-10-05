import java.awt.Color;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	//Definition of directions used, to facilitate movement of agents
	private final static int[] NORTH={0,1}, SOUTH={0,-1}, EAST={1,0}, WEST={-1,0};
	private final static int[][] DIRECTIONS=new int[][]{NORTH, SOUTH, EAST, WEST};

	//static number used to assign a unique ID to every rabbit
	private static int IDNumber = 0;

	//Personal variables of agent
	@SuppressWarnings("unused")
	private int ID;
	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace simulationSpace;

	//Constructor. Positioning will be handled by the space [rabbitSpace.addRabbitAt(new Rabbit...()) *will setXY()*]
	public RabbitsGrassSimulationAgent(int energy) {
		this.energy=energy;
		IDNumber++;
		ID = IDNumber;
	}

	//Method required to tell the Object2DDisplay how to render rabbits
	public void draw(SimGraphics arg0) {
		arg0.drawFastCircle(Color.white);
	}

	//How a rabbit must behave at each step
	public void step() {

		//Eat grass on its cell
		energy+=simulationSpace.eatGrassAt(x,y);

		//Choose a random direction to move
		int[] direction = DIRECTIONS[(int)Math.floor(Math.random()*4)];

		//Determine target coordinates (must get the size of the grid to do so)
		Object2DGrid grid = simulationSpace.getCurrentRabbitSpace();
		int newX = (x+direction[0]+grid.getSizeX())%grid.getSizeX(); //Handle boundaries by modulo
		int newY = (y+direction[1]+grid.getSizeY())%grid.getSizeY();

		//Ask the space to handle the move. It may not suceed. Space take care of setting rabbit's new coordinates with setXY()
		simulationSpace.moveRabbitAt(x, y, newX, newY);	

		//loose energy
		energy--;
	}

	//Getters & setters
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setSimulationSpace(RabbitsGrassSimulationSpace simulationSpace) {
		this.simulationSpace = simulationSpace;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getEnergy() {
		return energy;
	}



}
