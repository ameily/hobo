/**
 * CS 241 - project3 : MovingImage
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Date;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import java.awt.Toolkit;

/**
 * This class represents any moving image object. The image is able
 * to move based on a given rise and run and automatically bounce if
 * it collides with a wall.
 * @author Adam Meily
 */
public class MovingImage
{
	public enum Bounce {
		/**
		 * Automatically bounce off the wall
		 */
		Auto,

		/**
		 * Come around the opposite wall
		 */
		Continue,

		/**
		 * Set visibility to false when a wall is reached
		 */
		Disappear,

		/**
		 * Stop at a wall
		 */
		None };

	public enum Position
	{
		/**
		 * Left-most X point on the screen
		 */
		LeftWall,

		/**
		 * Right-most X point on the screen
		 */
		RightWall,

		/**
		 * Top-most Y Point on the screen
		 */
		TopWall,

		/**
		 * Center point on the screen
		 */
		Center,

		/**
		 * Bottom-most Y point on the screen
		 */
		BottomWall
	}

	final static String EXPLOSION_FILENAME = "explosion.gif";

	private int x, y, rise, run;
	private int destroyingTicks;
	private int points;
	private boolean visible;
	private Bounce bounce;
	private BufferedImage image;
	private String imageFilename;
	private Rectangle bounds;
	private long visibleSince;
	private boolean pointsCounted;

	private static int windowHeight, windowWidth;

	public MovingImage(Position x, Position y, int rise, int run, Bounce bounce, String imageFilename)
	{
		this.rise = rise;
		this.run = run;
		this.bounce = bounce;
		this.imageFilename = imageFilename;
		this.image = null;

		loadImage();
		setPosition(x, y);
		destroyingTicks = -1;
		setVisible(false);
		points = 0;
		pointsCounted = false;
	}

	public static void setWindowHeight(int height)
	{
		windowHeight = height;
	}

	public static void setWindowWidth(int width)
	{
		windowWidth = width;
	}

	public static int getWindowHeight()
	{
		return windowHeight;
	}

	public static int getWindowWidth()
	{
		return windowWidth;
	}

	public void setPosition(Position x, Position y)
	{
		if(x == Position.LeftWall)
			this.x = getImageLeftWallX();
		else if(x == Position.RightWall)
			this.x = getImageRightWallX();
		else if(x == Position.Center)
			this.x = getWindowCenterX() - (getImageWidth() / 2);

		if(y == Position.TopWall)
			this.y = getImageTopWallY();
		else if(y == Position.BottomWall)
			this.y = getImageBottomWallY();
		else if(y == Position.Center)
			this.y = getWindowCenterY() -  (getImageHeight() / 2);

		this.bounds.setLocation(this.x, this.y);
	}

	public int getImageWidth()
	{
		return image.getWidth();
	}

	public int getImageHeight()
	{
		return image.getHeight();
	}

	public void setX(int x)
	{
		this.x = x;
		bounds.setLocation(this.x, this.y);
	}

