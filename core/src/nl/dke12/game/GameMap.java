package nl.dke12.game;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import nl.dke12.bot.maze.MazeHeuristicDistance;
import nl.dke12.bot.maze.MazeMapNode;
import nl.dke12.bot.pathfinding.HeuristicMethod;
import nl.dke12.bot.pathfinding.MapGraph;
import nl.dke12.bot.pathfinding.MapNode;
import nl.dke12.util.ArrayUtil;
import nl.dke12.util.GameWorldLoader;
import nl.dke12.util.Log;
import nl.dke12.util.Logger;

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
    private Vector3 startPosition; // TODO: 21/06/2016 change to private

    /**
     * stores the start node
     */
    private MapNode startNode; // TODO: 21/06/2016 change to private

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

    private void fillHashMap(){
        stringIntegerHashMap.put(SolidObject.floor, floor);
        stringIntegerHashMap.put(SolidObject.wall, wall);
        stringIntegerHashMap.put(SolidObject.windmill, mill);
        stringIntegerHashMap.put(SolidObject.hole, hole);
        stringIntegerHashMap.put("empty", empty);
        //stringIntegerHashMap.put("misc", misc);
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

        fillHashMap();

        //calculate grid-based view of the golf course
        //preMakeGrid();

        //calculate graph-based view of the golf course with the help of physics simulations
        //preMakeGraph();
    }

    /**
     * determines the grid of the game world so when the information is requested the computation has already been done
     * sets the start and end nodes
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
        Log.log(String.format("dimension of the grid: [%d,%d] multiplied by %d\n", gridLength, gridWidth, UNIT_TO_CELL_RATIO));
        int[][] numgrid =  new int[gridLength][gridWidth];

        int cellLengthX = numgrid[0].length / (int) absoluteX;
        int cellLengthY = numgrid.length / (int) absoluteY;

        for(int i = 0; i < gameObjects.size(); i++)
        {
            SolidObject object = gameObjects.get(i);

            Vector3 pos = object.getPosition();
            float width = object.getWidth();
            float depth = object.getDepth();

            width = 4;
            depth = 4;

            int x =  (int)(pos.x - minX) / cellLengthX;
            int y =  (int)(pos.y - minY) / cellLengthY;

            int widthCells = (int) width / cellLengthX;
            int depthCells = (int) depth / cellLengthY;


            String s = gameObjects.get(i).getType();
            int toPutInArray = stringIntegerHashMap.containsKey(s) ? stringIntegerHashMap.get(s) : misc;
            //Log.log("s is " + s + " the thing we put in array is " + toPutInArray);

            if(s.equals(SolidObject.hole))
            {
                //Log.log("s = hole");
                for(int yDepth = y - depthCells; yDepth < (y + depthCells); yDepth++)
                {
                    for(int xWidth = x - widthCells; xWidth < (x + widthCells); xWidth++)
                    {
                        if(xWidth > x - HOLE_RADIUS && xWidth < x + HOLE_RADIUS &&
                                yDepth > y - HOLE_RADIUS && yDepth < y + HOLE_RADIUS)
                        {
                            numgrid[yDepth][xWidth] = toPutInArray;
                            goalNode = new MazeMapNode(xWidth,yDepth);
                        }
                        else
                        {
                            numgrid[yDepth][xWidth] = floor;
                        }
                    }
                }
            }
            else
            {
//                Log.log("in else but not in for loop");
//                Log.log("x = " + x + " y = " + y);
//                Log.log("depthCell = " + depthCells + " widthCells = " + widthCells);

                for (int yDepth = y - depthCells; yDepth < (y + depthCells); yDepth++)
                {
                    //Log.log(" ydepth = " + yDepth);
                    for (int xWidth = x - widthCells; xWidth < (x + widthCells); xWidth++)
                    {

                        //Log.log(" ydepth = " + yDepth + " xWidth = " + xWidth);
                        numgrid[yDepth][xWidth] = toPutInArray;
                        //Log.log("The thing we put in array at : " + xWidth + "; " + yDepth + " with val " + toPutInArray + " and actual : " + numgrid[yDepth][xWidth]);
                    }
                }
            }
        }

        //Log.log(" ball position: " + (int)(startPosition.y-minY) + " " + (int)(startPosition.x - minX));
        startNode = new MazeMapNode((int)(startPosition.y - minY),(int)(startPosition.x - minX));
        System.out.println("setting start node in premakegrid");
        Log.log(ArrayUtil.arrayToString(numgrid));
        generateGridMapGraph(numgrid);

        this.numgrid = numgrid; //// TODO: 20/06/2016 delete later because only to display path

    }
        public int[][] numgrid;


    /**
     * Generates the grid-based MapGraph
     */
    private void generateGridMapGraph(int[][] numgrid)
    {
        gridMapGraph = new MapGraph(startNode, goalNode, new MazeHeuristicDistance());
        //Log.log(ArrayUtil.arrayToString(numgrid));
        Log.log("created girdMapGraph");
        graph = new MazeMapNode[numgrid.length][numgrid[0].length];

        graph[((MazeMapNode) startNode).getY()][((MazeMapNode) startNode).getX()] = startNode;
        graph[((MazeMapNode) goalNode ).getY()][((MazeMapNode) goalNode ).getX()] = goalNode;

        //Log.log("startNode: " + startNode.getIdentifier() + " numValue= " + numgrid[((MazeMapNode) startNode).getY()][((MazeMapNode) startNode).getX()]);
        //Log.log("endNode:   " + goalNode.getIdentifier()  + " numValue= " + numgrid[((MazeMapNode) goalNode ).getY()][((MazeMapNode) goalNode ).getX()]);

        for(int i = 0; i < numgrid.length; i++)
        {
            for (int j = 0; j < numgrid[0].length; j++)
            {
                //Log.log("Creating mapNode for grid at pos : " + j + " " + i );
                if(numgrid[i][j] == 1 || numgrid[i][j] == 4 || numgrid[i][j] == 5 )
                {
                    getMapNode(j, i, graph);
                    giveNeighbours(j, i, graph, numgrid);
                }
                else
                {
                    //Log.log("Not creating neighbour because not floor at pos: " + j + " " + i );
                }
            }
        }
        //Log.log(gridMapGraph.getStartNode().fullInformation() + "\n " + gridMapGraph.getGoalNode().fullInformation());
    }

    MapNode[][] graph; // TODO: 20/06/2016 not instance variable

    private MapNode getMapNode(int x, int y, MapNode[][] grid) throws ArrayIndexOutOfBoundsException
    {
        if(grid[y][x] == null)
        {
            grid[y][x] = new MazeMapNode(x,y);
            //System.out.println("creating new node");
            //Log.log("Creating mapNode for grid at pos : " + x + " " + y );
        }
        return grid[y][x];
    }

    private void giveNeighbours(int x, int y, MapNode[][] grid, int[][] numgrid)
    {
        //Log.log("");
        //Log.log("Checking for neighbours of: " + grid[y][x].getIdentifier());
        MapNode node = grid[y][x];
        for(int i = -1; i < 2; i++)
        {
            for(int j = -1; j < 2; j++)
            {
                if( i == 0 && j == 0)
                {
                    continue;
                }
                else
                {
                    try {
                        //Log.log("Trying neighbour at pos    " + (j+x) + " " + (i+y));

                        if (numgrid[i + y][j + x] == floor || numgrid[i + y][j + x] == hole || numgrid[i + y][j + x] == misc)
                        {
                            MapNode neighbouringNode = getMapNode(x + j, y + i, grid);
                            if(x == x+j || y == y+i) //not diagonal
                            {
                                node.giveNeighbour(neighbouringNode, 10);
                            }
                            else
                            {
                                node.giveNeighbour(neighbouringNode, 14);
                            }
                           // Log.log("        Added neighbour at pos     " + (j+x) + " " + (i+y));
                        }
//                        else if (numgrid[i + y][j + x] == wall)
//                        {
//                            MapNode neighbouringNode = getMapNode(x + j, y + i, grid);
//                            neighbouringNode.setWalkable(false);
//                            if(x == x+j || y == y+i) //not diagonal
//                            {
//                                node.giveNeighbour(neighbouringNode, Integer.MAX_VALUE);
//                            }
//                            else
//                            {
//                                node.giveNeighbour(neighbouringNode, Integer.MAX_VALUE);
//                            }
//                        }
                    }
                    catch(ArrayIndexOutOfBoundsException e){} //handles getting nodes outside of grid, e.g a node at a wall
                    catch (MapNode.NeighbourException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        //Log.log(node.fullInformation());
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
//        Log.log(String.format("Initial variables: \n" +
//                "max X: %f\tmin X: %f\n" +
//                "max Y: %f\tmin Y: %f\n",
//                maxX, minX, maxY, minY));
//        Log.log("Total amount of objects: " + gameObjects.size());

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
            //Log.log(String.format("current object %s:\nwidth: %f\tdepth: %f\n", object.getType(), width, depth));
            //determine if this object exceeds the current dimensions
            if((temp = position.x + width) > maxX)
            {
                //Log.log("new maxX: " + temp);
                maxX = temp;
            }
            if((temp = position.x - width) < minX)
            {
                //Log.log("new minX: " + temp);
                minX = temp;
            }
            if((temp = position.y + depth) > maxY)
            {
               // Log.log("new maxY: " + temp);
                maxY = temp;
            }
            if((temp = position.y - depth) < minY)
            {
               // Log.log("new minY: " + temp);
                minY = temp;
            }
        }
//        Log.log(String.format("final variables: \n" +
//                        "max X: %f\tmin X: %f\n" +
//                        "max Y: %f\tmin Y: %f\n",
//                maxX, minX, maxY, minY));
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
    public synchronized MapGraph getGridBasedMapGraph()
    {
        preMakeGrid();
        return gridMapGraph;
    }

    /**
     * get a graph based view of the game course based on simulating every shot in the golf course.
     * @return A MapGraph holding every shot
     */
    public MapGraph getGraphBasedMapGraph()
    {
        preMakeGraph();
        Log.log("asking for graph based mapgraph which is null");
        return null;
    }

    public void setStartNode(Vector3 ballPos)
    {
        System.out.println("setting start node");
        this.startPosition = new Vector3(ballPos);
        this.gridMapGraph = getGridBasedMapGraph();
    }

    public MapNode getStartNode()
    {
        return startNode;
    }

    public Vector3 getStartPosition()
    {
        return startPosition;
    }

    public MapNode getGoalNode()
    {
        return goalNode;
    }
}
