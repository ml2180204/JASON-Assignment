import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

//Map.java
//This class contains all known information about the map space the robot is operating in
//It is based on an occupancy grid
public class RRModel extends GridWorldModel{
	
	private final int LENGTH; // Arena length
	private final int WIDTH; // Arena width
	private final int GRID_LENGTH; // Length of one grid square
	private final int GRID_WIDTH; // Width of one grid square
	private final int COLUMNS; // Number of gridsquares on x axis
	private final int ROWS; // number of gridsquares on y axis

	///////////////////////////
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// !!!!!!!!!!!!!!!!!!!!!!Might need to switch COLUMNS and ROWS over, and gird
	/////////////////////////// length width subsequently

	// Checked may not be necessary unless want to use probability, could just
	// assign grid to be 1 or 0
	private GridSquare[][] grid; // Array of coordinates and the occupancy

	private GridSquare originSquare = null; // Grid square where robot had begun
	private GridSquare endSquare = null; // Grid Square where robot should finish
	private GridSquare robotSquare = null; // Grid Square robot currently occupies
	private boolean findEnd = false;

	//simulate the current robot
//	private int curr_head = 0;
//    private Location curr_location = new Location (4,3);
//    public Location smltRed = new Location(4,3);
//    public Location smltBlue = new Location(4,0);
//    public Location smltGreen = new Location(0,4);
    
    public static final ArrayList<GridSquare> ps_square = new ArrayList<GridSquare>(); //store the possible square
    ArrayList<GridSquare> next_ps_square = new ArrayList<GridSquare>(); //store the next_possible square
  
    //the agents belief
    public Integer scoutHead;
    public Location nextVictim = new Location (0,0);
    public boolean detected_obstacles = false;
//    public boolean detected_color = false;
    public boolean[] obstacles = new boolean[4]; 
    public String victimColor = "empty";
    
    //possible victim list
    ArrayList<GridSquare> ps_victim = new ArrayList<GridSquare>();
    //found victim list
    ArrayList<GridSquare> found_victim = new ArrayList<GridSquare>();
    
	// Constructor method
	public RRModel(int l, int w, int gl, int gw) {
		super(Math.round(w/gw),Math.round(l/gl),1);
		
		// Set grid dimensiosn
		LENGTH = l;
		WIDTH = w;
		GRID_LENGTH = gl;
		GRID_WIDTH = gw;
		COLUMNS = Math.round(WIDTH / GRID_WIDTH);
		ROWS = Math.round(LENGTH / GRID_LENGTH);
		
		// Initialise number of squares in map
		// One row of squares on each axis *may* be a slightly different size
		grid = new GridSquare[COLUMNS][ROWS];

		
		// Create graph of all grid squares
		// HashMap<String, GridSquare> gridGraph = new HashMap<String, GridSquare>();
		// Initialise each grid square and set its X and Y coordinates and size
		// Size may vary for the last row and column so this code adjusts the size of
		// those squares
		int lengthRemaining = LENGTH;

		for (int y = 0; y < ROWS; y++) {
			int widthRemaining = WIDTH;
			for (int x = 0; x < COLUMNS; x++) {
				// If a normal grid square
				if (x != COLUMNS - 1 && y != ROWS - 1) {
					grid[x][y] = new GridSquare(x, y, x * GRID_WIDTH, y * GRID_LENGTH, GRID_WIDTH, GRID_LENGTH);
					// If grid square is in the last column on the x axis
				} else if (x == COLUMNS - 1 && y != ROWS - 1) {
					grid[x][y] = new GridSquare(x, y, x * GRID_WIDTH, y * GRID_LENGTH, widthRemaining, GRID_LENGTH);
					// If grid square is in the last row on the y axis
				} else if (x != COLUMNS - 1 && y == ROWS - 1) {
					grid[x][y] = new GridSquare(x, y, x * GRID_WIDTH, y * GRID_LENGTH, GRID_WIDTH, lengthRemaining);
					// If grid square is in the last column of the x axis and the last column of the
					// y axis
				} else if (x == COLUMNS - 1 && y == ROWS - 1) {
					grid[x][y] = new GridSquare(x, y, x * GRID_WIDTH, y * GRID_LENGTH, widthRemaining, lengthRemaining);
				}
				widthRemaining = widthRemaining - GRID_WIDTH;
			}
			lengthRemaining = lengthRemaining - GRID_LENGTH;
		}
		
		//initialize the obstacles on the map
//		grid[2][1].setOccupied(true);
//		grid[4][1].setOccupied(true);
//		grid[1][2].setOccupied(true);
//		grid[0][5].setOccupied(true);
//		grid[4][4].setOccupied(true);
//		grid[5][4].setOccupied(true);
//		ps_victim.add(grid[0][0]);
//		ps_victim.add(grid[2][2]);
//		ps_victim.add(grid[4][0]);
//		ps_victim.add(grid[3][3]);
//		ps_victim.add(grid[2][4]);
		//update
	}

