import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;



public class searcher {

    private static final String goalState= "012345678";
    private static final String[] movesList = {"up", "down", "left", "right"};
    private int maxNodes = 100000000;
    public int nodesCreated;
    private puzzle p; // Reference to the puzzle object

    public searcher(puzzle p) {
        this.p = p;
    }
    public searcher(fifteenPuzzle p) {
        this.p = p;
    }

    private static class Node implements Comparable<Node> {
        
        String state;
        int g; // Cost from start to current node
        int h; // Heuristic: sum of distances from tiles to their goal positions
        Node parent;
        String move; //move from parent to this node
        String heuristic; // the heuristic used to calculate values
        

        public Node(String state, int g, Node parent, String move, String heuristic) {
            this.state = state;
            this.g = g;
            this.heuristic = heuristic;

            if(heuristic.equals("h1")){
                this.h = calculateHeuristic1(state);
            }
            else if(heuristic.equals("h2")){
                this.h = calculateHeuristic2(state);
            }
            else{
                this.h = beamEval(state);
            }
            this.parent = parent;
            this.move = move;
        }

        // Compare nodes based on f = g + h
        public int compareTo(Node other) {
            if(this.heuristic.equals("beam")){
                return Integer.compare(this.h, other.h);
            }
            else return Integer.compare(this.g + this.h, other.g + other.h);
        }

        private int calculateHeuristic2(String state) {//Manhatten
            int h = 0;
            for (int i = 0; i < 9; i++) {
             char tile = state.charAt(i);
                if (tile != '0') {
                    int goalIndex = goalState.indexOf(tile);
                    int goalRow = goalIndex / 3;
                    int goalCol = goalIndex % 3;
                    int currentRow = i / 3;
                    int currentCol = i % 3;
                    h += Math.abs(goalRow - currentRow) + Math.abs(goalCol - currentCol);
                }
            }
        return h;
        }

        private int calculateHeuristic1(String state) { //Misplaced Tiles
            int misplacedTiles = 0;
            for (int i = 0; i < 9; i++) {
                if (state.charAt(i) != '0' && state.charAt(i) != goalState.charAt(i)) {
                    misplacedTiles++;
                }
            }
            return misplacedTiles;
        }

        private int beamEval(String stateString){
            int h2 = calculateHeuristic2(stateString);
            int h1 = calculateHeuristic1(stateString);
            return h1+h2;
        }
    }

    

    private List<Node> generateNextNodes(Node currentNode, String heuristic) {
        List<Node> neighbors = new ArrayList<>();
        String currentStateStr = currentNode.state;
        int blankIndex = currentStateStr.indexOf('0');
        int row = blankIndex / 3;
        int col = blankIndex % 3;
    
        for (String direction : movesList) {
            int newRow = row;
            int newCol = col;
    
            switch (direction) {
                case "up":
                    newRow--;
                    break;
                case "down":
                    newRow++;
                    break;
                case "left":
                    newCol--;
                    break;
                case "right":
                    newCol++;
                    break;
            }
    
            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                char[] newState = currentStateStr.toCharArray();
                int newIndex = newRow * 3 + newCol;
                newState[blankIndex] = currentStateStr.charAt(newIndex);
                newState[newIndex] = '0';
    
                String newStateStr = new String(newState);
                String move = direction; // Set the move
    
                Node neighborNode = new Node(newStateStr, currentNode.g + 1, currentNode, move, heuristic);
                nodesCreated++;
                neighbors.add(neighborNode);
            }
        }
    
