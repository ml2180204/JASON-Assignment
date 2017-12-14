// Agent doctor in project qunidaye

/* Initial beliefs and rules */

mapObs(1,0).
mapObs(2,3).
mapObs(0,0).
psVictim(4,3).
psVictim(4,0).
psVictim(0,4).
psVictim(2,0).
psVictim(4,4).
needUpdateObs:- mapObs(_,_).
needUpdateVic:- psVictim(_,_).
/* Initial goals */

!updateObs(map).
!updateVic(map).

/* Plans */

+!start : true <- .print("hello world.").

+!updateObs(map): needUpdateObs
	<- ?mapObs(X,Y);
	   initObs(X,Y);
	   -mapObs(X,Y);
	   !!updateObs(map).
	   
+!updateObs(map): not needUpdateObs
	<- .print("obstacles update finished");
		!finishUpdate(map).
		
+!updateVic(map): needUpdateVic
	<- ?psVictim(X,Y);
	   initVictim(X,Y);
	   -psVictim(X,Y);
	   !!updateVic(map).
	   
+!updateVic(map): not needUpdateVic
	<- .print("possible victim area update finished");
		!finishUpdate(map).
			      
+!finishUpdate(map): not needUpdateObs & not needUpdateVic 
	<-  .print("map update finished")
		!!localization(slots).
		
+!finishUpdate(map): needUpdateObs | needUpdateVic
	<- .print("continue...").
		
+!localization(slots) : not found_location(scout)
	<-  if(not active[source(A)]){
		.print("ask scout to move to next possible location");
		.send(scout, achieve, next(slot));
		}
		.wait(300);
		!!localization(slots).
		
+obs(X)[source(A)]
	<- .print(A, " tells me the ", X , "direction is an obstacle");
		-obs(X)[source(A)].
	
+!localization(slots): found_location(scout)
	<-  .wait(300);
		!!find(victim).
	
+!find(victim) : found_all_victim
	<- remove(ps_victim);
		.print("found all victims").
		
+!find(victim)
	<- !!astar(victim).

+!astar(victim): atVictim
    <- !!check(slot).
+!astar(victim): not atVictim
	<-  ?nextSquare(X,Y);
		if(not active[source(A)]){
			.print("ask scout go to(",X,",",Y,")");
			.send(scout,achieve,goto(X,Y));
		}
	   .wait(300);
	   !!astar(victim).
	   
+!check(slot)
	<- if(not active[source(A)]){
		.print("at victim area, ask scout to check the priority");
		.send(scout,achieve,check(slot));
		}
	.wait(300);
	!!find(victim).
	
+color(X)[source(A)]
	<- .print(A, " tells me the priority is ", X);
		-color(X)[source(A)].