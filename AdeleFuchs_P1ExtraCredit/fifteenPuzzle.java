import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class fifteenPuzzle {

    public long seed = 0;
    private ArrayList<Integer> board;

     public fifteenPuzzle(){
        board = new ArrayList<>(16);
    }

    public void setState(String state){
        //Set the puzzle state. Argument specifies puzzle
        //tile positions with a sequence of 4 groups of 4 digits
        state.trim();
        board.clear();
        String[] parts = state.split(" ");
        if (parts.length != 16) {
            System.out.println("Invalid input. The puzzle state must have exactly 15 numbers.");
            return;
        }

        for (String number : parts) {
            int num = Integer.parseInt(number);
            if (num < 0 || num > 15) {
                System.out.println("Invalid digit in the puzzle state: " + num);
                return;
            }
            board.add(num);
        }

    }

    public void printState(){
        //prints current puzzle state
        for (int i = 0; i < 16; i++) {
            System.out.print(board.get(i) + " ");
            if ((i + 1) % 4 == 0) {
                System.out.println();
            }
        }
        System.out.println("---------");
    }

    public String[] stateToBoard(String state) {
        String[] newboard = new String[4];
        String[] stateTokens = state.trim().split("\\s+");
        for (int i = 0; i < 16; i++) {
            if (i % 4 == 0) {
                newboard[i / 4] = "";
            }
            newboard[i / 4] += stateTokens[i] + " ";
        }
        return newboard;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            s.append(board.get(i)+ " ");
        }
        return s.toString();
    }

    boolean validMove;
    public void move (String direction){
        //Move the BLANK tile 'up','down','left','right'
        int hold;
        int blankSpot = board.indexOf(0);
        //add in constraints for when each can be called
        //eg if 0 in 0-2 cant move up
       
        validMove = true;
        if(direction.equals("up")&& blankSpot>3){
            hold = board.get(blankSpot-4);
            board.set(blankSpot,hold);
            board.set(blankSpot-4,0);
        }
        else if(direction.equals("down")&& blankSpot<12){
            hold = board.get(blankSpot+4);
            board.set(blankSpot,hold);
            board.set(blankSpot+4,0);
        }
        else if(direction.equals("left") && blankSpot!=0 && blankSpot!=4 && blankSpot!= 8 && blankSpot !=12){
            hold = board.get(blankSpot-1);
            board.set(blankSpot,hold);
            board.set(blankSpot-1,0);
        }
        else if(direction.equals("right")&& blankSpot!=3 && blankSpot!=7 && blankSpot!= 11 && blankSpot!= 15){
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
            if (validMove == false){
                i--;
            }
            else{
                System.out.println(randomMove);
            }
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
        fifteenPuzzle fp = new fifteenPuzzle(); // Create an instance of fifteenPuzzle class
        solver sSolver = new solver(fp); //Create a solver for fp

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
                        StringBuilder holder = new StringBuilder();
                        if(parts.length == 17){
                            for(int i=1; i<=16; i++){
                                holder.append(parts[i] + " ");
                            }
                            String combined = holder.toString();
                            fp.setState(combined);
                        }
                        else {
                            System.out.println("Invalid setState command");
                        }
                    break;
                    case "solve":
                        if(parts[1].equals("beam")){
                            if (parts.length > 1) {
                                int n = Integer.parseInt(parts[2]);
                                sSolver.beamSearch(n);
                            } else {
                                System.out.println("Invalid solve beam command. Missing maxNodes.");
                            }
                        }
                        else{
                            if (parts.length > 1) {
                            sSolver.aStarSearch(parts[2]);
                            } else {
                            System.out.println("Invalid solve A Star command. Missing heuristic.");
                            }
                        }
                    break;

                    case "maxnodes":
                        if (parts.length > 1) {
                            int n = Integer.parseInt(parts[1]);
                            sSolver.maxNodes(n);
                        }else {
                            System.out.println("Invalid maxNodes command. Missing integer parameter.");
                        }
                    
                    break;

                    case "printstate":
                        fp.printState();
                        break;

                    case "move":
                        if (parts.length > 1) {
                            fp.move(parts[1]);
                        } else {
                            System.out.println("Invalid move command. Missing direction.");
                        }
                        break;

                    case "randomizestate":
                        if (parts.length > 1) {
                            int n = Integer.parseInt(parts[1]);
                            fp.randomizeState(n);
                        } else {
                            System.out.println("Invalid randomizeState command. Missing number of moves.");
                        }
                        break;
                    case "updateseed":
                        if(parts.length>1){
                            int n = Integer.parseInt(parts[1]);
                            fp.updateSeed(n);
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