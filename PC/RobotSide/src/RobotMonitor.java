// RobotMonitor.java
// Used to write messages and display map to EV3 LCD Screen

import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

public class RobotMonitor extends Thread {

	private int delay;
	public Robot robot;
	private String msg;

	private char EMPTY = 'E';
	private char ORIGIN = 'O';
	private char ROBOT = 'R';
	private char FINISH = 'F';

	GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();

	// Make the monitor a daemon and set
	// the robot it monitors and the delay
	public RobotMonitor(Robot r, int d) {
		this.setDaemon(true);
		delay = d;
		robot = r;
		msg = "";
	}

	// Allow extra messages to be displayed
	public void resetMessage() {
		this.setMessage("");
	}

	// Clear the message that is displayed
	public void setMessage(String str) {
		msg = str;
	}

	// Draws a grid showing the occupancy probability of each square
	// Except the square the robot occupies
	public void textGrid(Map map) {
		lcd.clear();
		int xOffset = 25;
		int yOffset = 0;
		DecimalFormat df = new DecimalFormat("0.0");

		for (int y = 0; y < map.getRows(); y++) {

			for (int x = 0; x < map.getColumns(); x++) {
				if (map.getGridSquare(x, y) != map.getRobotSquare()) {
					if (map.getGridSquare(x, y).getScanCount() != 0) {
						String probability = df.format(map.getGridSquare(x, y).occupancyProbability());
						lcd.drawString(probability, xOffset + x * 23, yOffset + y * 23, 20);
					} else {
						lcd.drawString("0.0", xOffset + x * 23, yOffset + y * 23, 20);
					}
				} else {
					lcd.drawChar(ROBOT, xOffset + x * 23, yOffset + y * 23, 20);
				}

			}

		}

	}

	// Draw a graphical grid
	public void graphicalGrid(Map map) {
		lcd.clear();
		int xOffset = 50;
		int yOffset = 15;
		int scale = 2;

		// Draw arena outline
		lcd.drawRect(xOffset, yOffset, map.getWidth() / scale, map.getLength() / scale);

		int totalY = 0;
		int y = 0;
		// Draw individual grid squares
		for (y = 0; y < map.getRows(); y++) {

			int totalX = 0;
			int x;
			for (x = 0; x < map.getColumns(); x++) {
				// Gridsquare contains an obstacle
				if (map.getGridSquare(x, y).isOccupied()) {
					lcd.fillRect(xOffset + totalX, yOffset + totalY, map.getGridSquare(x, y).getGridWidth() / scale,
							map.getGridSquare(x, y).getGridLength() / scale);
				} else {
					// Draw empty Rectangle
					lcd.drawRect(xOffset + totalX, yOffset + totalY, map.getGridSquare(x, y).getGridWidth() / scale,
							map.getGridSquare(x, y).getGridLength() / scale);
					// Gridsquare contains the finishing point
					if (map.getGridSquare(x, y) == map.getEndSquare()) {
						lcd.drawChar(FINISH, xOffset + 2 + totalX, yOffset + 2 + totalY, 30);
						// Gridsquare contains the origin point
					} else if (map.getGridSquare(x, y) == map.getOriginSquare()) {
						lcd.drawChar(ORIGIN, xOffset + 2 + totalX, yOffset + 2 + totalY, 30);
						// Gridsquare is occupied by robot
					} else if (map.getGridSquare(x, y) == map.getRobotSquare()) {
						lcd.drawChar(ROBOT, xOffset + 2 + totalX, yOffset + 2 + totalY, 30);
						// Gridsquare is empty
					} else {
						lcd.drawChar(EMPTY, xOffset + 2 + x * 16, yOffset + 2 + y * 16, 30);
					}
				}
				totalX = totalX + map.getGridSquare(x, y).getGridWidth() / scale;

			}
			x--;
			totalY = totalY + map.getGridSquare(x, y).getGridLength() / scale;
		}

	}

	// The monitor writes various bits of robot state to the screen, then
	// sleeps.
	public void run() {

		boolean graphical = true;

		while (true) {

			lcd.setFont(Font.getSmallFont());

			lcd.drawString(msg, 0, 100, 0);
			// Press enter button to toggle between Graphical and Text based grid
			if (Button.ENTER.isDown()) {
				// This is an xor gate that flips the variable value
				graphical ^= true;
			}

			if (graphical == true) {
				// Draw graphical grid map
				graphicalGrid(robot.getMap());
			} else {
				// Draw textual grid map
				textGrid(robot.getMap());
			}

			try {
				sleep(delay);
			} catch (Exception e) {

			}
		}
	}

}