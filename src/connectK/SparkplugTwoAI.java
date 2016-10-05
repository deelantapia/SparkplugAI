//	Deelan Tapia 81875361
//	CS 171 AI project

import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class SparkplugTwoAI extends CKPlayer{
	public class Node implements Comparable<Node>{
		private BoardModel state;
		private double value;
		private List<Node> children;
		private Point newlyPlacedPoint;
		
		public Node(BoardModel st){
			state = st;
			value = -1;
			children = new ArrayList<Node>();
			newlyPlacedPoint = null;
			
		}

		@Override
		public int compareTo(Node n) {
			int compareValue = (int) n.getValue();
			// allows for descending order
			return  (compareValue - (int)this.getValue());
		}
		
				
		public BoardModel getState(){
			return state;
		}
		
		public double getValue(){
			return value;
		}

		public void setValue(double v) {
			value = v;
		}

		public Point getNewlyPlacedPoint() {
			return newlyPlacedPoint;
		}

		public void setNewlyPlacedPoint(Point newPoint) {
			newlyPlacedPoint = newPoint;
		}
	}
	
	public class Tree{
		public Node root;
		public Tree(BoardModel state){
			root =  new Node(state);
		}
	}

	private Tree gameTree;
	private long stopTime;
	private byte opponent;
	private double val;
	private boolean bCutOff;
	private List<Node> listOfNodes;
	
	public SparkplugTwoAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "connectK.SparkplugTwoAI";
		gameTree = null;
		opponent = (byte)((player == 1) ? 2 : 1);
		bCutOff = false;
	}

	@Override
	public Point getMove(BoardModel state) {
		if(isFirstMove(state)){
			int x = 0;
			int y = 0;

			while(state.getSpace(x, y) == opponent){
				x = (int)(Math.random() * state.getWidth());
				y = (int)(Math.random() * state.getHeight());
			}

			return new Point(x, y);
		} else {
			return iDS_ABPruning(state);
		}
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		stopTime = System.currentTimeMillis() + deadline - 250;
		return getMove(state);
	}

	private boolean isFirstMove(BoardModel st) {
		for(int i = 0; i < st.getWidth(); i++) {
			for(int j = 0; j < st.getHeight(); j++) {
				if(st.getSpace(i, j) == player) {
					return false;
				}
			}
		}

		return true;
	}
	
	private double eval(BoardModel state){
		double value = 0;
		int kLen = state.getkLength();
		int kLenArray[] = new int[kLen];
		int connectLen[] = new int[8];
		/*
		connecttLen[0] is up
		connecttLen[1] is up-right
		connecttLen[2] is right
		connecttLen[3] is down-right
		connecttLen[4] is down
		connecttLen[5] is down-left
		connecttLen[6] is left
		connecttLen[7] is up-left
		*/  
		for(int i = 0; i < state.getWidth(); i++) {
			for(int j = 0; /*!timeIsUp() &&*/ j < state.getHeight(); j++){
				if(state.getSpace(i, j) == this.player) {
					// UP: count the number of connecting pieces from origin
					for(int up = 1; up < kLen; up++) {
						// Check within the bounds of the board dimensions
						if((j + up) < state.getHeight()){
							if(state.getSpace(i, j + up) == this.player) {
								++connectLen[0];
							}
							// if (i,j) is an opponent space than reset UP direction connect length value to zero
							else if(state.getSpace(i, j + up) == opponent) {
								connectLen[0] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// UP-RIGHT DIAGONAL: count the number of connecting pieces from origin
					for(int upRight = 1; upRight < kLen; upRight++) {
						// Check within the bounds of the board dimensions
						if((i + upRight) < state.getWidth() && (j + upRight) < state.getHeight()){
							if(state.getSpace(i + upRight, j + upRight) == this.player) {
								++connectLen[1];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i + upRight, j + upRight) == opponent) {
								connectLen[1] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// RIGHT: count the number of connecting pieces from origin
					for(int right = 1; right < kLen; right++) {
						// Check within the bounds of the board dimensions
						if((i + right) < state.getWidth()) {
							if(state.getSpace(i + right, j) == this.player){
								++connectLen[2];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i + right, j) == opponent) {
								connectLen[2] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// DOWN-RIGHT DIAGONAL: count the number of conneting pieces from origin
					for(int downRight = 1; downRight < kLen; downRight++) {
						// Check within the bounds of the board dimensions
						if((i + downRight) < state.getWidth() && (j - downRight) >= 0) {
							if(state.getSpace(i + downRight, j - downRight) == this.player){
								++connectLen[3];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i + downRight, j - downRight) == opponent) {
								connectLen[3] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// DOWN: count the number of connecting pieces from origin
					for(int down = 1; down < kLen; down++) {
						// Check within the bounds of the board dimensions
						if((j - down) >= 0) {
							if(state.getSpace(i, j - down) == this.player){
								++connectLen[4];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i, j - down) == opponent) {
								connectLen[4] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// DOWN-LEFT DIAGONAL: count the number of conneting pieces from origin
					for(int downLeft = 1; downLeft < kLen; downLeft++) {
						// Check within the bounds of the board dimensions
						if((i - downLeft) >= 0 && (j - downLeft) >= 0) {
							if(state.getSpace(i - downLeft, j - downLeft) == this.player){
								++connectLen[5];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i - downLeft, j - downLeft) == opponent) {
								connectLen[5] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					// LEFT DIAGONAL: count the number of connecting pieces from origin
					for(int Left = 1; Left < kLen; Left++) {
						// Check within the bounds of the board dimensions
						if((i - Left) >= 0 && state.getSpace(i - Left, j) == this.player){
							++connectLen[6];
						}
						// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
						else if((i - Left) >= 0 && state.getSpace(i - Left, j) == opponent) {
							connectLen[6] = 0;
							break;
						}
						// if (i,j) is empty than stop counting
						else {
							break;
						}
					}

					// UP-LEFT DIAGONAL: count the number of connecting pieces from origin
					for(int upLeft = 1; upLeft < kLen; upLeft++) {
						// Check within the bounds of the board dimensions
						if((i - upLeft) >= 0 && (j + upLeft) < state.getHeight()){
							if(state.getSpace(i - upLeft, j + upLeft) == this.player){
								++connectLen[7];
							}
							// if (i,j) is an opponent space than reset UP-RIGHT direction connect length value to zero
							else if(state.getSpace(i - upLeft, j + upLeft) == opponent) {
								connectLen[7] = 0;
								break;
							}
							// if (i,j) is empty than stop counting
							else {
								break;
							}
						}
					}

					for(int x = 0; x < 8; x++) {
						++kLenArray[connectLen[x]];
					}

					connectLen = new int[8];
				}
			}
		}

		// Do not count connect length of one
		for(int m = 1; m < kLen; m++){
			value += (m * kLenArray[m]);
		}

		return value;
	}
	
	private Point iDS_ABPruning(BoardModel state){
		gameTree = new Tree(state);
		Point result = null;
		Point newPoint;

		for(int d = 0; !timeIsUp() && d < Integer.MAX_VALUE; d++){
			newPoint = alphaBetaPruning(gameTree.root, d);
			if(newPoint != null) {
				result = newPoint;
			}
			if (!bCutOff) {
				return result;
			}
		}

		return result;
	}
	
	private Point alphaBetaPruning(Node node, int depth){
		Node maxValNode = maxValueNode(node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

		for(int i = 0; i < maxValNode.children.size(); i++) {
			if(maxValNode.getValue() == maxValNode.children.get(i).getValue()) {
				Node nextMoveNode = maxValNode.children.get(i);
				return nextMoveNode.getNewlyPlacedPoint();
			}
		}

		return maxValNode.getNewlyPlacedPoint();
	}
	
	private Node maxValueNode(Node node, int depth, double alpha, double beta){
		if(quiescence(node.getState())){
			return node;
		} else if (depth == 0) {
			bCutOff = true;
			return node;
		}

		bCutOff = false;
		val = Double.NEGATIVE_INFINITY;
		node.children = generateChildren(node, this.player);
		
		// sort into descending order so highest value first
		node.children = reverseOrder(quicksort(node.children, 0, node.children.size() - 1));

		// Start with the lowest value first for the minValue
		for (int x = 0; !timeIsUp() && x < node.children.size() ; x++) {
			val =  Math.max(val, minValueNode(node.children.get(x), depth - 1, alpha, beta).getValue());
			if(val >= beta) {
				node.setValue(val);
				return node;
			}			
			alpha = Math.max(alpha, val);
		}

		node.setValue(val);
		return node;
	}
	
	private Node minValueNode(Node node,  int depth, double alpha, double beta){
		if(quiescence(node.getState())) {
			return node;
		} else if (depth == 0) {
			bCutOff = true;
			return node;
		}

		bCutOff = false;	
		val = Double.POSITIVE_INFINITY;
		node.children = generateChildren(node, opponent);
		
		// sort into descending order so highest value first
		node.children = quicksort(node.children, 0, node.children.size() - 1);
		
		// Start with highest value for maxValue
		for (int x = 0; !timeIsUp() && x < node.children.size(); x++) {
			val = Math.min(val, maxValueNode(node.children.get(x), depth - 1, alpha, beta).getValue());
			if(val <= alpha) {
				node.setValue(val);
				return node;
			}
			beta = Math.min(beta, val);
		}

		node.setValue(val);
		return node;
	}
	
	private List<Node> generateChildren(Node node, byte plyr){
		BoardModel st;
		Point nPP;
		Node m;

		for(int i=0; i < node.getState().getWidth(); ++i) {
			for(int j=0; j < node.getState().getHeight(); ++j) {
				if(node.getState().getSpace(i, j) != this.player && node.getState().getSpace(i, j) != opponent) {
					st = node.getState();
					nPP = new Point(i, j);
					st = st.placePiece(nPP, plyr);
					m = new Node(st);
					m.setNewlyPlacedPoint(nPP);
					m.setValue(eval(st));
					node.children.add(m);
				}
			}
		}

		return node.children;
	}
	
	private boolean quiescence(BoardModel state){
		BoardModel s = state.clone();
		return s.winner() == opponent;
	}
	
	private boolean timeIsUp(){
		long currentTime =  System.currentTimeMillis();
		return currentTime >= stopTime;
	}

	private List<Node> quicksort(List<Node> nodeChildren, int low, int high) {
		listOfNodes = nodeChildren;

		if (low < high) {
			double pivotValue = nodeChildren.get(high).getValue();

			int i = low;

			for(int j = low; j < high; j++) {
				if(listOfNodes.get(i).getValue() <= pivotValue) {
					listOfNodes = swap(listOfNodes, i, j);
					i++;
				}
			}

			listOfNodes = swap(listOfNodes, i, high);
			int pivotIndex = i;

			listOfNodes = quicksort(listOfNodes, low, pivotIndex-1);
			listOfNodes = quicksort(listOfNodes, pivotIndex+1, high);
		}

		return listOfNodes;
	}

	private List<Node> swap(List<Node> nodeList, int i, int j) {
		Node temp = nodeList.get(i);
		nodeList.set(i, nodeList.get(j));
		nodeList.set(j, temp);
		return nodeList;
	}

	private List<Node> reverseOrder(List<Node> nodeChildren) {
		int low = 0, high  = nodeChildren.size() - 1;
		Node temp;
		while(low < high) {
			swap(nodeChildren, low, high);
			++low;
			--high;
		}

		return nodeChildren;
	}
	
}