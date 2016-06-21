package nl.dke12.bot;

import com.badlogic.gdx.math.Vector3;
import nl.dke12.controller.GameController;
import nl.dke12.controller.InputProcessor;
import nl.dke12.game.SimulationData;
import nl.dke12.game.GameWorld;
import nl.dke12.game.TestChamber;
import nl.dke12.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ajki on 23/05/2016.
 */
public class RandomAI extends SimpleAI
{

    private boolean SEMI_RANDOM = false;

    private GameController gameController;
    private Random rng;
    private TestChamber simulator;

    public RandomAI(GameWorld gameWorld, InputProcessor processor)
    {
        super(gameWorld, processor);
        rng = new Random(System.currentTimeMillis());
        this.gameController = gameWorld.getGameController();
        this.simulator = new TestChamber(gameWorld);
    }

    @Override
    public void calculateBestMove()
    {
        if(super.isCloseToHole())
        {

        }
        else if(SEMI_RANDOM)
        {
            semiRandomDecision();
        }
        else
        {
            fullyRandomsimulatedDecision();
        }
    }

    private void semiRandomDecision()
    {
        float x = 1 - rng.nextFloat() * 2;
        float y = 1 - rng.nextFloat() * 2;
        Vector3 shotdir = new Vector3(x, y, 0.8f);
        //shotdir.rotate((float) i, 0,0, 1);
        shotdir.scl(2.1540658f / shotdir.len());

        float force = rng.nextFloat();
        float height = rng.nextFloat();

        //System.out.println(String.format("added: vector:%s height: %f force: %f", shotdir, 0.5, 0.3));

        this.distance = shotdir;
        gameController.setForceMultiplier(force);
        gameController.setHeightMultiplier(height);
    }

    private void fullyRandomsimulatedDecision()
    {
        ArrayList<SimulationData> simData = new ArrayList<SimulationData>();

        //create 10 random vectors between the possible
        for (int i = 0; i < 15; i++)
        {

            float x = 1 - rng.nextFloat() * 2;
            float y = 1 - rng.nextFloat() * 2;
            Vector3 shotdir = new Vector3(x, y, 0.8f );
            shotdir.scl(2.1540658f/shotdir.len());

            //float heightmult = Math.round(rng.nextFloat() * 10) / 10;
            float heightmult = 0.6f - (rng.nextFloat() - 0.5f);
            //float forcemult = Math.round(rng.nextFloat() * 10) / 10;
            float forcemult =  1.0f - (rng.nextFloat()- 0.5f);

            simData.add(new SimulationData(
                    super.ballPosition,
                    shotdir,
                    heightmult, forcemult,
                    super.holePosition
            ));
        }

        //simulate the 15 shots

        simulator.simulateShot(simData);

        //choose the best one

        SimulationData bestSimulation = extractBestShot(simData);
        //Log.log("best sim:" +  bestSimulation);
        System.out.println("Shooting the ball with " + bestSimulation);
        this.distance = bestSimulation.getDirection();
        gameController.setForceMultiplier(bestSimulation.getForceModifier());
        gameController.setHeightMultiplier(bestSimulation.getHeightModifier());
    }

    private SimulationData extractBestShot(ArrayList<SimulationData> simData)
    {
        SimulationData bestShot = simData.get(0);
        for(int i = 1; i<simData.size(); i++)
        {
            SimulationData data = simData.get(i);
            Log.log(data.toString());
            if(data.isGotBallInHole())
            {
                return data;
            }
            if(bestShot.absDistFromHole() > data.absDistFromHole())
            {
                bestShot = simData.get(i);
            }
        }
        return bestShot;
    }

}
