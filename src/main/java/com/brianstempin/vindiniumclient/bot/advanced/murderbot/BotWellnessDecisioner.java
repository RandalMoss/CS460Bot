package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.BotUtils;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Decides if the bot is "well" (healthy) and acts accordingly.
 *
 * This decisioner will check to make sure the bot is healthy enough to play on and act accordingly.
 *
 * On Maslow's Hierarchy of needs, this one services psychological and safety needs.
 */
public class BotWellnessDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove> {

    private static final Logger logger = LogManager.getLogger(BotWellnessDecisioner.class);

    private final Decision<AdvancedMurderBot.GameContext, BotMove> yesDecisioner;
    private final Decision<AdvancedMurderBot.GameContext, BotMove> noDecisioner;
    private final Decision<AdvancedMurderBot.GameContext, BotMove> cowardDecisioner;

    public BotWellnessDecisioner(Decision<AdvancedMurderBot.GameContext, BotMove> yesDecisioner,
                                 Decision<AdvancedMurderBot.GameContext, BotMove> noDecisioner,
                                 Decision<AdvancedMurderBot.GameContext, BotMove> cowardDecisioner) {
        this.yesDecisioner = yesDecisioner;
        this.noDecisioner = noDecisioner;
        this.cowardDecisioner = cowardDecisioner;
    }

    @Override
    public BotMove makeDecision(AdvancedMurderBot.GameContext context) {

        GameState.Hero me = context.getGameState().getMe();
        Vertex myVertex = context.getGameState().getBoardGraph().get(me.getPos());
        Map<GameState.Position, GameState.Hero> heroesByPosition = context.getGameState().getHeroesByPosition();

        // Do we have money for a pub?
        if(me.getGold() < 2) {
            // We're broke...pretend like we're healthy.
            logger.info("Bot is broke.  Fighting on even if its not healthy.");
            return yesDecisioner.makeDecision(context);
        }

        // Is the bot already next to a pub?  Perhaps its worth a drink
        for(Vertex currentVertex : myVertex.getAdjacentVertices()) {
            if(context.getGameState().getPubs().containsKey(
                    currentVertex.getPosition())) {
                if(me.getLife() < 70) {
                    logger.info("Bot is next to a pub already and could use health.");
                    return BotUtils.directionTowards(me.getPos(), currentVertex.getPosition());
                }
                GameState.Position neighboringPosition = currentVertex.getPosition();
                if(heroesByPosition.containsKey(neighboringPosition)) {
                    logger.info("Coward Bot activate!");
                    return cowardDecisioner.makeDecision(context);
                }
            }
        }

        // Is the bot well?
        if(context.getGameState().getMe().getLife() >= 30 && context.getGameState().getMe().getLife() <= 60) {
            logger.info("Bot is healthy. Coward time!");
            return cowardDecisioner.makeDecision(context);
        }
        else if(context.getGameState().getMe().getLife() > 60){
        	logger.info("Lets try to get some loot!");
        	return yesDecisioner.makeDecision(context);
        }
        else {
            logger.info("Bot is damaged.");
            return noDecisioner.makeDecision(context);
        }
    }
}
