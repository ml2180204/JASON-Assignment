import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class RRComm{
	private static final String server_ip = "172.20.1.139";
    // port is 8888
    private static final int port = 8888; 
    private Socket socket;
    // define I/O
    private BufferedReader in;
    private PrintStream out;
    
    public RRComm(RRModel model, RREnv env) throws Exception {  
    	socket = new Socket(server_ip, port); 
        System.out.print("connect");
    }
    
    public void sendToRobot(String command) throws IOException {
    	out = new PrintStream(socket.getOutputStream());
    	out.println(command);
    	out.flush();
    }
    
    public String readFromRobot() throws IOException {
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
    	String ret = in.readLine();
    	while (ret==null) {
    	}
    	return ret;
    }
    
	public synchronized void close() {
		if (socket.isClosed()) {
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
