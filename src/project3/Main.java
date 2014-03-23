/**
 * CS 241 - project3 : Main
 * @author Adam Meily
 * Instructor: Travis Doom
 * Lab TA: Brandon Gump
 */

package project3;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;

/**
 * This is the main application for the game. It creates
 * the window (JFrame) and all the controls.
 */
public class Main extends JFrame implements KeyListener, ActionListener
{
	private Game game;
	private JButton fireButton;
	private JButton startGameButton;
	private JButton leftButton;
	private JButton rightButton;
	private JCheckBox playMusicCheck;
	private JTextField pointsField;
	private JTextField livesField;
	private JPanel gameOptions;
	private JPanel gameControls;
	private static int GAME_HEIGHT = 400;
	private static int GAME_WIDTH = 800;
	private int currentPoints;
	private int livesLeft;
	private boolean leftPressed, rightPressed;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
		Main main = new Main();
		main.initWindow();
		main.initGame();
		main.run();
    }

	/**
	 * Create the window and its components.
	 */
	public void initWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout( new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS) );
		this.addKeyListener(this);
		game = new Game();
		
		game.init(GAME_WIDTH, GAME_HEIGHT);
		game.setBorder(BorderFactory.createRaisedBevelBorder());
		game.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));

		gameOptions = new JPanel(new GridLayout(4, 1, 10, 10));
		gameControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

		// initialize all game controls
		leftButton = new JButton("<");
		leftButton.addActionListener(this);
		leftButton.setFocusable(false);
		gameControls.add(leftButton);

		fireButton = new JButton("Fire!");
		fireButton.addActionListener(this);
		fireButton.setFocusable(false);
		gameControls.add(fireButton);

		rightButton = new JButton(">");
		rightButton.addActionListener(this);
		rightButton.setFocusable(false);
		gameControls.add(rightButton);

		// initialize all game options
		playMusicCheck = new JCheckBox("Play Music", true);
		playMusicCheck.addActionListener(this);
		playMusicCheck.setFocusable(false);
		gameOptions.add(playMusicCheck);

		pointsField = new JTextField("Points: 0");
		pointsField.setEditable(false);
		pointsField.setFocusable(false);
		pointsField.setBorder(BorderFactory.createEmptyBorder());
		gameOptions.add(pointsField);

		livesField = new JTextField("Lives: 2");
		livesField.setEditable(false);
		livesField.setFocusable(false);
		livesField.setBorder(BorderFactory.createEmptyBorder());
		gameOptions.add(livesField);

		gameOptions.add(gameControls);

		add(game);
		add(gameOptions);

		pack(); // auto-size the JFrame
		setFocusable(true);
		setVisible(true);
	}

	/**
	 * Initialize the game.
	 */
	public void initGame()
	{
		leftPressed = false;
		rightPressed = false;
		currentPoints = 0;
		livesLeft = 2;
	}

	/**
	 * Default constructor.
	 */
	public Main()
	{
		super("Hobo On a Ham Sandwhich");
	}

	/**
	 * Run the game. This method does not return until the
	 * game is over.
	 */
	public void run()
	{
		game.start();
		game.startMusic();
		game.repaint();
		int won = 0;

		while(won == 0)
		{
			pause();
			game.moveImages();
			game.checkForCollisions();
			won = game.checkVictoryConditions();
			game.removeOldEnemies();
			game.respawnImages();
			game.repaint();

			if(currentPoints != game.getPointsTotal())
			{
				currentPoints = game.getPointsTotal();
				pointsField.setText("Points: " + currentPoints);
			}

			if(livesLeft != game.getPlayerLivesLeft())
			{
				livesLeft = game.getPlayerLivesLeft();
				livesField.setText("Lives: " + livesLeft);
			}
		}

		// the game is over, find out who won
		game.stopMusic();
		if(won < 0)
		{
			if(game.getPlayerLivesLeft() >= 0 && game.getRuntime() > 90)
				livesField.setText("Lives: Time ran out");
			else
				livesField.setText("Lives: You're Dead");
			
			JOptionPane.showMessageDialog(null, "You LOST!!!", "Alert",
					JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "You WON!!!", "Alert",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Pause the thread for 20 milliseconds
	 */
	private void pause()
	{
		try
		{
			Thread.sleep(20);
		}
		catch(Exception e)
		{
		}
	}

	/**
	 * Called when a key is typed. This is not used.
	 */
	public void keyTyped(KeyEvent arg)
	{

	}

	/**
	 * Called when a key is pressed.
	 * @param arg the KeyEvent thrown
	 */
	public void keyPressed(KeyEvent arg)
	{
		if(arg.getKeyCode() == KeyEvent.VK_LEFT)
		{
			game.queuePlayerMove(-1);
			leftPressed = true;
		}
		else if(arg.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			game.queuePlayerMove(1);
			rightPressed = true;
		}
		else if(arg.getKeyCode() == KeyEvent.VK_SPACE)
			game.fireBullet();
	}

	/**
	 * Called when a key is released.
	 * @param arg the KeyEveny thrown
	 */
	public void keyReleased(KeyEvent arg)
	{
		if(arg.getKeyCode() == KeyEvent.VK_LEFT)
		{
			leftPressed = false;
		}
		else if(arg.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			rightPressed = false;
		}

		if(leftPressed)
			game.queuePlayerMove(-1);
		else if(rightPressed)
			game.queuePlayerMove(1);
		else
			game.queuePlayerMove(0);
	}

	/**
	 * Called when a button / check box is clicked.
	 * @param e the ActionEvent thrown
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == startGameButton)
		{
			startGameButton.setEnabled(false);
			//run();
		}
		else if(e.getSource() == leftButton)
		{
			// this is a bad idea, but it is to discourage
			// the use of the left and right buttons instead
			// of the keyboard
			game.queuePlayerMove(-1);
			pause();
			game.queuePlayerMove(0);
		}
		else if(e.getSource() == rightButton)
		{
			game.queuePlayerMove(1);
			pause();
			game.queuePlayerMove(0);
		}
		else if(e.getSource() == fireButton)
			game.fireBullet();
		else if(e.getSource() == playMusicCheck)
		{
			if(playMusicCheck.isSelected())
				game.startMusic();
			else
				game.stopMusic();
		}
	}

}
