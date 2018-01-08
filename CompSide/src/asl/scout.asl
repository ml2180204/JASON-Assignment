// Agent scout in project qunidaye

/* Initial beliefs and rules */

/* Initial goals */
	
/* Plans */

+!detect(obstacles)[source(A)]
	<-	detect(obstacles);
		?obs(X);
		.print(X," is an obstacle");
		.send(doctor, achieve, localization(slot)).
		
+!move(slot)[source(A)] : not found_location(scout)
	<-  next(slot);
		.send(doctor, achieve, check(location)).

+!move(slot)
	<- .send(doctor, achieve, check(location)).

+!update(robot)[source(A)]
	<-  update(robot);
		.send(doctor, achieve, astar(victim)).
		
+!goto(X,Y)[source(A)]
	<-  goto(X,Y);
		.send(doctor,achieve,find(victim)).
		
+!check(slot)[source(A)]
	<-  check(slot);
		?color(X);
		.print("The priority color is ",X);
		.send(doctor,achieve,color(X)).
		
		