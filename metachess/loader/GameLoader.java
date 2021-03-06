package metachess.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import metachess.board.PlayableBoard;
import metachess.exception.FileAccessException;
import metachess.exception.FileContentException;
import metachess.exception.LoadException;
import metachess.game.Coords;
import metachess.game.Move;
import metachess.game.SavedGame;

/** Singleton of the Game Loader
 * @author Agbeladem (7DD)
 * @version 0.8.6
 */
public class GameLoader extends VariableLoader {

    private static GameLoader instance = new GameLoader();

    private PlayableBoard board;
    private SavedGame sg;

    @Override
    public void loadResource(String file) throws LoadException {
	sg = new SavedGame();
	try {
	    this.file = file;
	    BufferedReader br = new BufferedReader(new FileReader(file));

	    String line = br.readLine();
	    while(line.indexOf("{BEGIN}") == -1 && line != null) {
		readVariable(line);
		line = br.readLine();
	    }

	    StreamTokenizer st = new StreamTokenizer(br);
	    st.eolIsSignificant(true);
	    st.wordChars('0', '9');
	    st.wordChars('_', '_');
	    int next = st.nextToken();
	    while(next != StreamTokenizer.TT_EOF) {
			if(next == StreamTokenizer.TT_WORD) {
			    String move = st.sval;
			    if(move.length() >= 4) {
				boolean promotion = move.length() > 4;
				char a = move.charAt(0);
				char b = move.charAt(1);
				char c = move.charAt(2);
				char d = move.charAt(3);
				if(!Coords.isValid(a, b))
				    throw new FileContentException("Bad Coords format : "+a+b, file);
				else if(!Coords.isValid(c, d))
				    throw new FileContentException("Bad Coords format : "+c+d, file);
				else {
				    Move m = new Move(new Coords(a, b), new Coords(c, d), 0, board);
				    if(promotion) if(move.charAt(4) != '_') throw new FileContentException ("Bad BCG format : "+move, file);
					else m.setPromotionPiece(move.substring(5, move.length()));
				    next = st.nextToken();
				    if(next == StreamTokenizer.TT_NUMBER)
					m.setTime((long)st.nval);
				    else
					st.pushBack();
				    sg.addMove(m);
				}
			    } else throw new FileContentException("Bad Move Format : "+move, file);
			} else if(next != StreamTokenizer.TT_EOL) throw new FileContentException("Invalid token value : "+next, file);
			next = st.nextToken();
	    }
	    br.close();
	} catch(IOException e) {
	    throw new FileAccessException(file);
	}
    }

    protected void setVariable(String name, String value) throws FileContentException {

	if(name.equals("atomic"))
	    sg.setAtomic(value.equals("true"));
	else if(name.equals("setup"))
	    sg.setSetup(value);
	else if(name.equals("whitelevel"))
	    sg.setWhiteAILevel(Integer.parseInt(value));
	else if(name.equals("blacklevel"))
	    sg.setBlackAILevel(Integer.parseInt(value));
 	else throw new FileContentException("Unknown variable \""+name+'"', file);
	
    }

    /** Get the saved game model that was loaded by this game loader
     * @return the game model
     */
    public static SavedGame getSavedGame() {
    	return instance.sg;
    }

    /** Load a saved game
     * @param file the file in which the game is saved
     * @param board the board in which this will be
     */
    public static void load(File file, PlayableBoard board) throws LoadException {
	instance.board = board;
	instance.loadResource(file.getPath());
    }

}

