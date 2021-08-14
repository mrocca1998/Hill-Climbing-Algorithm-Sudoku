import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Hill CLimbing Soduko agent
 *
 * @author Michael Rocca.
 */
public final class HillClimber {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private HillClimber() {
        // no code needed here
    }

    public static boolean contains(int[] array, int value) {
        for (int num : array) {
            if (value == num) {
                return true;
            }
        }
        return false;
    }

    //function to get the value of a particular space
    public static int getSpace(int i, String board, List<Integer> state) {
        char space = board.charAt(i);
        int count = 0;
        //getting a state space
        if (space == '*') {
            for (int j = 0; j <= i; j++) {
                if (board.charAt(j) == '*') {
                    count++;
                }
            }
            return state.get(count - 1);
        } else {
            //getting a permanent space
            return Character.getNumericValue(space);
        }
    }

    //reads the input file and creates the board vector
    public static String readInputBoard(BufferedReader file)
            throws IOException {
        String board = "";
        String str;
        file.readLine();
        while ((str = file.readLine()) != null) {
            board += str;
        }
        return board;
    }

    public static int evaluate(String board, List<Integer> state) {
        int conflicts = 0;
        int checkSpaceValue = 0;
        int currentSpaceValue;
        int checkCount = 0;
        int[][] checks = { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 }, { 0, 4, 8, 12 }, { 1, 5, 9, 13 },
                { 2, 6, 10, 14 }, { 3, 7, 11, 15 }, { 0, 1, 4, 5 },
                { 2, 3, 6, 7 }, { 8, 9, 12, 13 }, { 10, 11, 14, 15 } };
        //check conflicts at each space
        for (int i = 0; i < board.length(); i++) {
            //get the value of current space being checked
            currentSpaceValue = getSpace(i, board, state);
            //check for conflicts in each row, column box, containing space i
            for (int j = 0; j < 12; j++) {
                //inspect a col/row/box that includes the current space
                if (contains(checks[j], i)) {
                    checkCount = 0;
                    //look at each space in that col/row/box and count how many of the spaces are the same as the current space
                    for (int k = 0; k < 4; k++) {
                        checkSpaceValue = getSpace(checks[j][k], board, state);
                        if (currentSpaceValue == checkSpaceValue) {
                            checkCount++;
                        }
                    }
                    //if there are 2+ instances of the current value being checked in a row/col/box, log a conflict
                    if (checkCount > 1) {
                        conflicts++;
                    }
                }
            }
        }
        return conflicts;
    }

    public static List<Integer> successorFunction(String board,
            List<Integer> state) {
        List<Integer> lowestSuccessor = new LinkedList<Integer>();
        lowestSuccessor.addAll(state);
        List<Integer> working = new LinkedList<Integer>();
        working.addAll(state);
        for (int i = 0; i < state.size(); i++) {
            //copy current state to new array
            Collections.copy(working, state);
            for (int j = 1; j < 5; j++) {
                working.set(i, j);
                //for possible successors, compare that successor to the current highest succesor
                if (working.get(i) != state.get(i)) {
                    if (evaluate(board, working) < evaluate(board,
                            lowestSuccessor)) {
                        Collections.copy(lowestSuccessor, working);
                    }
                }
            }
        }
        return lowestSuccessor;
    }

    //fill the state vector with random values 1-4 for all open spaces
    public static void getInitialState(String board, List<Integer> state) {
        Random random = new Random();
        state.clear();
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == '*') {
                state.add(random.ints(1, 5).findFirst().getAsInt());
            }
        }
    }

    //hillClimber function that
    public static void hillClimber(String board, List<Integer> state,
            SimpleWriter out) {
        int totalIterations = 0;
        int currentIterations = 0;
        //keep searching until solution found
        int maxIterations = 16;
        while (evaluate(board, state) > 0) {
            if (currentIterations == maxIterations) {
                currentIterations = 0;
                //random restart
                getInitialState(board, state);
                out.println("Random Restart!");
                out.println();
            } else {
                currentIterations++;
                totalIterations++;
                //generate next best state
                List<Integer> successor = successorFunction(board, state);
                //compare next to current
                if (evaluate(board, successor) < evaluate(board, state)) {
                    //choose next state if next > current
                    Collections.copy(state, successor);
                }
                //print results of iteration
                out.println("Iteration " + totalIterations + ":");
                out.print("Chosen state: ");
                for (int i = 0; i < state.size() - 1; i++) {
                    out.print(state.get(i) + ", ");
                }
                out.print(state.get(state.size() - 1));
                out.println();
                out.println(
                        "State evaluation value: " + evaluate(board, state));
                out.println();
            }
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        BufferedReader inputFile = null;
        String board = "";

        //check command usage

        if (args.length != 1) {
            System.err.println("Usage: HillClimber <filename>");
            System.exit(1);
        }
        //open input file
        try {
            inputFile = new BufferedReader(new FileReader(args[0]));
        } catch (Exception e) {
            System.err.println(
                    "Ooops!  I can't seem to load the file \"" + args[0]
                            + "\", do you have the file in the correct place?");
            System.exit(1);
        }

        //LinkedLists for current state and whole board
        List<Integer> state = new LinkedList<Integer>();

        //read input file and save initial Board as a string
        try {
            board = readInputBoard(inputFile);
        } catch (Exception e) {
            System.err.println("Error reading file");
            System.exit(1);
        }

        //initialize the blank spaces on the board in the state vector
        getInitialState(board, state);

        /*
         * //run the hill climber algorithm. The final state is in spaceVector
         * when the hill Climber is complete
         */
        hillClimber(board, state, out);

        out.println("Solution found!");

        for (int i = 0; i < state.size() - 1; i++) {
            out.print(state.get(i) + ", ");
        }
        if (state.size() > 0) {
            out.print(state.get(state.size() - 1));
        }
    }

}
