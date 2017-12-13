import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RobotServer extends ServerSocket{
	
	private final static int ARENA_LENGTH = 195;
	private final static int ARENA_WIDTH = 150;
	private final static int GRID_LENGTH = 32; // Value taken from assignment webpage
	private final static int GRID_WIDTH = 30; // Value taken from assignment webpage
	
    private static final int port = 8888;
    private boolean serverClosed = false;
    private static BufferedReader in;
    private static PrintStream out;
    private boolean first = true;
    
    private Robot robot;
    
	public RobotServer(Robot robot) throws Exception {  
        super(port);  
        this.robot = robot;
    }
	
	public void main(String[] args) {
		Map map = new Map(ARENA_LENGTH, ARENA_WIDTH, GRID_LENGTH, GRID_WIDTH);
		Robot robot = new Robot(map);
		RobotMonitor myMonitor = new RobotMonitor(robot, 400);
		myMonitor.start();
		
		try {
			RobotServer rs = new RobotServer(robot);
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
					int x = Integer.parseInt(pos[0]);
					int y = Integer.parseInt(pos[1]);
					robot.Move(robot.getMap().getRobotSquare(), robot.getMap().getGridSquare(x, y));
					out.println("UPDATE_MOVE"+pos[0]+","+pos[1]);
					out.flush();
				}
				if(msg.startsWith("UPDATE_ROBOT")) {
					String[] info = msg.substring(13).split(",");
					int x = Integer.parseInt(info[0]);
					int y = Integer.parseInt(info[1]);
					int head = Integer.parseInt(info[2]);
					robot.direction_flag = head;
					robot.getMap().setRobotSquare(robot.getMap().getGridSquare(x, y));
				}
				switch(msg) {
				case "DETECT_OBSTACLES":
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
					robot.goAhead();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_LEFT":
					robot.goLeft();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_RIGHT":
					robot.goRight();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "GO_BACK":
					robot.goBack();
					out.println("UPDATE_POSSIBLE_LOCATION");
					out.flush();
				case "CHECK_VICTIM":
					String color = "empty";
					if(robot.getColour()[0]==2.0f) {
						color = "red";
					} else if (robot.getColour()[0] == 3.0f) {
						color = "blue";
					} else if (robot.getColour()[0] == 4.0f) {
						color = "green";
					}
					out.println("DETECTED_COLOR"+"+"+"green");
					out.flush();			
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
	}
}
