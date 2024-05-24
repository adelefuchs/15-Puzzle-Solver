import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class puzzle {
    public long seed = 0;
    private ArrayList<Integer> board;

    public puzzle(){
        board = new ArrayList<>(9);
    }

    public void setState(String state){
        //Set the puzzle state. Argument specifies puzzle
        //tile positions with a sequence of 3 groups of 3 digits
        board.clear();
        String useablestate = state.replaceAll("\\s", "");
        if (useablestate.length() != 9) {
            System.out.println("Invalid input. The puzzle state must have exactly 9 digits.");
            return;
        }

        for (char digit : useablestate.toCharArray()) {
            int num = Character.getNumericValue(digit);
            if (num < 0 || num > 8) {
                System.out.println("Invalid digit in the puzzle state: " + num);
                return;
            }
            board.add(num);
        }

    }
    
    //print out board state
    public void printState(){
        //prints current puzzle state
        for (int i = 0; i < 9; i++) {
            System.out.print(board.get(i) + " ");
            if ((i + 1) % 3 == 0) {
                System.out.println();
            }
        }
        System.out.println("---------");
    }

    //converts string state to board layout 
    public String[] stateToBoard(String state) {
        String[] newboard = new String[3];
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0) {
                newboard[i / 3] = "";
            }
            newboard[i / 3] += state.charAt(i) + " ";
        }
        return newboard;
    }

    //return the puzzle back in a straight string
    public String toString(){
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            s.append(board.get(i));
        }
        return s.toString();
    }
    
    public boolean validMove;
    public void move (String direction){
        //Move the BLANK tile 'up','down','left','right'
        int hold;
        int blankSpot = board.indexOf(0);
        //add in constraints for when each can be called
        //eg if 0 in 0-2 cant move up
       
        validMove = true;
        if(direction.equals("up")&& blankSpot>2){
            hold = board.get(blankSpot-3);
            board.set(blankSpot,hold);
            board.set(blankSpot-3,0);
        }
        else if(direction.equals("down")&& blankSpot<6){
            hold = board.get(blankSpot+3);
            board.set(blankSpot,hold);
            board.set(blankSpot+3,0);
        }
        else if(direction.equals("left") && blankSpot!=0 && blankSpot!=3 && blankSpot!= 6){
            hold = board.get(blankSpot-1);
            board.set(blankSpot,hold);
            board.set(blankSpot-1,0);
        }
        else if(direction.equals("right")&& blankSpot!=2 && blankSpot!=5 && blankSpot!= 8){
            hold = board.get(blankSpot+1);
            board.set(blankSpot,hold);
            board.set(blankSpot+1,0);
        }
        if(board.indexOf(0) == blankSpot){
            validMove = false;
            //System.out.println("Invalid Move");
        }
    }
    
    public void randomizeState(int n){
        //make n random moves from the goal state.
        //since goal not reachable from all states this ensures a soln
        String[]moves = {"up","down","left","right"};
        Random random = new Random();
        if(seed != 0){ random.setSeed(seed);}
        for(int i=0; i<n;i++){
            String randomMove = moves[random.nextInt(moves.length)];  
            move(randomMove);
            //The next two statements print the board as well to enable tracking the 0 visually through each move
            //printState();
            //System.out.println("---------");
            if (validMove == false){
                i--;
            }
            else{System.out.println(randomMove);}
        }
    }

    //method to change the seed
    public void updateSeed(long n){
        seed = n;
    }



    public static void main(String[] args){
        //check if call has any or too many txt files as argument
        if (args.length != 1) {
            System.out.println("Please input a single file");
            return;
        }

        String inputFileName = args[0]; //Holds the file's name
        puzzle myPuzzle = new puzzle(); // Create an instance of puzzle class
        searcher sSearcher = new searcher(myPuzzle); //Create a searcher for myPuzzle

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;

            //go through the lines of the text file
            while ((line = reader.readLine()) != null) {
                //split into seperate strings at spaces so seperate method names from parameters
                String[] parts = line.split(" ");

                if (parts.length == 0) {
                    continue;
                }
                //normalize
                String command = parts[0].toLowerCase();

                switch (command) {
                    case "setstate":
                        if (parts.length == 2) {//account for spaces
                            myPuzzle.setState(parts[1]);
                        } 
                        else if(parts.length > 2){
                            String holder = parts[1] + parts[2] + parts[3];
                            String combined = holder.replaceAll("\\s", "");
                            myPuzzle.setState(combined);
                        }
                        else {
                            System.out.println("Invalid setState command");
                        }
                        break;
                    case "solve":
                        if(parts[1].equals("beam")){
                            if (parts.length > 1) {
                                int n = Integer.parseInt(parts[2]);
                                sSearcher.beamSearch(n);
                            } else {
                                System.out.println("Invalid solve beam command. Missing maxNodes.");
                            }
                        }
                        else{
                            if (parts.length > 1) {
                            sSearcher.aStarSearch(parts[2]);
                            } else {
                            System.out.println("Invalid solve A Star command. Missing heuristic.");
                            }
                        }
                    break;

                    case "maxnodes":
                        if (parts.length > 1) {
                            int n = Integer.parseInt(parts[1]);
                            sSearcher.maxNodes(n);
                        }else {
                            System.out.println("Invalid maxNodes command. Missing integer parameter.");
                        }
                    
                    break;

                    case "printstate":
                        myPuzzle.printState();
                        break;

                    case "move":
                        if (parts.length > 1) {
                            myPuzzle.move(parts[1]);
                        } else {
                            System.out.println("Invalid move command. Missing direction.");
                        }
                        break;

                    case "randomizestate":
                        if (parts.length > 1) {
                            int n = Integer.parseInt(parts[1]);
                            myPuzzle.randomizeState(n);
                        } else {
                            System.out.println("Invalid randomizeState command. Missing number of moves.");
                        }
                        break;
                    case "updateseed":
                        if(parts.length>1){
                            int n = Integer.parseInt(parts[1]);
                            myPuzzle.updateSeed(n);
                        }else {
                            System.out.println("Invalid updateSeed command. Missing seed.");
                        }
                        break;

                    default:
                        System.out.println(" ");
                        System.out.println(" ");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
        }
    }

}