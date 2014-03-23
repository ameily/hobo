/**
 * CS 241 - project3 : SmallEnemyImage
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

/**
 * Represents the small enemy image. This is the faster of the
 * two emenies and is worth 2 points.
 */
public class SmallEnemyImage extends MovingImage
{
	final static String SMALL_ENEMY_IMAGE = "enemy-small.gif";
	
	public SmallEnemyImage()
	{
		super(Position.RightWall, Position.TopWall, 2, 6, Bounce.Auto, SMALL_ENEMY_IMAGE);
		setPoints(2);
		setRandomX(0, getWindowWidth());
		setRandomY(0, getWindowHeight() / 2);
		setRandomRise(1, 3);
		setRandomRun(1, 3);
	}
}
