// Agent doctor in project qunidaye

/* Initial beliefs and rules */

mapObs(2,1).
mapObs(4,1).
mapObs(1,2).
mapObs(0,5).
mapObs(4,4).
mapObs(5,4).
psVictim(0,0).
psVictim(2,2).
psVictim(4,0).
psVictim(3,3).
psVictim(2,4).
needUpdateObs:- mapObs(_,_).
needUpdateVic:- psVictim(_,_).
/* Initial goals */

!init(map).

/* Plans */

+!init(map): needUpdateObs
	<- ?mapObs(X,Y);
	   initObs(X,Y);
	   -mapObs(X,Y);
	   !!init(map).
	   
+!init(map): needUpdateVic
	<- ?psVictim(X,Y);
	   initVictim(X,Y);
	   -psVictim(X,Y);
	   !!init(map).
			      
+!init(map)
	<-  .print("map update finished")
		!!check(location).
		
		
+!check(location): not found_location(scout)
	<- .send(scout, achieve, detect(obstacles)).
	
+!check(location)
	<- !!find(victim).

+!localization(slot)
	<-  trim(slot);
		.send(scout, achieve, move(slot)).
	
+!find(victim) : found_all_victim
	<- finish(work);
		.print("found all victims").
		
+!find(victim)
	<- !!update(robot).

+!update(robot)
	<-	.send(scout,achieve,update(robot)).


+!astar(victim): not atVictim
	<-  ?nextSquare(X,Y);
		.print("ask scout go to(",X,",",Y,")");
		.send(scout,achieve,goto(X,Y)).
	   
+!astar(victim): atVictim
    <- !!check(slot).
    
+!check(slot)
	<- .print("at victim area, ask scout to check the priority");
	.send(scout,achieve,check(slot)).
//	
//+color(X)[source(A)]
//	<- .print(A, " tells me the priority is ", X);
//		-color(X)[source(A)].