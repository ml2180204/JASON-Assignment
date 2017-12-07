import java.util.Random;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class MarsModel extends GridWorldModel {
        
        public static final int MErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not

        Random random = new Random(System.currentTimeMillis());
        
        public MarsModel(int l, int w, int gl, int gw) {
    		super(Math.round(w/gw),Math.round(l/gl),1);
    		
    		// Set grid dimensiosn
    		
            
            // initial location of agents
            try {
                setAgPos(0, 0, 0);
            
                Location r2Loc = new Location(4, 3);
                setAgPos(1, r2Loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // initial location of garbage
            add(MarsEnv.GARB, 3, 0);
            
        }
        
        void nextSlot() throws Exception {
			Thread.sleep(500);
            Location r1 = getAgPos(0);
            r1.x++;
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                return;
            }
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }
        
        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }
        
        void pickGarb() {
            // r1 location has garbage
            if (this.hasObject(MarsEnv.GARB, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(MarsEnv.GARB, getAgPos(0));
                    nerr = 0;
                    r1HasGarb = true;
                } else {
                    nerr++;
                }
            }
        }
        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(MarsEnv.GARB, getAgPos(0));
            }
        }
        void burnGarb() {
            // r2 location has garbage
            if (this.hasObject(MarsEnv.GARB, getAgPos(1))) {
                remove(MarsEnv.GARB, getAgPos(1));
            }
        }
    }