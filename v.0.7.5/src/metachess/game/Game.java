package metachess.game;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;

import metachess.boards.ChessBoard;
import metachess.boards.GraphicalBoard;
import metachess.builder.BuilderBox;
import metachess.logger.LogPanel;
import metachess.logger.Move;
import metachess.menus.FileBox;
import metachess.menus.GameModeBox;
import metachess.menus.Menu;

/** Main Class of a Metachess Game and its window
 * @author Jan and Agbeladem (7DD)
 * @version 0.8.0
 */
public class Game extends JFrame {

    private static final long serialVersionUID = 1L;
    private boolean atomic;
    private int whiteAILevel;
    private int blackAILevel;
    private final int maxAILevel;
    private String setup;
    private final Menu menu;
    private final ChessBoard board;
    private final GraphicalBoard gb;
    private final LogPanel histo;
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
    	maxAILevel = 4;

    	builder = new BuilderBox();
    	gmBox = new GameModeBox(this);
    	fileBox = new FileBox(this);

    	menu = new Menu(this);
    	setJMenuBar(menu);
	
    	board = new ChessBoard(this);
    	board.init(setup, atomic);
    	gb = new GraphicalBoard(board);
    	gb.init();
    	add(gb, BorderLayout.CENTER);

    	histo = new LogPanel(this);
    	add(histo, BorderLayout.EAST);

    	pack();
    	setVisible(true);
		
    }


    /** Jump to a given position of the game's logger
     * @param moves a list of all the played moves since the beginning
     */
    public void jump(ArrayList<Move> moves) {
    	newGame(moves.isEmpty());
    	board.jump(moves);
    }

    /** Ask for a new game with the gamebox dialog box*/
    public void askNewGame() {
    	if(gmBox.launch())
    		newGame();
    }
    
    /** Start a new game */
    public void newGame() { newGame(true); }

    /** Start a new game
     * @param clear tells whether the logger should be cleared
     */
    public void newGame(boolean clear){
    	board.init(setup, atomic);
    	gb.init();
    	if(clear) {
    		histo.clearMoves();
    		histo.update();
    	}

    }


    /** End the last game */
    public void endGame() {
    	newGame();
    }

    /** Add a move to the logger
     * @param m the move
     */
    public void addMove(Move m) {
    	histo.addMove(m);
    }

    /** Undo last move */
    public void undo(){
    	histo.undo();
    }

    /** Redo last undone move */
    public void redo() {
    	histo.redo();
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
    	fileBox.launch(true);
    }

    /** Save the game to a given path
     * @param file the path
     */
    public void saveGame(File file) {
    	try {
    	    ObjectOutputStream s;
    	    s = new ObjectOutputStream(new FileOutputStream(file));
    	    s.writeObject(getSavedGame());
    	    s.close();
    	} catch(Exception e) { System.out.println(e); }
    }
    
    public SavedGame getSavedGame() {
    	return new SavedGame(setup, atomic, whiteAILevel, blackAILevel, histo.getMoves());
    }

    public void loadGame() {
    	fileBox.launch(false);
    }
    public void loadGame(File file) {
    	try {
    	    ObjectInputStream s;
    	    s = new ObjectInputStream(new FileInputStream(file));
    	    SavedGame sg = (SavedGame)(s.readObject());
    	    loadGame(sg);
    	    s.close();
    	}
    	catch(Exception e) { System.out.println(e); }
    }
    
    public void loadGame(SavedGame sg){
	    setup = sg.getSetup();
	    atomic = sg.isAtomic();
	    whiteAILevel = sg.getAILevel(true);
	    blackAILevel = sg.getAILevel(false);
	    histo.loadGame(sg.getMoves());
    }


    public void launchBuilder() {
	builder.launch();
    }

    public boolean isAtomic() { return atomic; }
    
    public int getAILevel(boolean white) { return (white ? whiteAILevel : blackAILevel); }
    public int getMaxAILevel() { return maxAILevel; }

    public void setSetup(String s) { setup = s; }
    public void setWhiteAILevel(int wAI) { whiteAILevel = wAI; }
    public void setBlackAILevel(int bAI) { blackAILevel = bAI; }
    public void setAtomic(boolean a) { atomic = a; }
    

    public static void main(String[] argv) {
	new Game(argv.length == 1 ? argv[0] : "classic"); 
    }


}

