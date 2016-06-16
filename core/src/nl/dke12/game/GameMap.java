package nl.dke12.game;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.math.Vector3;
import nl.dke12.bot.maze.MazeHeuristicDistance;
import nl.dke12.bot.maze.MazeMapNode;
import nl.dke12.bot.pathfinding.HeuristicMethod;
import nl.dke12.bot.pathfinding.MapGraph;
import nl.dke12.bot.pathfinding.MapNode;
import nl.dke12.util.ArrayUtil;
import nl.dke12.util.GameWorldLoader;
import nl.dke12.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by nik on 13/06/16.
 */
public class GameMap
{
    /**
     * The radius of the golf ball hole.
     */
    private final static int HOLE_RADIUS = 1; //// TODO: 13/06/16 determine actual radius

    /**
     * the amount of cells get created for every unit the game world spans
     */
    private final static int UNIT_TO_CELL_RATIO = 4;

    /**
     * stores the spawning location of the golf ball
     */
    private Vector3 startPosition;

    /**
     * stores the start node
     */
    private MapNode startNode;

    /**
     * stores the end node
     */
    private MapNode goalNode;

    /**
     * Stores the position of the hole as x, y, z
     */
    private Vector3 holePosition;

    /**
     * list of all obstacles (eg walls, floor, slopes) in the game world.
     */
    private ArrayList<SolidObject> gameObjects;

    /**
     * the MapGraph generated by turning the game world into a grid
     */
    private MapGraph gridMapGraph;

    /**
     * the MapGraph generated by turning the game world into a graph using physics simulation
     */
    private MapGraph graphMapGraph;

    /**
     * List of integers mapping to obstacles, floor, and hole
     */
    private final int empty = 0;
    private final int floor = 1;
    private final int wall  = 2;
    private final int mill  = 3;
    private final int hole  = 4;
    private final int misc  = 5;

    /**
     * HashMap mapping the integers of the obstacles to the Strings used by SolidBodies
     */
    private final HashMap<String, Integer> stringIntegerHashMap = new HashMap<>(5);
    {
        stringIntegerHashMap.put(SolidObject.floor, floor);
        stringIntegerHashMap.put(SolidObject.wall, wall);
        stringIntegerHashMap.put(SolidObject.windmill, mill);
        stringIntegerHashMap.put(SolidObject.hole, hole);
        stringIntegerHashMap.put("empty", empty);
        stringIntegerHashMap.put("misc", misc);

    }

    /**
     * Creates a game map object which can translate the hole to the ai
     * @param loader gameWorldLoader object which has been used to create the game world
     */
    public GameMap(GameWorldLoader loader)
    {
        Log.log("Created GameMap object");

        //get information from the GameWorldLoader
        this.startPosition = loader.getStartPosition(); //currently just returns a new Vector at 0,0,0
        this.holePosition = loader.getHolePosition();
        this.gameObjects = loader.getSolidObjects();

        //calculate grid-based view of the golf course
        preMakeGrid();

        //calculate graph-based view of the golf course with the help of physics simulations
        preMakeGraph();
    }

