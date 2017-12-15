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

!start.

/* Plans */

+!start : not need_wait
 <- !updateObs(map);
 	!updateVic(map).

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
	
+!localization(slots): found_location(scout)
	<-  .wait(300);
		!!find(victim).
	
+!find(victim) : found_all_victim
	<- remove(ps_victim);
		.print("found all victims").
		
+!find(victim)
	<- !!astar(victim).

+!astar(victim): not atVictim
	<-  ?nextSquare(X,Y);
		if(not active[source(A)]){
			.print("ask scout go to(",X,",",Y,")");
			.send(scout,achieve,goto(X,Y));
		}
	   .wait(300);
	   !!astar(victim).
+!astar(victim): atVictim
    <- !!check(slot).
    
+!check(slot)
	<- if(not active[source(A)]){
		.print("at victim area, ask scout to check the priority");
		.send(scout,achieve,check(slot));
		}
	.wait(300);
	!!find(victim).
//	
//+color(X)[source(A)]
//	<- .print(A, " tells me the priority is ", X);
//		-color(X)[source(A)].