import java.util.ArrayList;

//Object representing a single grid square in the Map Class
//Stores information about whether the square is occupied, its coordinates
//and number of times it has been scanned

public class GridSquare {

	private int xPos; // Position on x axis in grid where gridsquare begins
	private int yPos; // Position on y axis in grid where gridsquare begins
	private int xCoordinate; // Gridsquare number on x axis (x coordinate)
	private int yCoordinate; // Gridsquare number on y axis (y coordinate
	private int gridLength; // Length of grid square
	private int gridWidth; // Width of grid square
	private boolean beenHere = false; // States whether this square has been visited
	private int scanCount = 0; // How many times this square has been scanned for obstacles
	private int M = 0; // the number M in each grid
	
	private ArrayList<Integer> psHead = new ArrayList<Integer>();
	private ArrayList<Integer> nextPsHead = new ArrayList<Integer>();
	
	private final double OCCUPANCY_THRESHOLD = 0.5; // Minimum threshold for a gridsquare to be classed as occupied

	// Parameters that are used in the A* search Algorithm
	// And to treat grid as a connected graph
	private ArrayList<GridSquare> adjacencyList = new ArrayList<GridSquare>(); // Neighbouring squares

	public GridSquare(int xc, int yc, int xp, int yp, int gw, int gl) {

		xCoordinate = xc;
		yCoordinate = yc;
		xPos = xp;
		yPos = yp;
		gridWidth = gw;
		gridLength = gl;
	}

	// Returns whether this square has been visited before
	public boolean getBeenHere() {
		return beenHere;
	}

	// Set whether this square has been visited before
	public void setBeenHere(boolean beenHere) {
		this.beenHere = beenHere;
	}

	// Check if probability is above threshold.
	// If above threshold and has been scanned at least once then probably occupied
	// If below threshold and scanned at least once then probably empty
	// If not scanned then it is unknown and assumed empty
	public boolean isOccupied() {
		if (occupancyProbability() >= OCCUPANCY_THRESHOLD && scanCount > 0) {
			return true;
		} else if (occupancyProbability() < OCCUPANCY_THRESHOLD && scanCount > 0) {
			return false;
		} else {
			return false;
		}
	}

	// set the grid as an empty grid
	public void setOccupied(boolean i) {
		if (!i) {
			M = 0;
			scanCount = 0;
		} else {
			M = 1;
			scanCount = 1;
		}
	}

	// Return number of times gridsquare has been scanned
	public int getScanCount() {
		return scanCount;
	}

	// Increment scan count if square is scanned
	public void incrementScanCount() {
		scanCount++;
	}

	public void changeM(int change) {
		M += change;
		scanCount++;
	}

	// Return the M
	public int getM() {
		return M;
	}

	// Calculate probability that square is occupied
	public double occupancyProbability() {
		return (double) (getM() + getScanCount()) / (double) (2 * getScanCount());
	}

	// Return x coordinate where gridsquare begins
	public int getX() {
		return xPos;
	}

	// Return y coordinate where gridsquare begins
	public int getY() {
		return yPos;
	}

	// Return the length of this grid square
	public int getGridLength() {
		return gridLength;
	}

	// Return the width of this gridsquare
	public int getGridWidth() {
		return gridWidth;
	}

	public int getXCoordinate() {
		return xCoordinate;
	}

	public int getYCoordinate() {
		return yCoordinate;
	}

	// Return a list of all neighbouring squares
	public ArrayList<GridSquare> getAdjacencyList() {
		return adjacencyList;
	}

	// Add a neighbouring square to the adjacency list
	public void addToAdjacencyList(GridSquare square) {
		adjacencyList.add(square);
	}

	// Remove a neighbouring square from adjacency list
	// E.g upon discovery of an obstacle or wall
	public void removeFromAdjacencyList(GridSquare square) {
		adjacencyList.remove(square);
	}
	
	//return the list of possible head
	public ArrayList<Integer> getHeadList(){
		return psHead;
	}
	
	//Add a possible head of the square
	public void addHead(Integer head) {
		psHead.add(head);
	}
	
	//remove a possible head
	public void removeHead(Integer head) {
		psHead.remove(head);
	}
	
	//return the list of next possible head
	public ArrayList<Integer> getNextHeadList(){
		return nextPsHead;
	}
	
	//Add a next possible head of the square
	public void addNextHead(Integer head) {
		nextPsHead.add(head);
	}
	
	//remove a possible head
	public void removeNextHead(Integer head) {
		nextPsHead.remove(head);
	}
}