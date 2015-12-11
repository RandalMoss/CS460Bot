package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.BotUtils;
import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Pub;
import com.brianstempin.vindiniumclient.dto.GameState;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Decides the best way to get healed.
 *
 * This decisioner will do its best to steer the bot towards a tavern without confrontation.
 *
 * On the Maslow Hierarchy, this falls under safety.
 */
public class HealDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove> {

    private static final Logger logger = LogManager.getLogger(HealDecisioner.class);

    @Override
    public BotMove makeDecision(AdvancedMurderBot.GameContext context) {
        logger.info("Need to heal; running to nearest pub.");

//        Map<GameState.Position, AdvancedMurderBot.DijkstraResult> dijkstraResultMap = context.getDijkstraResultMap();
//
//        // Run to the nearest pub
//        Pub nearestPub = null;
//        AdvancedMurderBot.DijkstraResult nearestPubDijkstraResult = null;
//        for(Pub pub : context.getGameState().getPubs().values()) {
//            AdvancedMurderBot.DijkstraResult dijkstraToPub = dijkstraResultMap.get(pub.getPosition());
//            if(dijkstraToPub != null) {
//                if(nearestPub == null || nearestPubDijkstraResult.getDistance() >
//                    dijkstraToPub.getDistance()) {
//                    nearestPub = pub;
//                    nearestPubDijkstraResult = dijkstraResultMap.get(pub.getPosition());
//                }
//            }
//        }
//
//        if(nearestPub == null)
//            return BotMove.STAY;
//
//        // TODO How do we know that we're not walking too close to a foe?
//        GameState.Position nextMove = nearestPub.getPosition();
//        while(nearestPubDijkstraResult.getDistance() > 1) {
//            nextMove = nearestPubDijkstraResult.getPrevious();
//            nearestPubDijkstraResult = dijkstraResultMap.get(nextMove);
//        }
//
//        return BotUtils.directionTowards(nearestPubDijkstraResult.getPrevious(), nextMove);
        GameState.Position myPosition = context.getGameState().getMe().getPos();
        Map<GameState.Position, Pub> pubs = context.getGameState().getPubs();
        Map<GameState.Position, Hero> enemies = context.getGameState().getHeroesByPosition();
        Map<GameState.Position, AdvancedMurderBot.DijkstraResult> dijkstraResultMap = context.getDijkstraResultMap();
        AdvancedMurderBot.DijkstraResult nearestPubDijkstraResult = null;
        Pub bestPub = null;
        int evaluation = Integer.MAX_VALUE;
        for(Pub pub : pubs.values()){
        	int pubEval = 0;
        	GameState.Position pubPos = pub.getPosition();
        	pubEval += getEuclideanDistance(myPosition.getX(), myPosition.getY(), pubPos.getX(), pubPos.getY());
        	
        	int enemyEval = 1000;
        	for(Hero enemy : enemies.values()){
        		if(enemy.getPos().equals(myPosition)){ //skip us
        			continue;
        		}
        		else{
        			int mineToEnemyDistance = getEuclideanDistance(pub.getPosition().getX(), pub.getPosition().getY(), enemy.getPos().getX(), enemy.getPos().getY());
        			if(mineToEnemyDistance <= enemyEval){
        				enemyEval = mineToEnemyDistance;
        			}
        		}
        	}
        	pubEval -= enemyEval + enemyEval;
        	if(pubEval <= evaluation){
        		bestPub = pub;
        		evaluation = pubEval;
        		nearestPubDijkstraResult = dijkstraResultMap.get(pub.getPosition());
        	}
        }
        
        AdvancedMurderBot.DijkstraResult currentResult = nearestPubDijkstraResult;
        GameState.Position currentPosition = bestPub.getPosition();

        while(currentResult.getDistance() > 1) {
            currentPosition = currentResult.getPrevious();
            currentResult = dijkstraResultMap.get(currentPosition);
        }
        if(context.getGameState().getMe().getLife() < 70){
        	logger.info("Going to nearest pub farthest away from enemies!");
        	return BotUtils.directionTowards(context.getGameState().getMe().getPos(), currentPosition);
        }
        else{
        	CowardDecisioner cd = new CowardDecisioner();
        	return cd.makeDecision(context);
        }
    }
    
	private int getEuclideanDistance(int playerX, int playerY, int x, int y){
		return (int)Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));
	}
}
