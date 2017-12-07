// Agent doctor in project qunidaye

/* Initial beliefs and rules */

/* Initial goals */

!localization(slots).

/* Plans */

+!start : true <- .print("hello world.").

+!localization(slots) : not found_location(scout)
	<- next(slot);
		.print("w");
		.wait(400);
	   !!localization(slots).
+!localization(slots): found_location(scout)
	<- !!find(victim).
	
+!find(victim) : found_all_victim
	<- remove(ps_victim);
		.print("found all victims").
		
+!find(victim)
	<- !!astar(victim).

+!astar(victim): atVictim
    <- !!check(slot).
+!astar(victim): not atVictim
	<- goto(victim);
	   !!astar(victim).
	   
+!check(slot)
	<- check(slot);
	!!find(victim).