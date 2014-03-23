/**
 * CS 241 - project3 : PlayerImage
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

/**
 * Represents the player's character image. This can
 * only move after a queueMove() is called with the
 * given direction.
 */
public class PlayerImage extends MovingImage
{
	final static String PLAYER_IMAGE_FILENAME = "player.gif";
	private int movement;

	public PlayerImage()
	{
		super(Position.Center, Position.BottomWall, 0, 5, Bounce.None, PLAYER_IMAGE_FILENAME);
	}

	/**
	 * We override the move method because the player has
	 * to queue a move before the image should move.
	 * @see MovingImage#move()
	 */
	@Override
	public int move()
	{
		int ret = 0;

		if(movement != 0 || isExploding())
		{
			if((movement * getRun()) < 0) // ie. the user changed direction
				setRun(0 - getRun());
			
			ret = super.move();
		}

		return ret;
	}

	/**
	 * Queue the player movement
	 * @param direction -1 to move left, 1 to move right, 0 to stop
	 */
	public void queueMove(int direction)
	{
		movement = direction;
	}
}
