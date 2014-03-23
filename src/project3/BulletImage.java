/**
 * CS 241 - project3 : BulletImage
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

/**
 * This represents a bullet image.
 */
public class BulletImage extends MovingImage
{
	final static String BULLET_IMAGE = "bullet.gif";

	public BulletImage()
	{
		super(Position.LeftWall, Position.TopWall, -2, 0, Bounce.Disappear, BULLET_IMAGE);
		setVisible(false);
	}

	/**
	 * Fire a new bullet originating from the center of
	 * the given image (this should be PlayerImage).
	 * @param from where to shoot the bullet from.
	 */
	public void fireNewBullet(MovingImage from)
	{
		int shotX = from.getImageCenterX() - (getImageWidth() / 2);
		int shotY = from.getY() - getImageHeight();

		setX(shotX);
		setY(shotY);
		setVisible(true);
	}
}
