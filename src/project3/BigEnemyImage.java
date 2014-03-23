/**
 * CS 241 - project3 : BigEnemyImage
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

/**
 * Represents the big enemy image. This is slower than
 * the SmallEnemyImage and worth 1 point.
 */
public class BigEnemyImage extends MovingImage
{
	final static String BIG_ENEMY_IMAGE = "enemy-big.gif";
	
	public BigEnemyImage()
	{
		super(Position.LeftWall, Position.LeftWall, 2, 6, Bounce.Auto, BIG_ENEMY_IMAGE);
		setPoints(1);
		setRandomX(0, getWindowWidth());
		setRandomY(0, getWindowHeight() / 2);
		setRandomRise(1, 2);
		setRandomRun(1, 2);
	}
}
