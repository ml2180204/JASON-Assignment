import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RREnv extends Environment {
	
	private final static int ARENA_LENGTH = 195;
	private final static int ARENA_WIDTH = 150;
	private final static int GRID_LENGTH = 32; // Value taken from assignment webpage
	private final static int GRID_WIDTH = 30; // Value taken from assignment webpage
	
    public static final int row = 7; // grid size
    public static final int column = 6; // grid size
    public static final int GARB  = 16; // garbage code in grid model
    public static final int POSSIBLE_LOCATION = 32;
    public static final int POSSIBLE_HEAD = 64;
    public static final int GREENVIC= 128;
    public static final int BLUEVIC = 256;
    public static final int REDVIC = 512;
    public static final int POSSIBLE_VIC = 1024;
    
    public static final Literal fl = Literal.parseLiteral("found_location(scout)");
    public static final Literal fv = Literal.parseLiteral("found_all_victim");
    public static final Literal at = Literal.parseLiteral("atVictim");
    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    dto = Literal.parseLiteral("detect(obstacles)");
    public static final Term    ur = Literal.parseLiteral("update(robot)");
    public static final Term    cs = Literal.parseLiteral("check(slot)");
    public static final Term    rp = Literal.parseLiteral("remove(ps_victim)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");

    static Logger logger = Logger.getLogger(RREnv.class.getName());

    private RRModel model;
    private RRView view;
    private RRComm comm;
    @Override
    public void init(String[] args) {
    	model = new RRModel(ARENA_LENGTH, ARENA_WIDTH, GRID_LENGTH, GRID_WIDTH, comm);
       // model = new MarsModel();
        view  = new RRView(model);
        model.setView(view);
//        try {
//			comm = new RRComm(model,this);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        updatePercepts();	
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
//              model.nextSlot();
            	model.sendRobotBehavior();
            } else if (action.equals(dto)){
            	comm.sendToRobot("DETECT_OBSTACLES");
            } else if (action.equals(ur)){
            	String dx = String.valueOf(model.getAgPos(0).x);
            	String dy = String.valueOf(model.getAgPos(0).y);
            	String dh = String.valueOf(model.scoutHead);
                comm.sendToRobot("UPDATE_ROBOT"+dx+","+dy+","+dh);
            } else if (action.getFunctor().equals("goto")) {
            	int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                String dx = String.valueOf(x);
                String dy = String.valueOf(y);
//              model.moveTo(x, y);
                comm.sendToRobot("MOVE"+dx+","+dy);
            } else if (action.equals(cs)) {
//            	model.checkVictim();
            	comm.sendToRobot("CHECK_VICTIM");
            } else if (action.equals(rp)) {
            	model.ps_victim.clear();
            }
//            } else if (action.getFunctor().equals("move_towards")) {
//                int x = (int)((NumberTerm)action.getTerm(0)).solve();
//                int y = (int)((NumberTerm)action.getTerm(1)).solve();
//                model.moveTowards(x,y);
//            } else if (action.equals(pg)) {
//                model.pickGarb();
//            } else if (action.equals(dg)) {
//                model.dropGarb();
//            } else if (action.equals(bg)) {
//                model.burnGarb();
//            } else {
//                return false;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
//        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();
        
        boolean foundLocation = model.ps_square.size()== 1 
        			&& model.ps_square.get(0).getHeadList().size()== 1;
        boolean foundAllVictim = model.found_victim.size()== 3;
        boolean atVictim = model.getAgPos(0)!= null 
        			&& model.hasObject(POSSIBLE_VIC, model.getAgPos(0));
       
        if(model.detected_obstacles) {
        	Literal p = Literal.parseLiteral("detected_obstacles");
         	addPercept(p);
        }
//        if(model.detected_color) {
//        	Literal p = Literal.parseLiteral("detected_color");
//         	addPercept(p);
//        }
        
        if (foundLocation) {
        	addPercept(fl);
        	Literal nextSquare = Literal.parseLiteral("nextSquare("+ model.getNextSquare().getXCoordinate()
        			+ "," + model.getNextSquare().getYCoordinate()+")");
        	addPercept(nextSquare);
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

 