	void initObs(int x,int y) {
		add(RREnv.GARB, x, y);
		grid[x][y].setOccupied(true);
		updateAdjacencyList();
	}
	
	void initVictim(int x, int y) {
		add(RREnv.POSSIBLE_VIC, x, y);
		ps_victim.add(grid[x][y]);
	}
	// the N,S,W,E direction of one grid contains obstacles
	boolean NorthObs(int x, int y) {
		if(y!=0 && grid[x][y].getAdjacencyList().contains(grid[x][y-1])) {
			return false;
		} else {
			return true;
		}
	}
	
	boolean SouthObs(int x, int y) {
		if(y!=ROWS-1 && grid[x][y].getAdjacencyList().contains(grid[x][y+1])) {
			return false;
		} else {
			return true;
		}
	}
	
	boolean WestObs(int x, int y) {
		if(x!=0 && grid[x][y].getAdjacencyList().contains(grid[x-1][y])) {
			return false;
		} else {
			return true;
		}
	}
	
	boolean EastObs(int x, int y) {
		if(x!=COLUMNS-1 && grid[x][y].getAdjacencyList().contains(grid[x+1][y])) {
			return false;
		} else {
			return true;
		}
	}
	
	//the adjacency obstacle with robot direction
	boolean upObs(int x, int y, int head) {
		if(head%4==0) {
			return NorthObs(x,y);
		} else if(head%4==1 ||head%4==-3) {
			return EastObs(x,y);
		} else if(head%4==-1 || head%4==3) {
			return WestObs(x,y);
		} else if(head%4==-2 || head%4==2) {
			return SouthObs(x,y);
		} else {
			return false;
		}
	}
	
	boolean downObs(int x, int y, int head) {
		if(head%4==0) {
			return SouthObs(x,y);
		} else if(head%4==1 ||head%4==-3) {
			return WestObs(x,y);
		} else if(head%4==-1 || head%4==3) {
			return EastObs(x,y);
		} else if(head%4==-2 || head%4==2) {
			return NorthObs(x,y);
		} else {
			return false;
		}
	}
	
	boolean leftObs(int x, int y, int head) {
		if(head%4==0) {
			return WestObs(x,y);
		} else if(head%4==1 ||head%4==-3) {
			return NorthObs(x,y);
		} else if(head%4==-1 || head%4==3) {
			return SouthObs(x,y);
		} else if(head%4==-2 || head%4==2) {
			return EastObs(x,y);
		} else {
			return false;
		}
	}
	
