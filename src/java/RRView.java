import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import jason.environment.grid.GridWorldView;

public class RRView extends GridWorldView {
		public RRView(RRModel model) {
            super(model, "Recure Robot World", 700);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }
        /** draw application objects */
        @Override		
        public void draw(Graphics g, int x, int y, int object) {
        	Color c;
            switch (object) {
                case RREnv.GARB: drawGarb(g, x, y);  break;
                case RREnv.POSSIBLE_LOCATION: c = Color.YELLOW; drawLocation(g, x, y, c); break;
                case RREnv.POSSIBLE_VIC: drawPsVictim(g, x, y); break;
                case RREnv.REDVIC: drawRedVictim(g, x, y); break;
                case RREnv.BLUEVIC: drawBlueVictim(g, x, y); break;
                case RREnv.GREENVIC: drawGreenVictim(g, x, y); break;
                
              //  case RREnv.POSSIBLE_HEAD: drawHead(g, x, y); break;
            }
        }

        private void drawPsVictim(Graphics g, int x, int y) {
        	 g.setColor(Color.LIGHT_GRAY);
             drawString(g, x, y, defaultFont, "V");
		}
        
        private void drawRedVictim(Graphics g, int x, int y) {
       	 	g.setColor(Color.red);
       	 	g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
		}
        private void drawBlueVictim(Graphics g, int x, int y) {
       	 	g.setColor(Color.blue);
       	 	g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
		}
        private void drawGreenVictim(Graphics g, int x, int y) {
       	 	g.setColor(Color.green);
       	 	g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
		}


        
		@Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R";
            c = Color.yellow;
            super.drawAgent(g, x, y, c, -1);
            c = Color.orange;
            drawLocation(g, x ,y, c);
            if (id == 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);                
            }
            super.drawString(g, x, y, defaultFont, label);
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }
        

        public void drawLocation(Graphics g, int x, int y, Color c) {
            g.setColor(c);
            for (int i=0; i<RRModel.ps_square.size(); i++) {
            	if(RRModel.ps_square.get(i).getXCoordinate()==x &&
            		RRModel.ps_square.get(i).getYCoordinate()==y ) {
            		for(int j=0; j<RRModel.ps_square.get(i).getHeadList().size(); j++) {
            			if(RRModel.ps_square.get(i).getHeadList().get(j)%4==0) {
            				int px[] = {x*cellSizeW+(int)(cellSizeW*0.35),x*cellSizeW+(int)(cellSizeW*0.5),x*cellSizeW+(int)(cellSizeW*0.65)};
            				int py[] = {y*cellSizeH+(int)(cellSizeH*0.3),y*cellSizeH+(int)(cellSizeH*0.1),y*cellSizeH+(int)(cellSizeH*0.3)};
            				g.fillPolygon(px, py, px.length);
            			}
            			if(RRModel.ps_square.get(i).getHeadList().get(j)%4==1 ||
            				RRModel.ps_square.get(i).getHeadList().get(j)%4==-3 ) {
            				int px[] = {x*cellSizeW+(int)(cellSizeW*0.7),x*cellSizeW+(int)(cellSizeW*0.9),x*cellSizeW+(int)(cellSizeW*0.7)};
            				int py[] = {y*cellSizeH+(int)(cellSizeH*0.35),y*cellSizeH+(int)(cellSizeH*0.5),y*cellSizeH+(int)(cellSizeH*0.65)};
            				g.fillPolygon(px, py, px.length);
            			}
            			if(RRModel.ps_square.get(i).getHeadList().get(j)%4==-1 ||
            				RRModel.ps_square.get(i).getHeadList().get(j)%4==3 ) {
            				int px[] = {x*cellSizeW+(int)(cellSizeW*0.3),x*cellSizeW+(int)(cellSizeW*0.1),x*cellSizeW+(int)(cellSizeW*0.3)};
            				int py[] = {y*cellSizeH+(int)(cellSizeH*0.35),y*cellSizeH+(int)(cellSizeH*0.5),y*cellSizeH+(int)(cellSizeH*0.65)};
            				g.fillPolygon(px, py, px.length);
            			}
            			if(RRModel.ps_square.get(i).getHeadList().get(j)%4==2 ||
            				RRModel.ps_square.get(i).getHeadList().get(j)%4==-2 ) {
            				int px[] = {x*cellSizeW+(int)(cellSizeW*0.35),x*cellSizeW+(int)(cellSizeW*0.5),x*cellSizeW+(int)(cellSizeW*0.65)};
            				int py[] = {y*cellSizeH+(int)(cellSizeH*0.7),y*cellSizeH+(int)(cellSizeH*0.9),y*cellSizeH+(int)(cellSizeH*0.7)};
            				g.fillPolygon(px, py, px.length);
            			}
            		}       		            		
            	}
            }
        }

     
    } 