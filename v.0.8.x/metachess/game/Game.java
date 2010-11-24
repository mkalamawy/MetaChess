package metachess.game;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.UIManager;

import metachess.ai.AIBoardTree;
import metachess.boards.ChessBoard;
import metachess.boards.GraphicalBoard;
import metachess.builder.BuilderBox;
import metachess.dialog.ErrorDialog;
import metachess.dialog.FileBox;
import metachess.dialog.GameModeBox;
import metachess.exceptions.MetachessException;
import metachess.library.DataExtractor;
import metachess.loader.GameLoader;
import metachess.model.GameBehaviour;
import metachess.model.PanelLinkBehaviour;
import metachess.panel.MainPanel;

/** Main Class of a Metachess Game and its window
 * @author Jan and Agbeladem (7DD)
 * @version 0.8.6
 */
public class Game extends JFrame implements PanelLinkBehaviour, GameBehaviour {

    private static final long serialVersionUID = 1L;
    private boolean atomic;
    private int whiteAILevel;
    private int blackAILevel;
    private String[] AILevels = {"Human", "Very Easy", "Easy", "Average", "Master", "Elder"};
    private String setup;
    private final Menu menu;
    private final ChessBoard board;
    private final GraphicalBoard gb;
    private final MainPanel panel;
    private final GameModeBox gmBox;
    private final FileBox fileBox;
    private final BuilderBox builder;

    /** Create a new game
     * @param setup the file name of the desired setup (without the extension)
     */
    public Game(String setup) {
    	super("MetaChess");

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	this.setup = setup;

	atomic = false;
	whiteAILevel = 0;
	blackAILevel = 3;


	gmBox = new GameModeBox(this);
	fileBox = new FileBox(this);

	menu = new Menu(this);
	setJMenuBar(menu);

	builder = new BuilderBox();

	board = new ChessBoard(this);
	board.init(setup, atomic);

	gb = new GraphicalBoard(board);
	gb.init();
	gb.update();
	add(gb, BorderLayout.CENTER);

	panel = new MainPanel(this);
	add(panel, BorderLayout.EAST);
	
	pack();
	setVisible(true);

	board.launch();
	    
    }

    /** Jump to a given position of the game's logger
     * @param moves a list of all the played moves since the beginning
     */
    public void jump(ArrayList<Move> moves) {
	//newGame(moves.isEmpty());
    	if(!board.isLocked()) {
	    newGame(false);
	    board.jump(moves);
    	}
    }

 

    /** Ask for a new game with the gamebox dialog box*/
    public void askNewGame() {
    	if(!board.isLocked() && gmBox.launch()) newGame();
    }
    
    /** Start a new game */
    private void newGame() { newGame(true); }

    /** Start a new game
     * @param clear tells whether the logger should be cleared
     */
    private void newGame(boolean clear) {
	board.init(setup, atomic);
	gb.init();
	gb.update();
	clear(clear);
	if(clear)
	    board.launch();
    }
    

    /** End the last game, meaning one player has won or that it is a draw */
    public void endGame() {
    	// to replace with EndGameDialog("winner is : " + board.getWinner()); (pseudo-code)
	    AIBoardTree aiboard = new AIBoardTree(board, 1);
		System.out.println("\nGAME OVER! (Final score : " + aiboard.getBestMoveSequence().getScore() + ")");
    }

    /** Update the menu to enable/disable the Undo or Redo items
     * @param backable whether the Undo command is available
     * @param forwardable whether the Redo command is avaiable
     */
    public void updateMenu(boolean backable, boolean forwardable) {
    	menu.update(backable, forwardable);
    }

    /** Launch the file box to save the game */
    public void saveGame() {
    	if(!board.isLocked()) fileBox.launch(true);
    }

    /** Save the game to a given path
     * @param file the path
     */
    public void saveGame(File file) {
	try {
	    new SavedGame(setup, atomic, whiteAILevel, blackAILevel,
			  panel.getMoves()).save(file);
	} catch(MetachessException e) {
	    new ErrorDialog(e);
	}
    }

    /** Launch the file box to load a game */
    public void loadGame() {
    	if(!board.isLocked()) fileBox.launch(false);
    }
    
    /** Load the game contained in a specified mcg file
     * @param file the file that contains the saved game
     */
    public void loadGame(File file) {

	try {
	    GameLoader.load(file, board);
	    loadGame(GameLoader.getSavedGame());
	} catch(MetachessException e) { new ErrorDialog(e); }

    }


    // PANEL LINK BEHAVIOUR

    @Override
    public void addMove(Move m) { panel.addMove(m); }

    @Override
    public void undo() { panel.undo(); }

    @Override
    public void redo() { panel.redo(); }

    @Override
    public void clear(boolean b) { panel.clear(b); }

    @Override
    public void loadGame(SavedGame sg){

	clear(true);

	setup = sg.getSetup();
	atomic = sg.isAtomic();

	board.init(setup, atomic);
	gb.init();
	gb.update();

	whiteAILevel = 0;
	blackAILevel = 0;

	ArrayList<Move> moves = sg.getMoves();
	int n = moves.size();
	for(int i = 0 ; i < n ; i++)
	    board.playMove(moves.get(i));

	whiteAILevel = sg.getWhiteAILevel();
	blackAILevel = sg.getBlackAILevel();

	board.launch();
    }

    @Override
    public void count(String pieceName, boolean isWhite) {
    	panel.count(pieceName, isWhite);
    }


    // GAME BEHAVIOUR

    @Override
    public boolean isAtomic() { return atomic; }

    @Override
    public int getWhiteAILevel() { return whiteAILevel; }

    @Override
    public int getBlackAILevel() { return blackAILevel; }

    @Override
    public String getSetup() { return setup; }

    @Override
    public void setSetup(String s) { setup = s; }

    @Override
    public void setWhiteAILevel(int wAI) { whiteAILevel = wAI; }

    @Override
    public void setBlackAILevel(int bAI) { blackAILevel = bAI; }

    @Override
    public void setAtomic(boolean a) { atomic = a; }

    @Override
    public ArrayList<Move> getMoves() {
	return null;
    }




    public void launchBuilder() {
	builder.launch();
    }

    public int getMaxAILevel() { return AILevels.length; }
    public String[] getAILevels() { return AILevels; }
    public boolean isBoardLocked() { return board.isLocked(); }

    public static void main(String[] argv) {
	DataExtractor.checkDataVersion();
	try {
	    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
	} catch(Exception e) {}
			
	new Game(argv.length == 1 ? argv[0] : "classic"); 
    }


}