	public void setY(int y)
	{
		this.y = y;
		bounds.setLocation(this.x, this.y);
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public int getPoints()
	{
		return this.points;
	}

	public void setPoints(int points)
	{
		this.points = points;
	}

	public void setRise(int rise)
	{
		this.rise = rise;
	}

	public void setRun(int run)
	{
		this.run = run;
	}

	public int getRise()
	{
		return rise;
	}

	public int getRun()
	{
		return run;
	}

	public void setImageFilename(String filename)
	{
		this.imageFilename = filename;
	}

	public int loadImage() throws RuntimeException
	{

		try
		{
			File fp = new File(imageFilename);
			if(fp.isFile())
			{
				image = ImageIO.read(fp);
				bounds = new Rectangle(x, y, image.getHeight(), image.getWidth());
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException("Could not read image file '" + imageFilename + "'");
		}

		if(image == null)
			throw new RuntimeException("Could not read image file '" + imageFilename + "'");

		return 0;
	}

	/**
	 * Moves the image along the rise and run path.
	 * @return 0 if nothing happened (ie. a wall was hit and the bounce is set to
	 * Bounce.None), 1 otherwise.
	 */
	public int move()
	{
		// if it isn't visible or is in the process of being destroyed, exit
		if(!isVisible())
			return 0;

		if(destroyingTicks >= 0) // in the process of destorying
		{
			destroyingTicks++;
			if(isDestroyed())
				setVisible(false);

			if((destroyingTicks % 5) == 0)
			{
				double rads = 0.62;
				
				AffineTransform transform = new AffineTransform();
				// image wil rotate 10 times, this means each time
				// it should rotate 36 degress or 0.62 radians.
				// the original explosion image is 64 x 64, therefore
				// we use 32, 32 as the center point to rotate around.
				transform.rotate(rads, 32.0, 32.0);

				//transform.scale(1.05, 1.05);

				AffineTransformOp op = new AffineTransformOp(transform,
						AffineTransformOp.TYPE_BILINEAR);
				image = op.filter(image, null);
			}

			return 0;
		}
		
		int ret = 0;
		
		// top = 1, right = 2, bottom = 4, left = 8
		int walls = getTouchingWalls();
		ret = walls;

		if(bounce == Bounce.Auto)
		{
			if((walls & 1) > 0 || (walls & 4) > 0) // top or bottom
			{
				reverseRise();
			}

			if((walls & 2) > 0 || (walls & 8) > 0) // left or right
			{
				reverseRun();
			}
		}
		else if(bounce == Bounce.Continue)
		{
			if((walls & 1) > 0) // top
				setY(0);

			if((walls & 2) > 0) // right
				setX(windowWidth - getImageWidth());

			if((walls & 4) > 0) // bottom
				setY(windowHeight - getImageHeight());

			if((walls & 8) > 0) // left
				setX(0);
		}
		else if(bounce == Bounce.Disappear)
		{
			if(walls > 0)
				setVisible(false);
		}
		else if(bounce == Bounce.None)
		{
			if(walls > 0)
			{
				Toolkit.getDefaultToolkit().beep();
				return 0;
			}
		}

		x += run;
		y += rise;
		bounds.setLocation(x, y);

		return ret;
	}

	/**
	 * Get the walls that the image is touching
	 * @return a value that is or'ed (|) with any of
	 * the following values:
	 * 1: top wall
	 * 2: right wall
	 * 3: bottom wall
	 * 4: left wall
	 */
	private int getTouchingWalls()
	{
		int ret = 0;

		// check top
		if((y + rise) <= 0)
			ret = ret | 1;

		//check right
		if((x + getImageWidth() + run) > windowWidth)
			ret = ret | 2;

		// check bottom
		if((y + getImageHeight() + rise) > windowHeight)
			ret = ret | 4;

		// check left
		if((x + run)  <= 0)
			ret = ret | 8;

		return ret;
	}

	/**
	 * Set the image's visiblity
	 * @param visible the image's visibility
	 */
	public void setVisible(boolean visible)
	{
		if(visible != this.visible)
		{
			if(visible == true)
				visibleSince = new Date().getTime();
			else
				visibleSince = -1;
		}
		
		this.visible = visible;
	}

	/**
	 * Get the number of seconds the image has been visible
	 * @return the number of seconds the image has been visible
	 */
	public int getSecondsVisible()
	{
		if(visibleSince < 0)
			return 0;
		
		long now = new Date().getTime();
		return (int)(now - visibleSince) / 1000;
	}

	/**
	 * Is the image currently visible
	 * @return the image's current visibility
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Reverse the image's run
	 */
	public void reverseRun()
	{
		run = -run;
	}

	/**
	 * Reverse the image's rise
	 */
	public void reverseRise()
	{
		rise = -rise;
	}

	/**
	 * Paint the image using the given Graphics object
	 * @param g the Graphics object to paint with
	 */
	public void paint(Graphics g)
	{
		if(isVisible())
			g.drawImage(image, x, y, null);
	}

	/**
	 * Get the window's center X point
	 * @return the window's center X point
	 */
	public int getWindowCenterX()
	{
		return getWindowWidth() / 2;
	}

	/**
	 * Get the window's center Y point
	 * @return the window's center Y point
	 */
	public int getWindowCenterY()
	{
		return getWindowHeight() / 2;
	}

	/**
	 * Get the greatest Y-axis point that the image
	 * could be positioned
	 * @return the greatest Y-axis point that the
	 * image could be positioned
	 */
	public int getImageBottomWallY()
	{
		return windowHeight - getImageHeight();
	}

	/**
	 * Get the least Y-axis point that the image
	 * could be positioned
	 * @return the least Y-axis point the image
	 * could be positioned
	 */
	public int getImageTopWallY()
	{
		return 0;
	}

	/**
	 * Get the least X-axis point that the image
	 * could be positioned
	 * @return the least X-axis point the image
	 * could be positioned
	 */
	public int getImageLeftWallX()
	{
		return 0;
	}

	/**
	 * Get the greatest X-axis point that the image
	 * could be positioned
	 * @return greatest X-axis point the image could be
	 * positioned
	 */
	public int getImageRightWallX()
	{
		return windowWidth - getImageWidth();
	}

	/**
	 * Get the center X-axis pixel of the image
	 * @return the center X-axis pixel of the image
	 */
	public int getImageCenterX()
	{
		int center = x + (getImageWidth() / 2);

		return center;
	}

	/**
	 * Get the center Y-axis pixel of the image
	 * @return the center Y-axis pixel of the image
	 */
	public int getImageCenterY()
	{
		int center = y + (getImageHeight() / 2);

		return center;
	}

	/**
	 * Destory the image. This should be called if a collision
	 * occurs.
	 * @throws java.lang.RuntimeException
	 */
	public void destroy() throws RuntimeException
	{
		if(destroyingTicks < 0)
		{
			int oldWidth = getImageWidth();
			int oldHeight = getImageHeight();

			imageFilename = EXPLOSION_FILENAME;
			loadImage();

			// new image is smaller (x) than old image
			if(getImageWidth() < oldWidth)
			{
				x += (oldWidth - getImageWidth()) / 2;
			}

			if(getImageHeight() < oldHeight)
			{
				x += (oldHeight - getImageHeight()) / 2;
			}

			destroyingTicks = 0;
		}
	}

	/**
	 * Check if the image is destroyed.
	 * @return true if image is destroyed, false otherwise
	 */
	public boolean isDestroyed()
	{
		return destroyingTicks >= 50;
	}

	/**
	 * Check if there is a collision between this and another
	 * image.
	 * @param other the other image to check
	 * @return true if they are colliding, false otherwise
	 */
	public boolean isCollision(MovingImage other)
	{
		if(this.isVisible() && other.isVisible() && this.destroyingTicks < 0 && !other.isExploding())
			return bounds.intersects(other.getBounds());
		
		return false;
	}

	/**
	 * Get the bounds of the image. This is mainly used for collision
	 * detection.
	 * @return the bounds of the image
	 */
	public Rectangle getBounds()
	{
		return bounds;
	}

	/**
	 * Set a random x position for the image
	 * @param min the minimum value
	 * @param max the maximum value
	 */
	public void setRandomX(int min, int max)
	{
		int real_max = max;
		if((real_max + getImageWidth()) > getWindowWidth())
			real_max = getWindowWidth() - getImageWidth();
		
		setX(getRandom(min, real_max));
	}

	/**
	 * Set a random y position for the image
	 * @param min the minimum value
	 * @param max the maximum value
	 */
	public void setRandomY(int min, int max)
	{
		int real_max = max;
		if((real_max + getImageHeight()) > getWindowHeight())
			real_max = getWindowHeight() - getImageHeight();

		setY(getRandom(min, real_max));
	}

	/**
	 * Set a random rise for the image
	 * @param min the minimum rise
	 * @param max the maximum rise
	 */
	public void setRandomRise(int min, int max)
	{
		this.rise = getRandom(min, max);
	}

	/**
	 * Set a random run for the image
	 * @param min the minimum run
	 * @param max the maximum run
	 */
	public void setRandomRun(int min, int max)
	{
		this.run = getRandom(min, max);
	}

	/**
	 * Get a random integer
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return a random integer inbetween min and max
	 */
	private int getRandom(int min, int max)
	{
		return (int)(Math.random() * (max - min) + min + 1);
	}

	/**
	 * Print a string representation of this image. This
	 * is used for debugging purposes.
	 * @return the string representation of this image
	 */
	public String toString()
	{
		return "Rise: " + rise + "\nRun: " + run + "\nX: " + x + "\nY: " + y +"\n";
	}

	/**
	 * Check if the image is currently exploding
	 * @return true if the image is exploding, false otherwise.
	 */
	public boolean isExploding()
	{
		return destroyingTicks >= 0;
	}

	/**
	 * Have the points from this image been counted
	 * towards to the player's score.
	 * @return true if they have, false otherwise
	 */
	public boolean havePointsBeenCounted()
	{
		return pointsCounted;
	}

	/**
	 * Set that the points have been counted towards the
	 * player's score.
	 * @param val true if they have been
	 */
	public void setHavePointsBeenCounted(boolean val)
	{
		this.pointsCounted = val;
	}
}
