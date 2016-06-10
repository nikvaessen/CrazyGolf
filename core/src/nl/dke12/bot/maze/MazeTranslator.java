package nl.dke12.bot.maze;

import nl.dke12.bot.maze.Maze;
import nl.dke12.bot.pathfinding.MapGraph;
import nl.dke12.bot.pathfinding.MapGraphFactory;
import nl.dke12.bot.pathfinding.MapNode;
import nl.dke12.bot.pathfinding.Node;

import java.util.ArrayList;

/**
 * Created by Ajki on 07/06/2016.
 */
public class MazeTranslator implements MapGraphFactory<Maze>
{
    private final boolean debug = true;

    @Override
    public MapGraph makeMapGraph(Maze maze)
    {
        char[][] grid =  maze.getMaze();
        int endNodeX = maze.endCoords[0];
        int endNodeY = maze.endCoords[1];
        int startNodeX = maze.beginCoords[0];
        int startNodeY = maze.beginCoords[1];
        if(debug) {
            System.out.println("making a map graph of the following maze:");
            maze.printMaze();
            System.out.printf("Start node is at x: %d\ty: %d\nEnd node is at x: %d\ty: %d\n",
                    startNodeX, startNodeY, endNodeX, endNodeY);
        }

        MapNode[][] mapNodeGrid = new MapNode[grid.length][grid[0].length];

        for(int i = 0; i < grid.length; i++)
        {
            for(int j = 0; j < grid[i].length; j++)
            {
                if(grid[i][j] == Maze.openChar)
                {
                    mapNodeGrid[i][j] = new MazeMapNode(i,j);
                    giveNeighbours(i,j, mapNodeGrid);
                }
            }
        }

        MapGraph theMapGraph = new MapGraph(getMapNode(startNodeX, startNodeY, mapNodeGrid),
                getMapNode(endNodeX, endNodeY, mapNodeGrid), getArrayListOfAllNodes(mapNodeGrid));
        return theMapGraph;
    }

    private MapNode getMapNode(int x, int y, MapNode[][] grid) throws ArrayIndexOutOfBoundsException
    {
        if(grid[x][y] == null)
        {
            grid[x][y] = new MazeMapNode(x,y);
        }
        return grid[x][y];
    }

    private void giveNeighbours(int x, int y, MapNode[][] grid)
    {
        MapNode node = grid[x][y];
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
                    try
                    {
                        MapNode neighbouringNode = getMapNode(x + i,y + j, grid);
                        node.giveNeighbour(neighbouringNode, 1);
                    }
                    catch(ArrayIndexOutOfBoundsException e){} //handles getting nodes outside of grid, e.g a node at a wall
                    catch (MapNode.NeighbourException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private ArrayList<MapNode> getArrayListOfAllNodes(MapNode[][] grid)
    {
        ArrayList<MapNode> arrayList = new ArrayList<>();
        for(int i = 0; i < grid.length; i++)
        {
            for(int j = 0; j < grid[i].length; j++)
            {
                if(grid[i][j] != null)
                {
                    arrayList.add(grid[i][j]);
                }
            }
        }
        return arrayList;
    }

}
