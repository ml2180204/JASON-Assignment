// this class is used to test without robot

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimuServer {
	
    private static final int port = 8888;
    private boolean serverClosed = false;
    private BufferedReader in;
    private PrintStream out;
    private boolean first = true;
    private int round = 1;
    private int step=1	;
    private int color_step = 1;
    
    private Socket socket;
    private ServerSocket serverSocket;
    
	public SimuServer() throws Exception {  
        serverSocket = new ServerSocket(port);
        System.out.println("Server running..");
        socket = serverSocket.accept();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());
    }
	
	public static void main(String[] args) {
		try {
			SimuServer rs = new SimuServer();
			rs.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load() throws Exception {
		while(!serverClosed&&!socket.isClosed()) {
			try {
				String msg = in.readLine();
				if(socket.isClosed()) {
					serverSocket.close();
					System.out.println("");
				}
				if(msg.startsWith("MOVE")) {
					Thread.sleep(500);
					String[] pos = msg.substring(4).split(",");
					System.out.println("MOVE_TO("+pos[0]+","+pos[1]+")");
					int x = Integer.parseInt(pos[0]);
					int y = Integer.parseInt(pos[1]);
					out.println("UPDATE_MOVE");
					out.flush();
				} else if(msg.startsWith("UPDATE_ROBOT")) {
					Thread.sleep(500);
					String[] info = msg.substring(12).split(",");
					System.out.println("UPDATE_LOCATION");
					System.out.println("position:("+info[0]+","+info[1]+")");
					System.out.println("heading: "+info[2]);
					int x = Integer.parseInt(info[0]);
					int y = Integer.parseInt(info[1]);
					int head = Integer.parseInt(info[2]);
					out.println("DONE");
					out.flush();
				}
				switch(msg) {
				case "DETECT_OBSTACLES":
					System.out.println("DETECT_OBSTACLES");
					
					String front ="F", left="F", right="F", back = "F";
					if(step==1) {
						front = "F"; left="F"; right="T"; back = "F";
					}
					if(step==2) {
						front = "F"; left="F"; right="F"; back = "F";
					}
					if(step==3) {
						front = "F"; left="F"; right="F"; back = "F";
					}
					if(step==4) {
						front = "F"; left="T"; right="T"; back = "F";
					}
					first = false;
					step++;
					out.println("DETECTED_OBSTACLES"+","+front+","+left+","+right+","+back);
					out.flush();
					break;
				case "GO_AHEAD":
					System.out.println("GO_AHEAD");
					out.println("DONE");
					out.flush();
					break;
				case "GO_LEFT":
					System.out.println(msg);
					out.println("DONE");
					out.flush();
					break;
				case "GO_RIGHT":
					System.out.println(msg);
					out.println("DONE");
					out.flush();
					break;
				case "GO_BACK":
					System.out.println(msg);
					out.println("DONE");
					out.flush();
					break;
				case "CHECK_VICTIM":
					String color = "empty";
					if(color_step==1) {
						color = "red";
					}
					if(color_step==2) {
						color = "empty";
					}
					if(color_step==3) {
						color = "blue";
					}
					if(color_step==4) {
						color = "green";
					}
					if(color_step==5) {
						color = "empty";
					}
					color_step++;
					System.out.println("DETECTED_COLOR: "+color);
					out.println("DETECTED_COLOR"+","+color);
					out.flush();	
					break;
				}
			} catch (Exception e) {
				socket.close();
				serverSocket.close();
			} 
			
		}
	}
}
