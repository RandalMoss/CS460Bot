package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.BotUtils;
import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.bot.advanced.murderbot.AdvancedMurderBot.GameContext;
import com.brianstempin.vindiniumclient.dto.GameState;
import com.brianstempin.vindiniumclient.dto.GameState.Hero;

public class CowardDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove>{
	
    private final static Logger logger = LogManager.getLogger(CowardDecisioner.class);
    
//    private final Decision<AdvancedMurderBot.GameContext, BotMove> beBraveDecisioner;
    
//    public CowardDecisioner(Decision<AdvancedMurderBot.GameContext, BotMove> beBraveDecisioner){
//    	this.beBraveDecisioner = beBraveDecisioner;
//    }
	public BotMove makeDecision(AdvancedMurderBot.GameContext context) {
        GameState.Position myPosition = context.getGameState().getMe().getPos();
        Map<GameState.Position, Mine> mines = context.getGameState().getMines();
        Map<GameState.Position, Hero> enemies = context.getGameState().getHeroesByPosition();
        Map<GameState.Position, AdvancedMurderBot.DijkstraResult> dijkstraResultMap = context.getDijkstraResultMap();
        AdvancedMurderBot.DijkstraResult nearestMineDijkstraResult = null;
        Mine bestMine = mines.values().iterator().next();
        int evaluation = Integer.MAX_VALUE;
        for(Mine mine : mines.values()){
        	if(mine.getOwner() != null && mine.getOwner().getName().equals(context.getGameState().getMe().getName())){
        		continue;
        	}
        	int mineEval = 0;
        	GameState.Position minePos = mine.getPosition();
        	mineEval -= getEuclideanDistance(myPosition.getX(), myPosition.getY(), minePos.getX(), minePos.getY());
        	
        	int enemyEval = 100000;
        	for(Hero enemy : enemies.values()){
        		if(enemy.getPos().equals(myPosition)){ //skip us
        			continue;
        		}
        		else{
        			int mineToEnemyDistance = getEuclideanDistance(mine.getPosition().getX(), mine.getPosition().getY(), enemy.getPos().getX(), enemy.getPos().getY());
        			if(mineToEnemyDistance <= enemyEval){
        				enemyEval = mineToEnemyDistance;
        			}
        		}
        	}
        	mineEval += 10 - (enemyEval);
        	if(mineEval <= evaluation){
        		bestMine = mine;
        		evaluation = mineEval;
        		nearestMineDijkstraResult = dijkstraResultMap.get(bestMine.getPosition());
        	}
        }
        
        AdvancedMurderBot.DijkstraResult currentResult = nearestMineDijkstraResult;
        GameState.Position currentPosition = bestMine.getPosition();

        while(currentResult.getDistance() > 1) {
            currentPosition = currentResult.getPrevious();
            currentResult = dijkstraResultMap.get(currentPosition);
        }
        logger.info("Coward mode activate!");
        return BotUtils.directionTowards(context.getGameState().getMe().getPos(), currentPosition);
		
//		return beBraveDecisioner.makeDecision(context);
	}
	
	private int getEuclideanDistance(int playerX, int playerY, int x, int y){
		return (int)Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));
	}

}
