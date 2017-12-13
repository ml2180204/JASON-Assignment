import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class RRComm extends Socket{
	private static final String server_ip = "2";
    // port is 8888
    private static final int port = 8888; 

    // check if the socket is closed or not
    private boolean socketClosed = false;
    // define I/O
    private static BufferedReader in;
    private static PrintStream out;
    
    public RRComm(RRModel model, RREnv env) throws Exception {  
        super(server_ip, port);  

        while(!socketClosed) {
        	in = new BufferedReader(new InputStreamReader(this.getInputStream())); 
        	String ret = in.readLine();
        	if(ret.startsWith("DETECTED_COLOR")) {
        		String[] s = ret.split("+");
        		String color = s[1];
        		model.victimColor = color;
        		model.checkVictim();
        		env.updatePercepts();
        	}
        	if(ret.startsWith("UPDATE_MOVE")) {
        		String[] pos = ret.substring(12).split(",");
        		int x = Integer.parseInt(pos[0]);
        		int y = Integer.parseInt(pos[1]);
        		model.moveTo(x, y);
        		env.updatePercepts();
        	}
        	if(ret.startsWith("DETECTED_OBSTACLES")) {
        		String[] s = ret.substring(19).split("+");
        		for(int i=0; i<s.length; i++) {
        			if(s[i].equals("T")) {
        				model.obstacles[i] = true;
        			} else {
        				model.obstacles[i] = false;
        			}
        		}
        		model.detected_obstacles = true;
        		env.updatePercepts();
        	}
        	
        	if(ret.equals("UPDATE_POSSIBLE_LOCATION")) {
        		model.nextSlot();
        		model.detected_obstacles = false;
        		env.updatePercepts();
        	}
//        	switch(ret) {
//        	case "UPDATE_POSSIBLE_LOCATION":
//        		
//        	case "DETECTED_COLOR":
//        		model.detected_color = true;
//        		env.updatePercepts();
//        	case "UPDATE_MOVE":
//        		model.goToVcitim();
//        		env.updatePercepts();
//        	} 
        }
    }
    
    public void sendToRobot(String command) throws IOException {
    	out = new PrintStream(this.getOutputStream());
    	out.println(command);
    	out.flush();
    	out.close();
    }
}
