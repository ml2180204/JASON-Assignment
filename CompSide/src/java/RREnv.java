import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RREnv extends Environment {
	
	private final static int ARENA_LENGTH = 195;
	private final static int ARENA_WIDTH = 150;
	private final static int GRID_LENGTH = 32; // Value taken from assignment webpage
	private final static int GRID_WIDTH = 25; // Value taken from assignment webpage
	
    public static final int GARB  = 16;
    public static final int POSSIBLE_LOCATION = 32;
    public static final int GREENVIC= 64;
    public static final int BLUEVIC = 128;
    public static final int REDVIC = 256;
    public static final int POSSIBLE_VIC = 512;
    
    public static final Literal fl = Literal.parseLiteral("found_location(scout)");
    public static final Literal fv = Literal.parseLiteral("found_all_victim");
    public static final Literal at = Literal.parseLiteral("atVictim");
    public static final Term    ts = Literal.parseLiteral("trim(slot)");
    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    dto = Literal.parseLiteral("detect(obstacles)");
    public static final Term    ur = Literal.parseLiteral("update(robot)");
    public static final Term    cs = Literal.parseLiteral("check(slot)");
    public static final Term    fn = Literal.parseLiteral("finish(work)");

    static Logger logger = Logger.getLogger(RREnv.class.getName());

    private RRModel model;
    private RRView view;
    private RRComm comm;
    @Override
    public void init(String[] args) {
    	model = new RRModel(ARENA_LENGTH, ARENA_WIDTH, GRID_LENGTH, GRID_WIDTH);
        view  = new RRView(model);
        model.setView(view);
        try {
 			comm = new RRComm();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
        	if (action.getFunctor().equals("initObs")) {

        		int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.initObs(x, y);
        	} else if (action.getFunctor().equals("initVictim")) {

        		int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.initVictim(x, y);
        	} else if(action.equals(ts)) {
        		model.nextSlot();
        	} else if (action.equals(ns)) {
            	sendRobotBehavior();
            	String ret = comm.readFromRobot();
            } else if (action.equals(dto)){
            	comm.sendToRobot("DETECT_OBSTACLES");
            	String ret = comm.readFromRobot();
            	if(ret.startsWith("DETECTED_OBSTACLES")) {
            		String[] s = ret.substring(19).split(",");
            		for(int i=0; i<s.length; i++) {
            			if(s[i].equals("T")) {
            				model.obstacles[i] = true;
            			} else {
            				model.obstacles[i] = false;
            			}
            		}
            	}
            } else if (action.equals(ur)){
            	String dx = String.valueOf(model.getAgPos(0).x);
            	String dy = String.valueOf(model.getAgPos(0).y);
            	String dh = String.valueOf(model.scoutHead);
                comm.sendToRobot("UPDATE_ROBOT"+dx+","+dy+","+dh);
                String ret = comm.readFromRobot();
            } else if (action.getFunctor().equals("goto")) {
            	int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                String dx = String.valueOf(x);
                String dy = String.valueOf(y);
                comm.sendToRobot("MOVE"+dx+","+dy);
                String ret = comm.readFromRobot();
                if(ret.startsWith("UPDATE_MOVE")) {
            		model.moveTo(x, y);
            	}
            } else if (action.equals(cs)) {
            	comm.sendToRobot("CHECK_VICTIM");
            	String ret = comm.readFromRobot();
            	if(ret.startsWith("DETECTED_COLOR")) {
            		String[] s = ret.split(",");
            		String color = s[1];
            		model.victimColor = color;
            		model.checkVictim();
            	}
            } else if (action.equals(fn)) {
            	model.ps_victim.clear();
            	comm.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    void sendRobotBehavior() throws IOException {
		if(!model.obstacles[0]) {
			comm.sendToRobot("GO_AHEAD");
		} else if(!model.obstacles[1]) {
			comm.sendToRobot("GO_LEFT");
		} else if(!model.obstacles[2]) {
			comm.sendToRobot("GO_RIGHT");
		} else if(!model.obstacles[3]) {
			comm.sendToRobot("GO_BACK");
		}
	}
    
    /** creates the agents perception based on the RRModel */
    void updatePercepts() {
        clearPercepts();
        
        boolean foundLocation = model.ps_square.size()== 1 
        			&& model.ps_square.get(0).getHeadList().size()== 1;
        boolean foundAllVictim = model.found_victim.size()== 3;
        boolean atVictim = model.getAgPos(0)!= null 
        			&& model.hasObject(POSSIBLE_VIC, model.getAgPos(0));
       
        // update the adjacency grid information
        if(model.obstacles[0]) {
        	Literal p = Literal.parseLiteral("obs(up)");
         	addPercept(p);
        }
        if(model.obstacles[1]) {
        	Literal p = Literal.parseLiteral("obs(left)");
         	addPercept(p);
        }
        if(model.obstacles[2]) {
        	Literal p = Literal.parseLiteral("obs(right)");
         	addPercept(p);
        }
        if(model.obstacles[3]) {
        	Literal p = Literal.parseLiteral("obs(down)");
         	addPercept(p);
        }
        if(!model.obstacles[0]&&!model.obstacles[1]&&!model.obstacles[2]&&!model.obstacles[3]) {
        	Literal p = Literal.parseLiteral("obs(none)");
         	addPercept(p);
        }
        
        if (foundLocation) {
        	addPercept(fl);
        	//update the next grid need to attend
        	Literal nextSquare = Literal.parseLiteral("nextSquare("+ model.getNextSquare().getXCoordinate()
        			+ "," + model.getNextSquare().getYCoordinate()+")");
        	addPercept(nextSquare);
        	
        	//update the victim color
        	if (model.victimColor.equals("red")){
             	Literal red = Literal.parseLiteral("color(red)");
             	addPercept(red);
             } else if (model.victimColor.equals("blue")){
             	Literal blue = Literal.parseLiteral("color(blue)");
             	addPercept(blue);
             } else if (model.victimColor.equals("green")){
             	Literal green = Literal.parseLiteral("color(green)");
             	addPercept(green);
             } else {
            	Literal empty = Literal.parseLiteral("color(empty)");
              	addPercept(empty);
             }
        }
        if (foundAllVictim) {
        	addPercept(fv);
        }
        if (atVictim){
        	addPercept(at);
        } 
    }
}

 
