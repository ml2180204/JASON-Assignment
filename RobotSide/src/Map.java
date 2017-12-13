import java.util.ArrayList;
import java.util.Collections;

//Map.java
//This class contains all known information about the map space the robot is operating in
//It is based on an occupancy grid
public class Map {

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

	// Constructor method
	public Map(int l, int w, int gl, int gw) {

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

	}

	private void addAdjacencyList(int x, int y) {
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
	public void setRobotSquare(GridSquare square) {
		robotSquare = square;
		robotSquare.setBeenHere(true);
	}

	// Return the grid square where the robot should finish
	public GridSquare getEndSquare() {
		return endSquare;
	}

	// Set the grid square where the robot should finish
	public void setEndSquare(GridSquare square) {
		endSquare = square;
		findEnd = true;
	}

	// return findEnd
	public boolean getFindEnd() {
		return findEnd;
	}

	// Return entire grid
	public GridSquare[][] getGrid() {
		return grid;
	}

	// Return a gridsquare
	public GridSquare getGridSquare(int x, int y) {
		return grid[x][y];
	}

	// update
	public void updateAdjacencyList() {

		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				if (grid[x][y].getBeenHere()) {
					grid[x][y].setOccupied(false);
				}
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

	// Scan a square and record whether we believe an obstacle is present
	public void scan(int x, int y, boolean occupied) {
		// Update the number of times this grid square has been scanned
		grid[x][y].incrementScanCount();
		if (occupied == true) {
			grid[x][y].changeM(1);
		} else {
			grid[x][y].changeM(-1);
		}

	}

	// Return length of arena
	public int getLength() {
		return LENGTH;
	}

	// Return width of arena
	public int getWidth() {
		return WIDTH;
	}

	// Return average length of one grid
	public int getOneGridLength() {
		return GRID_LENGTH;
	}

	// Return average width of one grid
	public int getOneGridWidth() {
		return GRID_WIDTH;
	}

	// Return the number of columns on the x axis
	public int getColumns() {
		return COLUMNS;
	}

	// Return the number of rows on the y axis
	public int getRows() {
		return ROWS;
	}

	// A* search algorithm to return an arraylist of sequential GridSquares showing
	// the shortest path using a Manhattan distance heuristic function
	public ArrayList<GridSquare> findPath(GridSquare start, GridSquare end) {

		end = getGridSquare(end.getXCoordinate(), end.getYCoordinate());
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