// Agent doctor in project qunidaye

/* Initial beliefs and rules */

/* Initial goals */

!localization(slots).

/* Plans */

+!start : true <- .print("hello world.").

+!localization(slots) : not found_location(scout)
	<-  if(not active[source(A)]){
		.print("ask scout to move to next possible location");
		.send(scout, achieve, next(slot));
		}
		.wait(2000);
		!!localization(slots).
	   
+!localization(slots): found_location(scout)
	<-  .print("Found location and update to robot")
		update(robot);
		.wait(3000);
		!!find(victim).
	
+!find(victim) : found_all_victim
	<- remove(ps_victim);
		.print("found all victims").
		
+!find(victim)
	<- !!astar(victim).

+!astar(victim): atVictim
    <- !!check(slot).
+!astar(victim): not atVictim
	<- ?nextSquare(X,Y);
		if(not active[source(A)]){
			.print("ask scout go to(",X,",",Y,")");
			.send(scout,achieve,goto(X,Y));
		}
	   .wait(2000);
	   !!astar(victim).
	   
+!check(slot)
	<- if(not active[source(A)]){
		.print("at victim area, ask scout to check the priority");
		.send(scout,achieve,check(slot));
		}
	.wait(2000);
	!!find(victim).
	
