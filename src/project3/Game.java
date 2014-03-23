/**
 * CS 241 - project3 : Game
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

import java.io.File;
import java.util.Date;

import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.MidiSystem;
import javax.swing.JOptionPane;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

/**
 * This is the workhorse class of the entire project. It
 * moves images on a defined interval of time, checks for
 * collisions, checks the victory conditions, and gives
 * the user control of what happens.
 */
public class Game extends JPanel
{
	private int pointsTotal;
	public static final int POINTS_TO_WIN = 20;
	public static final String MIDI_SONG_FILE = "game.mid";
	private Sequencer midiPlayer;
	private Sequence midiSong;
	private BigEnemyImage bigEnemy;
	private SmallEnemyImage smallEnemy;
	private BulletImage bullet;
	private PlayerImage player;
	private int playerLivesLeft;
	private long startTime;

	public Game()
	{
		super();
		//this.addKeyListener(this);
		startTime = 0;
	}

	/**
	 * Tell the game to start to game counter (ie. runtime start point)
	 */
	public void start()
	{
		startTime = new Date().getTime();
	}

	/**
	 * Get the number of seconds that the game has been running.
	 * @return the number of seconds the game has been running
	 */
	public int getRuntime()
	{
		return (int)(new Date().getTime() - startTime) / 1000;
	}

	/**
	 * Initialize the game. If this method returns -1 (ie. it fails),
	 * this is an unrecoverable error.
	 * @param windowWidth the width of the playing area
	 * @param windowHeight the height of the playing area
	 * @return 0 on success, -1 otherwise
	 */
	public int init(int windowWidth, int windowHeight)
	{
		MovingImage.setWindowHeight(windowHeight);
		MovingImage.setWindowWidth(windowWidth);

		bigEnemy = new BigEnemyImage();
		smallEnemy = new SmallEnemyImage();
		bigEnemy.setVisible(true);
		
		bullet = new BulletImage();
		player = new PlayerImage();
		pointsTotal = 0;
		player.setVisible(true);
		playerLivesLeft = 2;

		try
		{
			midiPlayer = MidiSystem.getSequencer();
			midiSong = MidiSystem.getSequence(new File(MIDI_SONG_FILE));
			midiPlayer.setSequence(midiSong);
			midiPlayer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			midiPlayer.open();
		}
		catch(Exception e)
		{
			midiPlayer = null;
			JOptionPane.showMessageDialog(null, "There was an error initializing " +
					"the Midi sequencer. Therefore, the midi music has been turned off.",
					"Alert", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
		}

		return 0;
	}

	/**
	 * Fire a bullet from the player
	 */
	public void fireBullet()
	{
		if(!bullet.isVisible() && !player.isDestroyed() && player.isVisible() &&
				!player.isExploding())
			bullet.fireNewBullet(player);
	}

	/**
	 * Check for collisions and and destroy images if needed.
	 */
	public void checkForCollisions()
	{
		boolean removeBullet = false;
		if(player.isCollision(smallEnemy) || player.isCollision(bigEnemy))
		{
			player.destroy();
			System.out.println("collision: enemy / player");
		}

		if(smallEnemy.isCollision(bigEnemy))
		{
			smallEnemy.reverseRise();
			smallEnemy.reverseRun();
			//bigEnemy.reverseRise();
			//bigEnemy.reverseRun();
			System.out.println("collision: big / small enemies");
		}

		if(smallEnemy.isCollision(bullet))
		{
			smallEnemy.destroy();
			removeBullet = true;
			System.out.println("collision: small enemy destroyed");
		}

		if(bigEnemy.isCollision(bullet))
		{
			bigEnemy.destroy();
			removeBullet = true;
			System.out.println("collision: big enemy destroyed");
		}

		if(removeBullet)
			bullet.setVisible(false);
	}


	/**
	 * Check to see if anyone has won.
	 * @return 0 if no one has not won, 1 if the player won, and -1 if the
	 * computer won.
	 */
	public int checkVictoryConditions()
	{
		int ret = 0;

		if(bigEnemy.isDestroyed() && !bigEnemy.havePointsBeenCounted())
		{
			pointsTotal += bigEnemy.getPoints();
			bigEnemy.setHavePointsBeenCounted(true);
		}

		if(smallEnemy.isDestroyed() && !smallEnemy.havePointsBeenCounted())
		{
			pointsTotal += smallEnemy.getPoints();
			smallEnemy.setHavePointsBeenCounted(true);
		}

		if(pointsTotal >= POINTS_TO_WIN && playerLivesLeft >= 0)
			ret = 1;
		else if(playerLivesLeft < 0 || getRuntime() > 90)
			ret = -1;

		return ret;
	}

	/**
	 * Respawn enemy images and the player image if needed.
	 */
	public void respawnImages()
	{
		if(!bigEnemy.isVisible() && !smallEnemy.isVisible())
		{
			smallEnemy = new SmallEnemyImage();
			smallEnemy.setVisible(true);
		}
		else if(!bigEnemy.isVisible() && shouldRespawn())
		{
			bigEnemy = new BigEnemyImage();
			bigEnemy.setVisible(true);
		}
		else if(!smallEnemy.isVisible() && shouldRespawn())
		{
			smallEnemy = new SmallEnemyImage();
			smallEnemy.setVisible(true);
		}

		if(player.isDestroyed())
		{
			playerLivesLeft--;
			if(playerLivesLeft >= 0)
			{
				player = new PlayerImage();
				player.setVisible(true);
			}
		}
	}

	/**
	 * Remove enemy images from the screen if they have been
	 * visible for over 30 seconds.
	 */
	public void removeOldEnemies()
	{
		if(smallEnemy.getSecondsVisible() > 30)
			smallEnemy.setVisible(false);
		else if(bigEnemy.getSecondsVisible() > 30)
			bigEnemy.setVisible(false);

	}

	/**
	 * Should an enemy image respawn. This is random.
	 * @return true if they should, false otherwise
	 */
	public boolean shouldRespawn()
	{
		return ((int)(Math.random() * 100)) == 99;
	}

	/**
	 * Move all visible / non exploding images.
	 */
	public void moveImages()
	{
		player.move();
		bullet.move();
		smallEnemy.move();
		bigEnemy.move();
	}

	/**
	 * Stop the Midi music.
	 */
	public void stopMusic()
	{
		if(midiPlayer != null)
			midiPlayer.stop();
	}

	/**
	 * Start the Midi music.
	 */
	public void startMusic()
	{
		if(midiPlayer != null)
			midiPlayer.start();
	}

	/**
	 * Get how many points the player has
	 * @return the player's score
	 */
	public int getPointsTotal()
	{
		return pointsTotal;
	}

	/**
	 * Paint all visible images using the given Graphics object.
	 * @param g Graphics object to paint with
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		player.paint(g);
		smallEnemy.paint(g);
		bigEnemy.paint(g);
		bullet.paint(g);
	}

	/**
	 * Queue the player's movement.
	 * @see PlayerImage#queueMove(int) 
	 */
	public void queuePlayerMove(int direction)
	{
		player.queueMove(direction);
	}

	/**
	 * Get how many lives left the player has.
	 * @return the player's lives left
	 */
	public int getPlayerLivesLeft()
	{
		return playerLivesLeft;
	}
}