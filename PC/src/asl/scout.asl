// Agent scout in project qunidaye

/* Initial beliefs and rules */

/* Initial goals */
	
/* Plans */

+!next(slot)[source(A)]
	<-	.send(doctor,tell,active);
		.print("Detect adjacency grids...");
		detect(obstacles);
//		?obs(X);
//		.send(doctor,tell,X);
		.print("Move to next possible location...");
		next(slot);
		.drop_desire(next(slot)[source(A)]);
		.send(doctor,untell,active).
		

+!goto(X,Y)[source(A)]
	<-  .send(doctor,tell,active);	
		.print("Update robot position and heading")
		update(robot);
		.print("Invoke robot to move to (",X,",",Y,")");
		goto(X,Y);
		.drop_desire(goto(X,Y)[source(A)]);
		.send(doctor,untell,active).
		
+!check(slot)[source(A)]
	<-  .send(doctor,tell,active);
		check(slot);
		?color(X);
		.print("The priority color is ",X);
		send(doctor,tell,color(X));
		.drop_desire(check(slot)[source(A)]);
		.send(doctor,untell,active).
		