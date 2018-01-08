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

	private EV3MediumRegulatedMotor sensorMotor;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private OdometryPoseProvider opp;

	public float[] endLocation = new float[2];
	public int direction_flag = 0;
	public int robot_x = 0;
	public int robot_y = 0;
	public boolean [] obstacles = new boolean[4];
	private int travel_offset = 10;
	private double obs_distance_offset = 0.22;
	
	public Robot() {
		// Instantiate Brick
		Brick myEV3 = BrickFinder.getDefault();

		// Assign robot to map

		// Instantiate Motors
		leftMotor = new EV3LargeRegulatedMotor(myEV3.getPort("B"));
		rightMotor = new EV3LargeRegulatedMotor(myEV3.getPort("D"));

		// Instantiate Sensors
		rightBump = new EV3TouchSensor(myEV3.getPort("S4"));
		leftBump = new EV3TouchSensor(myEV3.getPort("S1"));
		uSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		cSensor = new EV3ColorSensor(myEV3.getPort("S2"));

		leftSP = leftBump.getTouchMode();
		rightSP = rightBump.getTouchMode();
		distSP = uSensor.getDistanceMode();
		colourSP = cSensor.getRGBMode();

		leftSample = new float[leftSP.sampleSize()]; // Size is 1
		rightSample = new float[rightSP.sampleSize()]; // Size is 1
		distSample = new float[distSP.sampleSize()]; // Size is 1
		colourSample = new float[colourSP.sampleSize()]; // Size is 3

		// Set up the wheels by specifying the diameter of the
		// left (and right) wheels in centimeters, i.e. 3.25 cm
		// the offset number is the distance between the center
		// of wheel to the center of robot, i.e. half of track width
		// NOTE: this may require some trial and error to get right!!!
		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, 3.3).offset(-4.11);
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, 3.3).offset(4.11);
		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);

		// Create new move pilot
		pilot = new MovePilot(myChassis);
		sensorMotor = new EV3MediumRegulatedMotor(myEV3.getPort("C"));

		// Create new opp
		opp = new OdometryPoseProvider(pilot);

		// set the speed of the pilot
		pilot.setAngularSpeed(40);
		pilot.setLinearSpeed(5);
		sensorMotor.setSpeed(80);
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

	// Return opp
	public OdometryPoseProvider getOpp() {
		return opp;
	}

	// update the direction_flag
	public void changeFlag(int i) {
		direction_flag += i;
	}
	
	public boolean isBlue() {
		return (getColour()[0]<0.1 && getColour()[1]<0.16 && getColour()[2]>0.14);
	}
	
	public boolean isRed() {
		return (getColour()[0]>0.15 && getColour()[1]<0.04 && getColour()[2]<0.03);
	}
	
	public boolean isGreen() {
		return (getColour()[0]<0.1 && getColour()[1]>0.16 && getColour()[2]<0.06);
	}
	
	public boolean isBlack() {
		return (getColour()[0]<0.1 && getColour()[1] <0.1 && getColour()[2]<0.1);
	}
	
	//move behavior
	public void goAhead() {
		getPilot().travel(100000, true);
		while(!isBlack()) {
		}
		getPilot().stop();
		getPilot().travel(travel_offset);
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
	public void goNotTurnBack() {
		getPilot().travel(-100000, true);
		while(!isBlack()) {
		}
		getPilot().stop();
		getPilot().travel(-travel_offset);
	}
	
	//detect adjacency obs
	public void detectObstacles(boolean first) {
		double d = obs_distance_offset;
		if(getDistance()<d) {
			obstacles[0] =true;
		} else {
			obstacles[0] =false;
		}
		rotateSensor(90);
		if(getDistance()<d) {
			obstacles[1] =true;
		} else {
			obstacles[1] =false;
		}
		rotateSensor(-180);
		if(getDistance()<d) {
			obstacles[2] =true;
		} else {
			obstacles[2] =false;
		}
		if(first) {
			getPilot().rotate(90);
			if(getDistance()<d) {
				obstacles[3] =true;
			} else {
				obstacles[3] =false;
			}
			getPilot().rotate(-90);
		} else {
			rotateSensor(90);
		}
	}

	// one grid move to another adjacency grid
	public void Move(int x, int y, int dx, int dy) {
		if (x > dx) {
			if (faceLeft()) {
				goAhead();
			}
			if (faceRight()) {
				// getPilot().rotate(180);
				// direction_flag +=2;
				goNotTurnBack();
			}
			if (faceFront()) {
				getPilot().rotate(-90);
				goAhead();
			}
			if (faceBack()) {
				getPilot().rotate(90);
				goAhead();
			}
		} else if (x <dx) {
			if (faceRight()) {
				goAhead();
			}
			if (faceLeft()) {
				goNotTurnBack();
			}
			if (faceFront()) {
				getPilot().rotate(90);
				goAhead();
			}
			if (faceBack()) {
				getPilot().rotate(-90);
				goAhead();
			}
		} else if (y > dy) {
			if (faceFront()) {
				goAhead();
			}
			if (faceRight()) {
				getPilot().rotate(-90);
				goAhead();
			}
			if (faceLeft()) {
				getPilot().rotate(90);
				goAhead();
			}
			if (faceBack()) {
				goNotTurnBack();
			}
		} else if (y < dy) {
			if (faceBack()) {
				goAhead();
			}
			if (faceRight()) {
				getPilot().rotate(90);
				goAhead();
			}
			if (faceFront()) {
				goNotTurnBack();
			}
			if (faceLeft()) {
				getPilot().rotate(-90);
				goAhead();
			}

		}
	}

}