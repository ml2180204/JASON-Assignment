import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;

//Creates a robot object that can retrieve sensor data, rotate the ultrasound sensor 
//and access the robot move pilot
public class Robot {
	private final float robotLength = 25;
	private final float robotWidth = 20;

	private EV3TouchSensor leftBump, rightBump;
	private EV3UltrasonicSensor uSensor;
	private EV3ColorSensor cSensor;
	private SampleProvider leftSP, rightSP, distSP, colourSP;
	private float[] leftSample, rightSample, distSample, colourSample;

	private MovePilot pilot;
	private Map map;

	private EV3MediumRegulatedMotor sensorMotor;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private OdometryPoseProvider opp;

	public float[] endLocation = new float[2];
	public int direction_flag = 0;

	public Robot(Map gridMap) {
		// Instantiate Brick
		Brick myEV3 = BrickFinder.getDefault();

		// Assign robot to map
		map = gridMap;

		// Instantiate Motors
		leftMotor = new EV3LargeRegulatedMotor(myEV3.getPort("B"));
		rightMotor = new EV3LargeRegulatedMotor(myEV3.getPort("C"));

		// Instantiate Sensors
		rightBump = new EV3TouchSensor(myEV3.getPort("S1"));
		leftBump = new EV3TouchSensor(myEV3.getPort("S2"));
		uSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		cSensor = new EV3ColorSensor(myEV3.getPort("S4"));

		leftSP = leftBump.getTouchMode();
		rightSP = rightBump.getTouchMode();
		distSP = uSensor.getDistanceMode();
		colourSP = cSensor.getColorIDMode();

		leftSample = new float[leftSP.sampleSize()]; // Size is 1
		rightSample = new float[rightSP.sampleSize()]; // Size is 1
		distSample = new float[distSP.sampleSize()]; // Size is 1
		colourSample = new float[colourSP.sampleSize()]; // Size is 1

		// Set up the wheels by specifying the diameter of the
		// left (and right) wheels in centimeters, i.e. 3.25 cm
		// the offset number is the distance between the center
		// of wheel to the center of robot, i.e. half of track width
		// NOTE: this may require some trial and error to get right!!!
		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, 3.3).offset(-8.8);
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, 3.3).offset(8.8);
		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);

		// Create new move pilot
		pilot = new MovePilot(myChassis);
		sensorMotor = new EV3MediumRegulatedMotor(myEV3.getPort("A"));

		// Create new opp
		opp = new OdometryPoseProvider(pilot);

		// set the speed of the pilot
		pilot.setAngularSpeed(35);
	}

	// Close all sensors
	public void closeRobot() {
		leftBump.close();
		rightBump.close();
		uSensor.close();
		cSensor.close();
	}

	// Returns robot length and width
	public float getRobotLength() {
		return robotLength;
	}

	public float getRobotWidth() {
		return robotWidth;
	}

	// Returns left bumper sensor data
	public boolean isLeftBumpPressed() {
		leftSP.fetchSample(leftSample, 0);
		return (leftSample[0] == 1.0);
	}

	// Returns right bumper sensor data
	public boolean isRightBumpPressed() {
		rightSP.fetchSample(rightSample, 0);
		return (rightSample[0] == 1.0);
	}

	// Return the direction of the robot faced, assume the origin is front
	public boolean faceFront() {
		return (direction_flag % 4 == 0);
	}

	public boolean faceRight() {

		return (direction_flag % 4 == 1 || direction_flag % 4 == -3);
	}

	public boolean faceLeft() {
		return (direction_flag % 4 == -1 || direction_flag % 4 == 3);
	}

	public boolean faceBack() {
		return (direction_flag % 4 == -2 || direction_flag % 4 == 2);
	}

	// Update the Grid which has been detected (may be modified by real world data:
	// for example the distance between sensor and the center of the robot;
	// degree is the degree between sensor and the front direction of robot;
	public void updateGrid(float degree, float distance) {
		// the current robot position
		int x0 = map.getRobotSquare().getXCoordinate();
		int y0 = map.getRobotSquare().getYCoordinate();

		// the obstacles position in map
		int xn;
		int yn;
		if ((faceFront() && degree == 0) || (faceRight() && degree == 90)) {
			yn = y0 + (int) (distance * 100 / map.getOneGridLength()) + 1;
			if (yn > 0 && yn < map.getRows()) { // ensure the boundary and update
				map.getGridSquare(x0, yn).changeM(1);

			}
			// update other grid in the path of the detection is M--;
			if (yn > map.getRows()) {
				yn = map.getRows();
			}
			for (int i = y0; i < yn; i++) {
				map.getGridSquare(x0, i).changeM(-1);
			}
		} else if ((faceBack() && degree == 0) || (faceLeft() && degree == 90)) {
			yn = y0 - (int) (distance * 100 / map.getOneGridLength()) - 1;
			if (yn > 0 && yn < map.getRows()) { // ensure the boundary and update
				map.getGridSquare(x0, yn).changeM(1);
			}

			// update other grid in the path of the detection is M--;
			if (yn < 0) {
				yn = 0;
			}
			for (int i = yn; i < y0; i++) {
				map.getGridSquare(x0, i).changeM(-1);
			}
		} else if ((faceLeft() && degree == 0) || (faceFront() && degree == 90)) {
			xn = x0 + (int) (distance * 100 / map.getOneGridWidth()) + 1;
			if (xn > 0 && xn < map.getColumns()) { // ensure the boundary and update
				map.getGridSquare(xn, y0).changeM(1);
			}
			if (xn > map.getColumns()) {
				xn = map.getColumns();
			}
			// update other grid in the path of the detection is M--;
			for (int i = x0; i < xn; i++) {
				map.getGridSquare(i, y0).changeM(-1);
			}
		} else if ((faceRight() && degree == 0) || (faceBack() && degree == 90)) {
			xn = x0 - (int) (distance * 100 / map.getOneGridWidth()) - 1;
			if (xn > 0 && xn < map.getColumns()) { // ensure the boundary and update
				map.getGridSquare(xn, y0).changeM(1);
			}

			if (xn < 0) {
				xn = 0;
			}
			// update other grid in the path of the detection is M--;
			for (int i = xn; i < x0; i++) {
				map.getGridSquare(i, y0).changeM(-1);
			}
		}
	}

	// Returns Ultrasound sensor data

	public float getDistance() {
		distSP.fetchSample(distSample, 0);
		return distSample[0];
	}

	// Returns color sensor data

	public float[] getColour() {
		colourSP.fetchSample(colourSample, 0);
		return colourSample; // return array of 3 colours
	}

	// Rotate sensor motor to specified angle
	public void rotateSensor(int angle) {
		sensorMotor.rotate(angle);
	}

	// Return the robot movepilot
	public MovePilot getPilot() {
		return pilot;
	}

	// Return map the robot occupies
	public Map getMap() {
		return map;
	}

	// Return opp
	public OdometryPoseProvider getOpp() {
		return opp;
	}

	// update the direction_flag
	public void changeFlag(int i) {
		direction_flag += i;
	}
	
	//move behavior
	public void goAhead() {
		getPilot().travel(12);
	}
	public void goLeft() {
		getPilot().rotate(-90);
		goAhead();
	}
	public void goRight() {
		getPilot().rotate(90);
		goAhead();
	}
	public void goBack() {
		getPilot().rotate(180);
		goAhead();
	}
	
	//detect adjacency obs
	public boolean[] detectObstacles(boolean first) {
		boolean [] obstacles = new boolean[4];
		int d = getMap().getOneGridWidth();
		if(getDistance()>d) {
			obstacles[0] =true;
		} else {
			obstacles[0] =false;
		}
		rotateSensor(-90);
		if(getDistance()>d) {
			obstacles[1] =true;
		} else {
			obstacles[1] =false;
		}
		rotateSensor(180);
		if(getDistance()>d) {
			obstacles[2] =true;
		} else {
			obstacles[2] =false;
		}
		if(first) {
			getPilot().rotate(180);
			if(getDistance()>d) {
				obstacles[3] =true;
			} else {
				obstacles[3] =false;
			}
			getPilot().rotate(180);
		}
		return obstacles;
	}
	
	// Calculates the position of the robot
	// By Checking it's approximate position,
	// Ensure the robot will move to the next grid
	// calibrate the rotation
	// move_distance is the front of the robot will remove
	// right_distance is the center of the grid right of the robot
	public void moveToNextGrid(float move_distance, float right_distance) {
		// get the robot current position
		int x = getMap().getRobotSquare().getXCoordinate(); // old x
		int y = getMap().getRobotSquare().getYCoordinate(); // old y
		// get the direction of the robot to get the next position
		float initWallDistance = getDistance();
		getPilot().travel(move_distance);
		if (move_distance > 0) {
			if (faceFront()) {
				y++;
			} else if (faceBack()) {
				y--;
			} else if (faceLeft()) {
				x++;
			} else if (faceRight()) {
				x--;
			}
		} else {
			if (faceFront()) {
				y--;
			} else if (faceBack()) {
				y++;
			} else if (faceLeft()) {
				x--;
			} else if (faceRight()) {
				x++;
			}
		}

		// set the robot position
		getMap().setRobotSquare(getMap().getGridSquare(x, y));
		float afterWallDistance = getDistance();

		// calibrate if the right of the robot is not a obstacle
		if (afterWallDistance * 100 < right_distance && initWallDistance * 100 < right_distance) { // ensure the right
																									// is also a wall
			float mistakeDistance = initWallDistance - afterWallDistance;
			float angle = (float) (Math.toDegrees(Math.sin(mistakeDistance * 100 / move_distance)));
			getPilot().rotate(-angle);
		} else {
		}
	}

	// one grid move to another adjacency grid
	public void Move(GridSquare start, GridSquare end) {
		if (start.getXCoordinate() < end.getXCoordinate()) {
			if (faceLeft()) {
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceRight()) {
				// getPilot().rotate(180);
				// direction_flag +=2;
				moveToNextGrid(-getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceFront()) {
				getPilot().rotate(-90);
				direction_flag--;
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceBack()) {
				getPilot().rotate(90);
				direction_flag++;
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
		} else if (start.getXCoordinate() > end.getXCoordinate()) {
			if (faceRight()) {
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceLeft()) {
				moveToNextGrid(-getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceFront()) {
				getPilot().rotate(90);
				direction_flag++;
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
			if (faceBack()) {
				getPilot().rotate(-90);
				direction_flag--;
				moveToNextGrid(getMap().getOneGridWidth(), getMap().getOneGridLength());
			}
		} else if (start.getYCoordinate() < end.getYCoordinate()) {
			if (faceFront()) {
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceRight()) {
				getPilot().rotate(-90);
				direction_flag--;
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceLeft()) {
				getPilot().rotate(90);
				direction_flag++;
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceBack()) {
				moveToNextGrid(-getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
		} else if (start.getYCoordinate() > end.getYCoordinate()) {
			if (faceBack()) {
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceRight()) {
				getPilot().rotate(90);
				direction_flag++;
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceFront()) {
				moveToNextGrid(-getMap().getOneGridLength(), getMap().getOneGridWidth());
			}
			if (faceLeft()) {
				getPilot().rotate(-90);
				direction_flag--;
				moveToNextGrid(getMap().getOneGridLength(), getMap().getOneGridWidth());
			}

		}
	}

}