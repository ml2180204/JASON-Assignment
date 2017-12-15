// Agent scout in project qunidaye

/* Initial beliefs and rules */

/* Initial goals */
	
/* Plans */

+!detect(obstacles)[source(A)]
	<-	.print("Detect adjacency grids...");
		detect(obstacles);
		.send(doctor, achieve, localization(slot)).
		
+!move(slot)[source(A)] : not found_location(scout)
	<- .print("Move to next possible location...");
		next(slot);
		.send(doctor, achieve, check(location)).

+!move(slot)
	<- .send(doctor, achieve, check(location)).

+!update(robot)[source(A)]
	<- .print("Update robot position and heading");
		update(robot);
		.send(doctor, achieve, astar(victim)).
		
+!goto(X,Y)[source(A)]
	<-  .print("Invoke robot to move to (",X,",",Y,")");
		goto(X,Y);
		.send(doctor,achieve,find(victim)).
		
+!check(slot)[source(A)]
	<-  check(slot);
		?color(X);
		.print("The priority color is ",X);
		.send(doctor,tell,color(X));
		.send(doctor,achieve,find(victim)).
		