    /**
     * determines the grid of the game world so when the information is requested the computation has already been done
     */
    private void preMakeGrid()
    {
        //calculates the grid dimensions
        float[] dimensions = determineGridDimensions();
        float minX = dimensions[0];
        float maxX = dimensions[1];
        float minY = dimensions[2];
        float maxY = dimensions[3];

        float absoluteX = Math.abs(maxX - minX);
        float absoluteY = Math.abs(maxY - minY);
        Log.log(String.format("absoluteX: %f\tabsoluteY: %f\n", absoluteX, absoluteY));
        int gridLength = Math.round(absoluteY);
        int gridWidth  = Math.round(absoluteX);
        Log.log(String.format("dimension of the grid: [%d,%d] multiplied by %d\n",gridLength, gridWidth,
                UNIT_TO_CELL_RATIO));
        int[][] grid =  new int[gridLength][gridWidth];

        int cellLengthX = grid[0].length / (int) absoluteX;
        int cellLengthY = grid.length / (int) absoluteY;

        for(int i = 0; i < gameObjects.size(); i++)
        {
            SolidObject object = gameObjects.get(i);

            Vector3 pos = object.getPosition();
            float width = object.getWidth();
            float depth = object.getDepth();

            int x =  (int)(pos.x - minX) / cellLengthX;
            int y =  (int)(pos.y - minY) / cellLengthY;

            int widthCells = (int) width / cellLengthX;
            int depthCells = (int) depth / cellLengthY;

            String s = gameObjects.get(i).getType();
            int toPutInArray = stringIntegerHashMap.containsKey(s) ? stringIntegerHashMap.get(s) : misc;


            if(s.equals(SolidObject.hole))
            {
                for(int yDepth = y - depthCells; yDepth < (y + depthCells); yDepth++)
                {
                    for(int xWidth = x - widthCells; xWidth < (x + widthCells); xWidth++)
                    {
                        if(xWidth > x - HOLE_RADIUS && xWidth < x + HOLE_RADIUS &&
                                yDepth > y - HOLE_RADIUS && yDepth < y + HOLE_RADIUS)
                        {
                            grid[yDepth][xWidth] = toPutInArray;
                            goalNode = new MazeMapNode(xWidth,yDepth);
                        }
                        else
                        {
                            grid[yDepth][xWidth] = floor;
                        }
                    }
                }
            }
            else
            {
                for (int yDepth = y - depthCells; yDepth < (y + depthCells); yDepth++)
                {
                    for (int xWidth = x - widthCells; xWidth < (x + widthCells); xWidth++)
                    {
                        grid[yDepth][xWidth] = toPutInArray;
                    }
                }
            }
        }

        startNode = new MazeMapNode((int)(startPosition.y-minY),(int)(startPosition.x - minX));

        Log.log(ArrayUtil.arrayToString(grid));
        generateGridMapGraph(grid);
    }


    /**
     * Generates the grid-based MapGraph
     */
    private void generateGridMapGraph(int[][] grid)
    {
        MapGraph map = new MapGraph(startNode, goalNode, new MazeHeuristicDistance());
    }

    /**
     * loops over all objects in the golf course to determine how large the grid has to be
     */
    private float[] determineGridDimensions()
    {
        //initialise max and min values
        SolidObject o = gameObjects.get(0);
        Vector3 pos = o.getPosition();

        //largest  x and y found
        float maxX = pos.x + o.getWidth();
        float maxY = pos.y + o.getWidth();
        //smallest x and y found
        float minX = pos.x - o.getDepth();
        float minY = pos.y - o.getDepth();

        //debug
        Log.log(String.format("Initial variables: \n" +
                "max X: %f\tmin X: %f\n" +
                "max Y: %f\tmin Y: %f\n",
                maxX, minX, maxY, minY));
        Log.log("Total amount of objects: " + gameObjects.size());

        //loop
        float width, depth;         //used in loop to store dimensions of every object
        Vector3 position;           //these dimensions do not have to be divided by 2 from the center to get max and min
        float temp;                 //because the solidObject class already does that
        for(SolidObject object : gameObjects)
        {
            //get data from object
            width = object.getWidth();       //x
            depth = object.getDepth();       //y
            position = object.getPosition();
            Log.log(String.format("current object %s:\nwidth: %f\tdepth: %f\n", object.getType(), width, depth));
            //determine if this object exceeds the current dimensions
            if((temp = position.x + width) > maxX)
            {
                Log.log("new maxX: " + temp);
                maxX = temp;
            }
            if((temp = position.x - width) < minX)
            {
                Log.log("new minX: " + temp);
                minX = temp;
            }
            if((temp = position.y + depth) > maxY)
            {
                Log.log("new maxY: " + temp);
                maxY = temp;
            }
            if((temp = position.y - depth) < minY)
            {
                Log.log("new minY: " + temp);
                minY = temp;
            }
        }
        Log.log(String.format("final variables: \n" +
                        "max X: %f\tmin X: %f\n" +
                        "max Y: %f\tmin Y: %f\n",
                maxX, minX, maxY, minY));
        //calculate actual grid size and return it
        return new float[] {minX, maxX, minY, maxY};
    }

    /**
     * determines the graph of the game world based on physics simulations (e.g every possible shot from a certain point)
     */
    private void preMakeGraph()
    {

    }

    /**
     * get a simple grid/tile based view of the game course for a* and floodfill algorithm
     * @return a MapGraph holding the game course
     */
    public MapGraph getGridBasedMapGraph()
    {
        return null;
    }

    /**
     * get a graph based view of the game course based on simulating every shot in the golf course.
     * @return A MapGraph holding every shot
     */
    public MapGraph getGraphBasedMapGraph()
    {
        return null;
    }

}
