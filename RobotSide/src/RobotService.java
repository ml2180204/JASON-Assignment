import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RobotService extends ServerSocket{
	
    private static final int port = 8888;
    private boolean serverClosed = false;
    private static BufferedReader in;
    private static PrintStream out;
    private boolean first = true;
    
    private Robot robot;
    
	public RobotService(Robot robot) throws Exception {  
        super(port);  
        this.robot = robot;
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

	private void load() {
		Socket socket = null;
		while(!serverClosed) {
			try {
				synchronized (this) {
					socket = this.accept();
				}
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg = in.readLine();
				if(msg.startsWith("MOVE")) {
					String[] pos = msg.substring(5).split(",");
					System.out.println("MOVE_TO("+pos[0]+","+pos[1]+")");
					int x = Integer.parseInt(pos[0]);
					int y = Integer.parseInt(pos[1]);
					robot.Move(robot.robot_x,robot.robot_y,x,y);
					out.println("UPDATE_MOVE");
					out.flush();
				}
				if(msg.startsWith("UPDATE_ROBOT")) {
					String[] info = msg.substring(13).split(",");
					System.out.println("UPDATE_LOCATION_INFO");
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
					System.out.println("START_DETECT_OBSTACLES");
					String front ="F", left="F", right="F", back = "F";
					if(robot.detectObstacles(first)[0] ==true) {
						front ="T";
					}
					if(robot.detectObstacles(first)[1] ==true) {
						left ="T";
					}
					if(robot.detectObstacles(first)[2] ==true) {
						right ="T";
					}
					if(first) {
						if(robot.detectObstacles(first)[3] ==true) {
							back ="T";
						}
					}
					first = false;
					out.println("DETECTED_OBSTACLES"+"+"+front+"+"+left+"+"+right+"+"+back);
					out.flush();
				case "GO_AHEAD":
					System.out.println(msg);
					robot.goAhead();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_LEFT":
					System.out.println(msg);
					robot.goLeft();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_RIGHT":
					System.out.println(msg);
					robot.goRight();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_BACK":
					System.out.println(msg);
					robot.goBack();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "CHECK_VICTIM":
					String color = "empty";
					if(robot.getColour()[0]>0.15f) {
						color = "red";
					} else if (robot.getColour()[0] > 0.2f) {
						color = "blue";
					} else if (robot.getColour()[0] < 0.1f) {
						color = "green";
					}
					System.out.println("DETECTED_COLOR: "+color);
					out.println("DETECTED_COLOR"+"+"+color);
					out.flush();			
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
	}
}
