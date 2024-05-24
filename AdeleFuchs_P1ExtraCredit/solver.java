import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;



public class solver {

    private static final String goalState= "0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 ";
    private static final String[] movesList = {"up", "down", "left", "right"};
    private int maxNodes = 100000000;
    public int nodesCreated;
    private fifteenPuzzle p; // Reference to the fifteenpuzzle object

    public solver(fifteenPuzzle p) {
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
            for (int i = 0; i < 16; i++) {
             char tile = state.charAt(i);
                if (tile != '0') {
                    int goalIndex = goalState.indexOf(tile);
                    int goalRow = goalIndex / 4;
                    int goalCol = goalIndex % 4;
                    int currentRow = i / 4;
                    int currentCol = i % 4;
                    h += Math.abs(goalRow - currentRow) + Math.abs(goalCol - currentCol);
                }
            }
        return h;
        }

        private int calculateHeuristic1(String state) { //Misplaced Tiles
            int misplacedTiles = 0;
            for (int i = 0; i < 16; i++) {
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
        for (String direction : movesList) {
            fifteenPuzzle holder = new fifteenPuzzle();
            holder.setState(currentStateStr);
            holder.move(direction);
            String movedState = holder.toString();
            if(!movedState.equals(currentStateStr)){
                Node neighborNode = new Node(movedState, currentNode.g + 1, currentNode, direction, heuristic);
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

public static void main(String[] args){
    fifteenPuzzle fp = new fifteenPuzzle();
    fp.setState("0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    //fp.printState();
    solver s = new solver(fp);
    fp.updateSeed(678);
    //fp.randomizeState(119);
    //fp.printState();
    fp.setState("1 5 2 7 8 3 6 11 9 13 10 15 12 14 4 0");
    s.aStarSearch("h1");
    //s.beamSearch(5);
}
}