        return neighbors;
    }

    private static class MoveInfo {
    String move;
    String state;

    public MoveInfo(String move, String state) {
        this.move = move;
        this.state = state;
    }
}
    
    private static List<MoveInfo> tracePath(Node goalNode) {
    // Trace back the path from the goal state to the initial state
    List<MoveInfo> path = new ArrayList<>();
    Node currentNode = goalNode;

    while (currentNode != null) {
        path.add(new MoveInfo(currentNode.move, currentNode.state));
        currentNode = currentNode.parent;
    }

    Collections.reverse(path);
    return path;
}


    boolean solvedOrNot; // for experiments
    public void aStarSearch(String heurtistic) {
    nodesCreated = 0;
    PriorityQueue<Node> openList = new PriorityQueue<>();
    HashMap<String, Integer> gMap = new HashMap<>();

    Node initialNode = new Node(p.toString(), 0, null, null, heurtistic);
    nodesCreated++;
    openList.add(initialNode);
    gMap.put(initialNode.state, 0);

    while (!openList.isEmpty()) {
        if(nodesCreated>maxNodes){
            System.out.println("Max Nodes Exceeded. NodesCreated: "+ nodesCreated);
            break;
        }
        Node currentNode = openList.poll();
        String currentStateStr = currentNode.state;

        if (currentStateStr.equals(goalState)) {
            
            solvedOrNot=true;
             List<MoveInfo> path = tracePath(currentNode);
            int numMoves = path.size() - 1; // Exclude the initial state
            System.out.println("Number of moves to reach the goal: " + numMoves);
            System.out.println("Num nodes created: " + nodesCreated);
            System.out.println("Sequence of moves and states:");
            for (MoveInfo moveInfo : path) {
                System.out.println("Move: " + moveInfo.move);
                String[] board = p.stateToBoard(moveInfo.state);
                    for (String row : board) {
                    System.out.println(row);
                } // Use the puzzle method to format the board
                System.out.println("------");
            }
            break;
        }

        List<Node> nextNodes = generateNextNodes(currentNode,heurtistic);

        for (Node nextNode : nextNodes) {
            int newGCost = gMap.get(currentNode.state) + 1;

            //tracks seen states with better g value so we dont get into a loop
            if (!gMap.containsKey(nextNode.state) || newGCost < gMap.get(nextNode.state)) {
                gMap.put(nextNode.state, newGCost);
                openList.add(nextNode);
            }
        }
    }
}

    public void beamSearch(int k){
    PriorityQueue<Node> currentStates = new PriorityQueue<>();
    nodesCreated=0;
    //add initial state to priority queue and to the visited list
    Node initialNode = new Node(p.toString(),0,null,null, "beam");
    currentStates.add(initialNode);
    nodesCreated++;
    //fill queue with next k states
    boolean goalFound = false;

    while(nodesCreated<maxNodes && (!goalFound)){
        List<Node> nodesToAdd = new ArrayList<>();//temporary list to avoid concurrent modification of queue
        for (Node currentNode : currentStates) {
            String currentStateStr = currentNode.state;
            //check if goal state found
            if (currentStateStr.equals(goalState)) {
                goalFound = true;
                System.out.println("Solution:");
                //if found trace back path
                List<MoveInfo> path = tracePath(currentNode);
                int numMoves = path.size() - 1; // Exclude the initial state
                System.out.println("Number of moves to reach the goal: " + numMoves);
                System.out.println("Sequence of moves and states:");
                for (MoveInfo moveInfo : path) {
                System.out.println("Move: " + moveInfo.move);
                String[] board = p.stateToBoard(moveInfo.state);
                    for (String row : board) {
                    System.out.println(row);
                }
                System.out.println("------");
                }
                break;
            }
            else{
                //if state polled not goal state, add get all next Nodes but only add next k best to queue
                List<Node> nextNodes = generateNextNodes(currentNode,"beam");
                nextNodes.sort((a, b) -> a.compareTo(b));

                for(int i = 0; i < Math.min(k, nextNodes.size()); i++){
                    nodesToAdd.add(nextNodes.get(i));
                    
                    if (nodesCreated >= maxNodes) {
                        System.out.println("Maximum number of nodes reached. Aborting search.");
                        System.out.println("Num Nodes Created: " + nodesCreated);
                        return;
                    }
                }
            }
        }
        // Add nodes from the temporary list to the priority queue
        currentStates.addAll(nodesToAdd);
    }
    System.out.println("Num Nodes Created: " + nodesCreated);
}

    public void maxNodes(int n){
    //This is the number of nodes created, which is not (necessarily) the same as the
    //number of nodes that need to be stored, depending on the algorithm.
    maxNodes = n;
}

    public void experimentA(){
    int[] maxNodeLimit = {10,50,100,500,1000,5000,10000,50000,100000,500000};
    puzzle p = new puzzle(); 
    p.setState("012345678");
    p.updateSeed(678);
    for(int j = 0;j<10;j++){
        searcher s = new searcher(p);
        s.maxNodes(maxNodeLimit[j]);
        for(int i =5; i<=50; i=i+5){
            p.randomizeState(i);
            System.out.println("RANDOMIZED: " + i + "LIMIT:  "+ maxNodeLimit[j]);
            s.aStarSearch("h2");
            //s.beamSearch(5);
        }
    }
}

    public void experimentB(){
    puzzle p = new puzzle(); 
    p.setState("012345678");
    p.updateSeed(678);
    searcher s = new searcher(p);
    for(int i =5; i<=100; i=i+10){
        p.randomizeState(i);
        System.out.println("RANDOMIZED: " + i);
        //s.aStarSearch("h2");
        s.beamSearch(5);
    }
    p.setState("012345678");
    //p.randomizeState(45);
    //s.beamSearch(5);
    //p.randomizeState(25);
    //s.beamSearch(5);

}

//need to show test cases - multiple files of diff commands 
    public static void main(String[] args) {
    puzzle p = new puzzle();
    searcher m = new searcher(p);
    //m.experimentB();
    //p.updateSeed(678);
    p.updateSeed(678);
    p.setState("012345678");
    p.randomizeState(5);
    m.aStarSearch("h2");
    //m.beamSearch(5);
    //:D
    

}
}