import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class RRComm extends Socket{
	private static final String server_ip = "172.20.1.155";
    // port is 8888
    private static final int port = 8888; 

    // check if the socket is closed or not
    private boolean socketClosed = false;
    // define I/O
    private static BufferedReader in;
    private static PrintStream out;
    
    public RRComm(RRModel model, RREnv env) throws Exception {  
        super(server_ip, port);  
        System.out.print("connect");
    }
    
    public void sendToRobot(String command) throws IOException {
    	out = new PrintStream(this.getOutputStream());
    	out.println(command);
    	out.flush();
    	out.close();
    }
    
    public String readFromRobot() throws IOException {
    	in = new BufferedReader(new InputStreamReader(this.getInputStream())); 
    	String ret = in.readLine();
    	while (ret==null) {
    	}
    	return ret;
    }
}