	boolean rightObs(int x, int y, int head) {
		if(head%4==0) {
			return EastObs(x,y);
		} else if(head%4==1 ||head%4==-3) {
			return SouthObs(x,y);
		} else if(head%4==-1 || head%4==3) {
			return NorthObs(x,y);
		} else if(head%4==-2 || head%4==2) {
			return WestObs(x,y);
		} else {
			return false;
		}
	}
	//action
	void updatePsLocation(){
		boolean flag1;
		boolean flag2;
		if(!next_ps_square.isEmpty()) {
			for(int i=0; i<ps_square.size();i++) {
				ps_square.get(i).getHeadList().clear();
				remove(RREnv.POSSIBLE_LOCATION, ps_square.get(i).getXCoordinate(),ps_square.get(i).getYCoordinate());
			}
			ps_square.clear();
			ps_square.addAll(next_ps_square);
			
			for(int i=0; i<ps_square.size();i++) {
				ps_square.get(i).getHeadList().addAll(ps_square.get(i).getNextHeadList());
				ps_square.get(i).getNextHeadList().clear();
			}
			next_ps_square.clear();
		}
		if(ps_square.isEmpty()) {
			for (int y = 0; y < ROWS; y++) {
				for (int x = 0; x < COLUMNS; x++) {
					for(Integer ps_head=0; ps_head<4; ps_head++) {
						flag1=false;
						if(grid[x][y].isOccupied()) {
							break;
						}
						if(obstacles[0]) {
							if(upObs(x,y,ps_head)) {
								flag1=true;
							}
						} else if(obstacles[3]){
							if(downObs(x,y,ps_head)) {
								flag1=true;
							}
						} else if(obstacles[1]) {
							if(leftObs(x,y,ps_head)) {
								flag1=true;
							}
						} else if(obstacles[2]) {
							if(rightObs(x,y,ps_head)) {
								flag1=true;
							}
						} else {
							if(!rightObs(x,y,ps_head)&& !rightObs(x,y,ps_head)&& !rightObs(x,y,ps_head)&& !rightObs(x,y,ps_head)) {
								flag1=true;
							}
						}
						if(flag1) {
							if(!ps_square.contains(grid[x][y])) {
								ps_square.add(grid[x][y]);
							}
							grid[x][y].addHead(ps_head);
						}
					}
				}
			}
		}
		if(!ps_square.isEmpty()) {
			for(int i=0, len1=ps_square.size(); i<len1;i++) {
				for(int j=0, len2=ps_square.get(i).getHeadList().size(); j<len2; j++) {
					flag2=false;
					int x = ps_square.get(i).getXCoordinate();
					int y = ps_square.get(i).getYCoordinate();
					Integer ps_head = ps_square.get(i).getHeadList().get(j);
					if(obstacles[0]
							&& !upObs(x,y,ps_head)) {
						flag2=true;
					}
					if(!obstacles[0]
							&& upObs(x,y,ps_head)) {
						flag2=true;
					}
					if(obstacles[3]
							&& !downObs(x,y,ps_head)) {
						flag2=true;
					}
					if(!obstacles[3]
							&& downObs(x,y,ps_head)) {
						flag2=true;
					}
					if(obstacles[1]
							&& !leftObs(x,y,ps_head)) {
						flag2=true;
					}
					if(!obstacles[1]
							&& leftObs(x,y,ps_head)) {
						flag2=true;
					}
					if(obstacles[2]
							&& !rightObs(x,y,ps_head)) {
						flag2=true;
					}
					if(!obstacles[2]
							&& rightObs(x,y,ps_head)) {
						flag2=true;
					}
					if(flag2) {
						ps_square.get(i).getHeadList().remove(j);
						len2--;
						j--;
						if(ps_square.get(i).getHeadList().isEmpty()){
							ps_square.remove(i);
							len1--;
							i--;
						}
					}
				}
			}
		}
		drawPsLocation();
	}
	void updateNextPsLocation() {
		for(int i=0; i<ps_square.size();i++) {
			for(int j=0, s=ps_square.get(i).getHeadList().size(); j<s;j++) {
				int x = ps_square.get(i).getXCoordinate();
				int y = ps_square.get(i).getYCoordinate();
				int h = ps_square.get(i).getHeadList().get(j);
				if(h%4==0) {
					if(!next_ps_square.contains(grid[x][y-1])) {
						next_ps_square.add(grid[x][y-1]);
					}
					grid[x][y-1].addNextHead(h);
				} else if((h%4==1||h%4==-3)) {
					if(!next_ps_square.contains(grid[x+1][y])) {
						next_ps_square.add(grid[x+1][y]);
					}
					grid[x+1][y].addNextHead(h);
				} else if((h%4==-1||h%4==3)) {
					if(!next_ps_square.contains(grid[x-1][y])) {
						next_ps_square.add(grid[x-1][y]);
					}
					grid[x-1][y].addNextHead(h);
				} else if((h%4==2||h%4==-2)) {
					if(!next_ps_square.contains(grid[x][y+1])) {
						next_ps_square.add(grid[x][y+1]);
					}
					grid[x][y+1].addNextHead(h);
				}
			}
		}

	}
	void nextSlot() {
		System.out.println(obstacles[0]);
		System.out.println(obstacles[1]);
		System.out.println(obstacles[2]);
		System.out.println(obstacles[3]);
		
		updatePsLocation();
		if(!(ps_square.size()==1 && ps_square.get(0).getHeadList().size()==1)) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//specify one position
		int x = ps_square.get(0).getXCoordinate();
		int y = ps_square.get(0).getYCoordinate();
		int ps_head = ps_square.get(0).getHeadList().get(0);
		
		if(!upObs(x,y,ps_head)) {
		} else if(!leftObs(x,y,ps_head)) {

			for(int i=0; i<ps_square.size();i++) {
				for(int j=0; j<ps_square.get(i).getHeadList().size();j++) {
					int h = ps_square.get(i).getHeadList().get(j);
					ps_square.get(i).getHeadList().set(j, --h);
				}
			}
		} else if(!rightObs(x,y,ps_head)) {

			for(int i=0; i<ps_square.size();i++) {
				for(int j=0; j<ps_square.get(i).getHeadList().size();j++) {
					int h = ps_square.get(i).getHeadList().get(j);
					ps_square.get(i).getHeadList().set(j, ++h);
				}
			}
		} else if(!downObs(x,y,ps_head)) {

			for(int i=0; i<ps_square.size();i++) {
				for(int j=0; j<ps_square.get(i).getHeadList().size();j++) {
					int h = ps_square.get(i).getHeadList().get(j);
					int s = h+2;
					ps_square.get(i).getHeadList().set(j, s);
				}
			}
		}
		
		updateNextPsLocation();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		} else {
			remove(RREnv.POSSIBLE_LOCATION, ps_square.get(0).getXCoordinate(),ps_square.get(0).getYCoordinate());
			setAgPos(0, ps_square.get(0).getXCoordinate(), ps_square.get(0).getYCoordinate() );
			scoutHead = ps_square.get(0).getHeadList().get(0);
		}
		
	}
	
    void drawPsLocation() {
		for(int i=0; i<ps_square.size();i++) {
			add(RREnv.POSSIBLE_LOCATION, ps_square.get(i).getXCoordinate(),ps_square.get(i).getYCoordinate());
			//for(int j=0; j<ps_square.get(i).getHeadList().size(); j++) {
			//	add(RREnv.POSSIBLE_HEAD, ps_square.get(i).getXCoordinate(),ps_square.get(i).getYCoordinate());
		//	}
		}
	}
	
    void findNearestVictim() {
    	GridSquare start = grid[getAgPos(0).x][getAgPos(0).y];
    	int min_length = 1000;
    	for(int i=0; i<ps_victim.size(); i++) {
    		GridSquare end = ps_victim.get(i);
    		int curr_length = findPath(start,end).size();
    		if(curr_length<min_length) {	
    			min_length = curr_length;
    			nextVictim.x = end.getXCoordinate();
    			nextVictim.y = end.getYCoordinate();
    		}
    	}
    }
    
    
    void moveTo(int x, int y) {
    	Location scout = getAgPos(0);
        if (scout.x < x) {
            ++scout.x;
        	if(scoutHead%4==0) {
        		scoutHead++;
        	} else if(scoutHead%4==2 ||scoutHead%4==-2) {
        		scoutHead--;
        	}
        }
        if (scout.x > x) {
            --scout.x;
        	if(scoutHead%4==0) {
        		scoutHead--;
        	} else if(scoutHead%4==2 ||scoutHead%4==-2) {
        		scoutHead++;
        	}
        }
        if (scout.y < y) {
            ++scout.y;
        	if(scoutHead%4==1||scoutHead%4==-3) {
        		scoutHead++;
        	} else if(scoutHead%4==-1||scoutHead%4==3) {
        		scoutHead--;
        	}
        }
        if (scout.y > y) {
            --scout.y;
        	if(scoutHead%4==1||scoutHead%4==-3) {
        		scoutHead--;
        	} else if(scoutHead%4==-1||scoutHead%4==3) {
        		scoutHead++;
        	}
        }
        setAgPos(0, scout);
        ps_square.get(0).getHeadList().clear();
        ps_square.set(0, grid[scout.x][scout.y]);
        ps_square.get(0).getHeadList().add(scoutHead);
        setAgPos(0, scout);
   }
    
    GridSquare getNextSquare() {
    	findNearestVictim();
    	GridSquare nextSquare = findPath(grid[getAgPos(0).x][getAgPos(0).y],
    			grid[nextVictim.x][nextVictim.y]).get(0);
    	return nextSquare;
    }
    
    void goToVcitim() {
    	findNearestVictim();
    	GridSquare nextSquare = findPath(grid[getAgPos(0).x][getAgPos(0).y],grid[nextVictim.x][nextVictim.y]).get(0);
    	moveTo(nextSquare.getXCoordinate(),nextSquare.getYCoordinate());	
    }
    
    
    void checkVictim() {
    	int x = getAgPos(0).x;
    	int y = getAgPos(0).y;
    	if(victimColor.equals("red")) {
    		found_victim.add(grid[x][y]);
    		ps_victim.remove(grid[x][y]);
    		remove(RREnv.POSSIBLE_VIC, getAgPos(0));
    		add(RREnv.REDVIC, getAgPos(0));
    	} else if (victimColor.equals("blue")) {
    		found_victim.add(grid[x][y]);
    		ps_victim.remove(grid[x][y]);
    		remove(RREnv.POSSIBLE_VIC, getAgPos(0));
    		add(RREnv.BLUEVIC, getAgPos(0));
    	} else if (victimColor.equals("green")) {
    		found_victim.add(grid[x][y]);
    		ps_victim.remove(grid[x][y]);
    		remove(RREnv.POSSIBLE_VIC, getAgPos(0));
    		add(RREnv.GREENVIC, getAgPos(0));
    	} else {
    		ps_victim.remove(grid[x][y]);
    		remove(RREnv.POSSIBLE_VIC, getAgPos(0));
    	}
    	
    }
    
	void addAdjacencyList(int x, int y) {
		// If there is a square to the left and it does not contain an obstacle
		// and doesn't have gridsquare on its adjacency list already
		// add it to adjacency list
		if (x != 0) {
			if (grid[x - 1][y].isOccupied() == false && grid[x - 1][y].getAdjacencyList() != null) {
				// System.out.println(grid[x-1][y].getAdjacencyList().contains(grid[x][y]));
				if (!grid[x][y].getAdjacencyList().contains(grid[x - 1][y])) {
					grid[x][y].addToAdjacencyList(grid[x - 1][y]);
				}
			}
		}

		// If there is a square to the right and it does not contain an obstacle
		// add it to adjacency list
		if (x != COLUMNS - 1) {
			if (grid[x + 1][y].isOccupied() == false && grid[x + 1][y].getAdjacencyList() != null) {
				if (!grid[x][y].getAdjacencyList().contains(grid[x + 1][y])) {
					grid[x][y].addToAdjacencyList(grid[x + 1][y]);
				}
			}
		}

		// If there is a square downwards and it does not contain an obstacle
		// add it to adjacency list
		if (y != 0) {
			if (grid[x][y - 1].isOccupied() == false && grid[1][y - 1].getAdjacencyList() != null) {
				if (!grid[x][y].getAdjacencyList().contains(grid[x][y - 1])) {
					grid[x][y].addToAdjacencyList(grid[x][y - 1]);
				}
			}
		}

		// If there is a square upwards and it does not contain an obstacle
		// add it to adjacency list
		if (y != ROWS - 1) {
			if (grid[x][y + 1].isOccupied() == false && grid[x][y + 1].getAdjacencyList() != null) {
				if (!grid[x][y].getAdjacencyList().contains(grid[x][y + 1])) {
					grid[x][y].addToAdjacencyList(grid[x][y + 1]);

				}
			}
		}
	}

	// Return the square where the robot started
	public GridSquare getOriginSquare() {
		return originSquare;
	}

	// Return the gridsquare the robot is currently occupying
	public GridSquare getRobotSquare() {
		return robotSquare;
	}

	// Set the coordinates of the robot
	// State that the square has been visited
	void setRobotSquare(GridSquare square) {
		robotSquare = square;
		robotSquare.setBeenHere(true);
	}


	// update
	void updateAdjacencyList() {
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (grid[x][y].isOccupied() == false) {
					// Fill in this square's adjacency list
					addAdjacencyList(x, y);
				}
			}
		}
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (grid[x][y].isOccupied()) {
					// Go to each neighbour of an obstacled grid square and remove the obstacled
					// grid square
					// from their adjacency lists
					for (int i = 0; i < grid[x][y].getAdjacencyList().size(); i++) {
						GridSquare neighbour = grid[x][y].getAdjacencyList().get(i);
						if (neighbour.getAdjacencyList().contains(grid[x][y])) {
							neighbour.removeFromAdjacencyList(grid[x][y]);
						}
					}
				}
			}
		}

	}


	// A* search algorithm to return an arraylist of sequential GridSquares showing
	// the shortest path using a Manhattan distance heuristic function
	public ArrayList<GridSquare> findPath(GridSquare start, GridSquare end) {
		// Translate each grid square into a node object
		class Node implements Comparable<Node> {
			GridSquare square; // The grid square or "position" that this node corresponds to
			int g; // Distance travelled to this grid square from the start grid square
			int h; // Manhattan distance from this grid square to the final gridsquare
			Node parent; // Previous GridSquare to access this GridSquare from for shortest path

			// Constructor requires specifying which gridsquare this is, and its parent
			Node(GridSquare sqre, Node prnt) {
				square = sqre;
				parent = prnt;
			}

			// Comparison function to sort GridSquares in descending order of heuristic
			// value
			public int compareTo(Node compareNode) {
				int compareF = ((Node) compareNode).getF();
				// Sort in ascending order
				return compareF - this.getF();
			}

			// Return the parent node of a path
			public Node getParent() {
				return parent;
			}

			// Return the grid square associated with this node
			public GridSquare getSquare() {
				return square;
			}

			// Set parent node of a path
			public void setParent(Node parent) {
				this.parent = parent;
			}

			// Return g - the Manhattan distance from the start square
			public int getG() {
				return g;
			}

			// Set g
			public void setG(int g) {
				this.g = g;
			}

			// Return h - the Manhattan distance to the end square
			public int getH() {
				return h;
			}

			// Set h
			public void setH(int h) {
				this.h = h;
			}

			// Return the approximation heuristic
			public int getF() {
				return g + h;
			}
		}

		ArrayList<Node> closedList = new ArrayList<Node>();
		ArrayList<Node> openList = new ArrayList<Node>();

		boolean found = false;
		Node currentNode; // The current node being looked at
		Node endNode = null;// The final node once found

		// Add start node as first node to explore
		openList.add(new Node(start, null));

		while (!openList.isEmpty() && !found) {
			// Collections.sort(openList);
			currentNode = openList.remove(0);
			// Neighbour square is one square away from current square
			// cost = currentNode.getG() + 1;
			// Iterate through each neighbour of current node
			for (int i = 0; i < currentNode.square.getAdjacencyList().size(); i++) {
				Node neighbour = new Node(currentNode.square.getAdjacencyList().get(i), currentNode);

				// If neighbour is goal, stop search
				if (neighbour.square == end) {
					found = true;
					endNode = neighbour;
					break;
				}

				// Set a neighbouring node's G and H values
				// G is the distance from the inital node
				neighbour.setG(currentNode.getG() + 1);

				// H is the Manahttan distance which is calculated using a grid square's
				// coordinates
				neighbour.setH(Math.abs(neighbour.square.getXCoordinate() - end.getXCoordinate())
						+ Math.abs(neighbour.square.getYCoordinate() - end.getYCoordinate()));

				for (int j = 0; j < openList.size(); j++) {
					// If another node with same position exists and has a lower f, skip this node
					if (openList.get(j).square == neighbour.square && openList.get(j).getF() < neighbour.getF()) {
						break;
					}
				}

				for (int j = 0; j < closedList.size(); j++) {
					// If another node with same position exists and has a lower f, skip this node
					if (closedList.get(j).square == neighbour.square && closedList.get(j).getF() < neighbour.getF()) {
						break;
					}
				}

				// If this is a new node, add to open list to check later
				if (!openList.contains(neighbour) && !closedList.contains(neighbour)) {
					openList.add(neighbour);
				}
			}

			// This node has been looked at so put it in closedList
			closedList.add(currentNode);
			// System.out.println("X"+ currentNode.square.getXCoordinate() + " Y"+
			// currentNode.square.getYCoordinate() + " G = " + currentNode.getG());
		}

		// The sequence of gridsqares that must be travelled to in order get from start
		// to end
		ArrayList<GridSquare> sequence = new ArrayList<GridSquare>();
		currentNode = endNode;

		// Create sequential array of path to follow
		while (currentNode.parent != null) {
			sequence.add(currentNode.square);
			currentNode = currentNode.getParent();
		}

		// Order sequence from start -> end
		Collections.reverse(sequence);
		return sequence;
	}

	// BFS to find closest square to robot that has not been visited yet
	// Calls A* search to find shortest path to this unvisited and returns the path
	public ArrayList<GridSquare> findUnvisitedSquare() {
		ArrayList<GridSquare> visited = new ArrayList<GridSquare>();
		; // Nodes that have been checked and have been visited
		ArrayList<GridSquare> agenda = new ArrayList<GridSquare>();
		; // Nodes to check
		GridSquare currentNode = null;
		agenda.add(robotSquare);

		// Loop until a node is found that has not been visited, and
		do {
			currentNode = agenda.remove(0);
			visited.add(currentNode);
			// Iterate through each neighbour of current node
			for (int i = 0; i < currentNode.getAdjacencyList().size(); i++) {
				GridSquare neighbour = currentNode.getAdjacencyList().get(i);
				// If neighbour node is a new node, add it to
				if (agenda.contains(neighbour) == false && visited.contains(neighbour) == false) {
					agenda.add(neighbour);
				}
			}
		} while (currentNode.getBeenHere() == true && visited.size() < COLUMNS * ROWS);
		return findPath(robotSquare, currentNode);
	}

}
//class Simulation {
//	private ArrayList<Boolean[]> sr = new ArrayList<Boolean[]>();
//	public ArrayList<Boolean[]>  returnSimu() {
//		Location smltRed = new Location(4,3);
//		
//	}
//}