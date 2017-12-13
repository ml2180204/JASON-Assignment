import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldView;

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
    public static final Term    gt = Literal.parseLiteral("goto(victim)");
    public static final Term    cs = Literal.parseLiteral("check(slot)");
    
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");

    static Logger logger = Logger.getLogger(RREnv.class.getName());

    private RRModel model;
    private RRView view;
    @Override
    public void init(String[] args) {
    	model = new RRModel(ARENA_LENGTH, ARENA_WIDTH, GRID_LENGTH, GRID_WIDTH);
       // model = new MarsModel();
        view  = new RRView(model);
        model.setView(view);
        updatePercepts();	
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.equals(gt)) {
            	model.goToVcitim();
            } else if (action.equals(cs)) {
            	model.checkVictim();
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
        
        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();
        
        if (model.ps_square.size()==1 && model.ps_square.get(0).getHeadList().size()==1) {
        	addPercept(fl);
        }
        if (model.found_victim.size()==3) {
        	addPercept(fv);
        }
        if (model.getAgPos(0)!=null && model.hasObject(POSSIBLE_VIC, model.getAgPos(0))){
        	addPercept(at);
        } 
    }
}

 
