// Agent scout in project qunidaye

/* Initial beliefs and rules */
finishicheck :- pos(P,X,Y) & pos(r1,X,Y).
~checkFront.

/* Initial goals */
	

/* Plans */

+!next(slot)[source(A)]
	<-	.send(doctor,tell,active);
		.print("Move to next possible location...");
		!!detect(obstacles);
		next(slot);
		.drop_desire(next(slot)[source(A)]);
		.send(doctor,untell,active).
		
+!update(robot) : found_location(scout)
	<-	.print("Found location and update to robot")
		update(robot);
		.wait(500).
		
+!detect(obstacles): not detected_obstacles
	<-  .print("Detect adjacency grids...");
		detect(obstacles);
		!!detect(obstacles).
		

+!goto(X,Y)[source(A)]
	<-  .send(doctor,tell,active);
		goto(X,Y);
		.drop_desire(goto(X,Y)[source(A)]);
		.send(doctor,untell,active).
		
+!check(slot)[source(A)]
	<-  .send(doctor,tell,active);
		check(slot);
//		!!detect(color);
		?color(X);
		.print("The priority color is ",X);
		.drop_desire(check(slot)[source(A)]);
		.send(doctor,untell,active).
		
//+!detect(color): not detected_color
//	<-	.print("Detect color...");
//		detect(color);
//		!!detect(color).
