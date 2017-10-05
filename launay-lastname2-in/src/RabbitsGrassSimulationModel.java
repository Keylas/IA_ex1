import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

	//Main elements of the simulation
	private Schedule schedule;
	private RabbitsGrassSimulationSpace simulationSpace;
	private DisplaySurface displaySurf;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
	
	private OpenSequenceGraph rabbitsGraph;
	private OpenSequenceGraph grassGraph;

	//Initial values of the parameters of the simulation
	private static final int GRIDSIZE = 20;
	private static final int NUMBERRABBITSINIT = 20; //Initial number of rabbits
	private static final int INITIALENERGY = 50; //Energy of a newborn rabbit
	private static final int BIRTHCOST = 50;
	private static final int BIRTHTHRESHOLD = 100;
	private static final int GRASSGROWTHRATE = 15;
	private static final int GRASSVALUE = 5;

	
	private int gridSize = GRIDSIZE;
	private int numberRabbitsInit = NUMBERRABBITSINIT;
	private int birthThreshold = BIRTHTHRESHOLD;
	private int birthCost = BIRTHCOST;
	private int initialEnergy = INITIALENERGY;
	private int grassGrowthRate = GRASSGROWTHRATE;
	private int grassValue = GRASSVALUE;


	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}

	public void begin() {


		//Build all necessary components
		buildModel();
		buildSchedule();
		buildDisplay();
		//Start displaying
		displaySurf.display();
		rabbitsGraph.display();
		grassGraph.display();
	}

	public void setup() {
		//Inherited method, called when simulation is (re)started [refresh button]
		simulationSpace = null; //get rid of the simulation space, will be rebind with begin()

		rabbitList = new ArrayList<RabbitsGrassSimulationAgent>(); //prepare a new list for agents

		schedule = new Schedule(1); //prepare new schedule [1=basic action executed every tick]

		//special care for display: it's a window so we must close it properly
		if(displaySurf !=null) {
			displaySurf.dispose();
		}

		displaySurf = null;
		displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Window");
		registerDisplaySurface("Rabbit Grass Simulation Window", displaySurf);
		
		if(rabbitsGraph!=null) {
			rabbitsGraph.dispose();
		}
		
		rabbitsGraph = new OpenSequenceGraph("Rabbits", this);
		this.registerMediaProducer("Plot", rabbitsGraph);
		
		if(grassGraph!=null) {
			grassGraph.dispose();
		}
		grassGraph = new OpenSequenceGraph("Grass", this);
		this.registerMediaProducer("Plot", grassGraph);
		
	}


	/*add a new aggent: register it to the model by adding it to the list, then let the space handle the positioning
	 * this method may fail (an return false) if the space cannot find a free space
	 */
	private boolean addNewRabbit() {
		RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(initialEnergy);
		if(simulationSpace.addRabbit(a)) {
			rabbitList.add(a);
			return true;
		}
		return false;
	}

	/*
	 * Method which enforce the birth and deaths of rabbits, by rabbitGrassStep() after grass grew and rabbits moved
	 */
	private void lifeOfRabbits() {
		//check status of every rabbit
		for(int i=0; i<rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent r = rabbitList.get(i);
			int energy = r.getEnergy();

			if(energy<1) { //Rabbit dies
				simulationSpace.removeRabbitAt(r.getX(),r.getY());
				rabbitList.remove(r);
			} else if(energy>=birthThreshold){ //Rabbit give birth
				addNewRabbit();
				r.setEnergy(energy-birthCost); //Giving birth cost energy (else rabbits would continuously give birth once the threshold is reached)
			}
		}
	}

	//Build window displaying the simlation
	private void buildDisplay() {

		//Create ColorMap for grass
		ColorMap map = new ColorMap();

		//No grass => brown (dirt)
		map.mapColor(0, new Color(165,42,42)); //Brown

		// Different shades of green depending on amount of grass (1: green ... 5: dark green ... >8: black [default])
		for(int i=1; i<16; i++) {
			map.mapColor(i*grassValue, new Color(0,127-i*8,0));
		}

		//Value2DDpislay for grass
		Value2DDisplay displayGrass = new Value2DDisplay(simulationSpace.getCurrentGrassSpace(),map);

		//Object2DDisplay for rabbits
		Object2DDisplay displayRabbits = new Object2DDisplay(simulationSpace.getCurrentRabbitSpace());
		//Must give list of objects
		displayRabbits.setObjectList(rabbitList);

		//Adds the displays to displaySurf, order matters (first grass then rabbits)
		displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		displaySurf.addDisplayableProbeable(displayRabbits, "Rabbits");
		
		rabbitsGraph.addSequence("Rabbits", new RabbitGraph(), Color.BLUE);
		grassGraph.addSequence("Grass (energy value)", new GrassGraph(), Color.GREEN);


	}

	private void buildSchedule() {
		/*
		 * Build the schedule by creating classes for each "tasks" that has to happen at regular intervals.
		 * They are then given to the schedule.
		 * They must extends BasicAction.
		 */

		//The main tasks, executed every step:
		class RabbitGrassStep extends BasicAction{
			public void execute() {

				//Grow grass
				for(int i=0; i<grassGrowthRate; i++) {
					simulationSpace.addGrass(grassValue);
				}

				//Proceed each rabbit in a random order
				SimUtilities.shuffle(rabbitList);
				for(int i=0; i<rabbitList.size(); i++) {
					rabbitList.get(i).step();
				}

				//Handle deaths/births
				lifeOfRabbits();

				//Update display
				displaySurf.updateDisplay();
			}
		}
		//Tell schedule to execute action at beginning and every tick after
		schedule.scheduleActionBeginning(0, new RabbitGrassStep());

		class RabbitGrassCountLiving extends BasicAction {
			public void execute() {
				rabbitsGraph.step();
				grassGraph.step();
				System.out.println("Rabbits: "+rabbitList.size()+" | Grass: "+(int)(simulationSpace.getAmountGrass()/grassValue));
			}
		}
		schedule.scheduleActionAtInterval(10, new RabbitGrassCountLiving());
	}

	private void buildModel() {
		//Create a new simulation space
		simulationSpace = new RabbitsGrassSimulationSpace(gridSize, gridSize);

		/*
		 * Take care of the initial setting (here populate the space with rabbits)
		 * Grass will ge generated at the begining of each tick by the schedule,
		 * so it is not necessary to place pre-existing grass.
		 * But we need to do it here for rabbits
		 */

		for(int i=0; i<numberRabbitsInit; i++) {
			addNewRabbit();
		}

	}


	/*
	 * Method from SimModelImpl which relays the parameters which can be modified on the interface
	 * The engine will look for get|set + "String" hence the capitalization because int gridSize => int getGridSize()
	 * @see uchicago.src.sim.engine.SimModel#getInitParam()
	 */
	public String[] getInitParam() {
		String[] initParams = {"GridSize", "NumberRabbitsInit", "InitialEnergy", "BirthCost", "BirthThreshold", "GrassGrowthRate", "GrassValue"};
		return initParams;
	}
	
	
	
	class RabbitGraph implements DataSource, Sequence {

		public double getSValue() {
			return rabbitList.size();
		}

		public Object execute() {
			return new Double(getSValue());
		}
		
	}
	
	class GrassGraph implements DataSource, Sequence {

		public double getSValue() {
			return simulationSpace.getAmountGrass();
		}

		public Object execute() {
			return new Double(getSValue());
		}
		
	}
	

	//Getters and setters (for parameters) required for model

	public String getName() {
		return "Rabbit Grass Simulation";
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getGridSize() {
		return gridSize;
	}
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}
	public int getNumberRabbitsInit() {
		return numberRabbitsInit;
	}
	public void setNumberRabbitsInit(int numberRabbitsInit) {
		this.numberRabbitsInit = numberRabbitsInit;
	}
	public int getBirthThreshold() {
		return birthThreshold;
	}
	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}
	public int getBirthCost() {
		return birthCost;
	}
	public void setBirthCost(int birthCost) {
		this.birthCost = birthCost;
	}
	public int getInitialEnergy() {
		return initialEnergy;
	}
	public void setInitialEnergy(int initialEnergy) {
		this.initialEnergy = initialEnergy;
	}
	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}
	public void setGrassGrowthRate(int grassGrowthRate) {
		this.grassGrowthRate = grassGrowthRate;
	}

	public int getGrassValue() {
		return grassValue;
	}
	public void setGrassValue(int grassValue) {
		this.grassValue = grassValue;
	}


}
