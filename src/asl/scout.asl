// Agent scout in project qunidaye

/* Initial beliefs and rules */
finishicheck :- pos(P,X,Y) & pos(r1,X,Y).
~checkFront.

/* Initial goals */

!start.

/* Plans */

+!start : true <- .print("hello world.").


+checkFront : not checkfront
	<-	scan(_);
		+checkfront.