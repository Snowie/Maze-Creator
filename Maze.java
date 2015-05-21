/* 
 * Author: Lucas A. Doran
 * Date: 11/17/2013
 * Filename: Maze.java
 * Description: A program that uses disjoint sets to generate an ASCII maze.
 */

import java.util.*;

//A small class to represent a wall as a point in a 2d array
class Point
{
	Point(int y, int x)
	{
		this.x = x;
		this.y = y;
	}

	int x;
	int y;
}

public class Maze
{
	private static int [] parent;
	private static int [] rank;

	//The following three functions are taken from Dr. Szumlanski's notes on disjoint sets
	private static void union(int x, int y)
	{
		int setx = findset(x);
		int sety = findset(y);

		//If setx's rank is less than sety's, set the parent of setx to be setY
		if (rank[setx] < rank[sety])
			parent[setx] = sety;
		//If it is the other way around, do the opposite.
		else if (rank[sety] < rank[setx])
			parent[sety] = setx;
		//If they are equal, perform the same action but increment the rank
		else
		{
			parent[sety] = setx;
			rank[setx]++;
		}
	}
	
	private static int findset(int x)
	{
		//If we have found the root of the tree/set, return it
		if(parent[x] == x)
			return x;

		//Otherwise, find the root parent (and assign it to every node on the way up)
		//Path compression
		parent[x] = findset(parent[x]);

		return parent[x];
	}

	private static int [] makeset(int n)
	{
		//Create a parent a rank array
		parent = new int [n];
		rank = new int [n];

		//Initally, all ranks are 0 and parents are themselves
		for(int i = 0; i < n; ++i)
		{
			rank[i] = 0;
			parent[i] = i;
		}

		return parent;
	}

	//A function that returns a maze that has m by n cell dimensions ([2m + 1] x [2n + 1] in total)
	public static char [][] create(int width, int height)
	{
		//Keep the number of disjoint sets to use later
		int numSets = width * height;
		//Create a temporary list to store removable walls
		List <Point> tempList = new ArrayList<Point>();

		//Initialize the disjoint sets
		int [] parent = makeset(width * height);
		
		//The array needs to be 2h + 1 by 2w + 1 to include the walls.
		height += height + 1;
		width += width + 1;

		char [][] maze = new char[height][width];

		for(int i = 0; i < height; ++i)
		{
			for(int j = 0; j < width; ++j)
			{
				//The top or bottom walls, no cells can be removed from here
				if(i == 0 || i == height - 1)
				{
					maze[i][j] = '#';
					continue;
				}

				//The left and right walls, once again, nonremovable
				if(j == 0 || j == width - 1)
				{
					maze[i][j] = '#';
					continue;
				}

				//The horizontal rows of walls above and below cells
				if(i % 2 == 0)
				{
					maze[i][j] = '#';
					//Removable as long as they touch cells in anyway other than diagonal
					if(j % 2 != 0)
						tempList.add(new Point(i, j));
					
					continue;
				}

				//The walls between the left and right of the cells, always removable.
				if(j % 2 == 0)
				{
					maze[i][j] = '#';
					tempList.add(new Point(i, j));
					continue;
				}

				//The empty cells
				maze[i][j] = ' ';
			}
		}

		//Randomize the order in which we try to remove walls
		Collections.shuffle(tempList);

		//Create a queue based on a linked list to facilitate O(1) removal
		Queue <Point> walls = new LinkedList<Point>();

		//Copy the list to the queue
		for(int i = 0; i < tempList.size(); ++i)
			walls.add(tempList.get(i));

		//Run until we only have one set
		while(numSets > 1)
		{
			Point workingWall;
			//While we still have walls to work with...
			if(!walls.isEmpty())
			{
				//Pop the next wall off of the queue.
				workingWall = walls.remove();
				
				//Wall divides two cells above & below
				if(workingWall.y % 2 == 0)
				{
					//The number of cells in previous rows
					int toAdd = ((maze[0].length) / 2) * ((workingWall.y - 1)/2);
					
					//The row above  needs the cells of the previous rows
					int above = toAdd;
					//The row below requires that sum + another row
					int below = toAdd + (maze[0].length/2);

					//Now, add the wall's x/2 to them and you get their number
					above+= workingWall.x/2;
					below+= workingWall.x/2;
					
					//If those two cells are in different sets
					if(findset(above) != findset(below))
					{
						//Clear the wall
						maze[workingWall.y][workingWall.x] = ' ';
						//Put the cells in the same set
						union(above, below);
						//And finally, decrement the number of different sets
						--numSets;
					}
				}
				//Wall divides two cells left & right
				else
				{
					//The number of cells in rows that come previously
					int toAdd = ((maze[0].length) / 2) * ((workingWall.y - 1)/2);
					
					//The cell to the left is equivalent to the sum of the previous rows + the wall's x/2 - 1
					int left = toAdd + workingWall.x/2 - 1;
					//The cell to the left is simply the sum of the previous rows + the wall's x/2
					int right = toAdd + workingWall.x/2;
					
					//If the two cells are not of the same set
					if(findset(left) != findset(right))
					{
						//Clear the wall
						maze[workingWall.y][workingWall.x] = ' ';
						//Put the cells in the same set
						union(left, right);
						//And finally, decrement the number of different sets.
						--numSets;
					}
				}
			}
			else
			{
				//Failsafe break
				break;
			}
		}

		//Denote the start of the maze as 's' at the top left (within the walls)
		maze[1][1] = 's';
		//Denote the end of the maze as 'e' in the bottom left (within the walls)
		maze[height - 2][width - 2] = 'e';

		return maze;
	}

	public static double difficultyRating()
	{
		return 2.0;
	}
	
	public static double hoursSpent()
	{
		return 4.3;
	}
}