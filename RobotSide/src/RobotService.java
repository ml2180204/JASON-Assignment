import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;

public class RobotService {
	
    private static final int port = 8888;
    private boolean serverClosed = false;
    private BufferedReader in;
    private PrintStream out;
    private boolean first = true;
    
    private Robot robot;
    private Socket socket;
    private ServerSocket serverSocket;
    
	public RobotService(Robot robot) throws Exception {  
		this.robot = robot;
        serverSocket = new ServerSocket(port);
        System.out.println("Server running..");
        socket = serverSocket.accept();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());
    }
	
	public static void main(String[] args) {

		Robot robot = new Robot();
		
		try {
			RobotService rs = new RobotService(robot);
			rs.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load() throws IOException {
		while(!serverClosed) {
			try {
				String msg = in.readLine();
				if(msg.startsWith("MOVE")) {
					String[] pos = msg.substring(4).split(",");
					System.out.println("MOVE_TO("+pos[0]+","+pos[1]+")");
					int x = Integer.parseInt(pos[0]);
					int y = Integer.parseInt(pos[1]);
					robot.Move(robot.robot_x,robot.robot_y,x,y);
					out.println("UPDATE_MOVE");
					out.flush();
				} else if(msg.startsWith("UPDATE_ROBOT")) {
					String[] info = msg.substring(12).split(",");
					System.out.println("UPDATE_LOCATION");
					
					int x = Integer.parseInt(info[0]);
					int y = Integer.parseInt(info[1]);
					int head = Integer.parseInt(info[2]);
					robot.direction_flag = head;
					robot.robot_x = x;
					robot.robot_y = y;
					out.println("DONE");
					out.flush();
				}
				switch(msg) {
				case "DETECT_OBSTACLES":
					System.out.println("DETECT_OBSTACLES");
					String front ="F", left="F", right="F", back = "F";
					robot.detectObstacles(first);
					if(robot.obstacles[0] ==true) {
						front ="T";
					}
					if(robot.obstacles[1] ==true) {
						left ="T";
					}
					if(robot.obstacles[2] ==true) {
						right ="T";
					}
					if(first) {
						if(robot.obstacles[3] ==true) {
							back ="T";
						}
					}
					first = false;
					out.println("DETECTED_OBSTACLES"+","+front+","+left+","+right+","+back);
					out.flush();
					break;
				case "GO_AHEAD":
					System.out.println(msg);
					robot.goAhead();
					out.println("DONE");
					out.flush();
					break;
				case "GO_LEFT":
					System.out.println(msg);
					robot.goLeft();
					out.println("DONE");
					out.flush();
					break;
				case "GO_RIGHT":
					System.out.println(msg);
					robot.goRight();
					out.println("DONE");
					out.flush();
					break;
				case "GO_BACK":
					System.out.println(msg);
					robot.goBack();
					out.println("DONE");
					out.flush();
					break;
				case "CHECK_VICTIM":
					String color = "empty";
					if(robot.isRed()) {
						color = "red";
					} else if (robot.isBlue()) {
						color = "blue";
					} else if (robot.isGreen()) {
						color = "green";
					}
		
					System.out.println("DETECTED_COLOR: "+color);
					out.println("DETECTED_COLOR"+","+color);
					out.flush();	
					break;
				}
			} catch (Exception e) {
				socket.close();
				serverSocket.close();
				System.out.println("Press Escape to quit");
				while(!Button.ESCAPE.isDown()) {
				}
			} 
			
		}
	}
}
