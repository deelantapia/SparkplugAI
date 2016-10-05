package connectK;//	Deelan Tapia 81875361
//	CS 171 AI project

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SparkplugAI extends CKPlayer{
	public class Node implements Comparable<Node>{
		public BoardModel state;
		public double value;
		public List<Node> children;
		public Point newlyPlacedPoint;
		
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
			return  (compareValue - (int)this.value);
		}
		
				
		public BoardModel getState(){
			return state;
		}
		
		public double getValue(){
			return value;
		}
	}
	
	public class Tree{
		public Node root;
		public Tree(BoardModel state){
			root =  new Node(state);
		}
	}

	private Tree gameTree;
	private Node result;
	private long stopTime;
	private byte opponent;
	private double val;
	
	public SparkplugAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "connectK.SparkplugAI";
		gameTree = null;
		result = null;
		opponent = (byte)((player == 1) ? 2 : 1);
	}

	@Override
	public Point getMove(BoardModel state) {
		return iDS_ABPruning(state);
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		stopTime = System.currentTimeMillis() + deadline;
		return getMove(state);
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
		for(int i = 0; i < state.getWidth(); i++)
			for(int j = 0; j < state.getHeight(); j++)
				if(state.getSpace(i, j) == this.player){
//					++connectLen[0];
					//left edge
					if(i == 0){
						//bottom left corner
						if(j == 0){
							for(int h = 1; h < kLen; h++){
								//up
								if((j+h) < state.getHeight() && state.getSpace(i, j+h) == this.player)
									++connectLen[0];
								else if((j+h) < state.getHeight() && state.getSpace(i, j+h) == opponent)
									connectLen[0] = 0;
								//upper right diagonal
								if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == this.player)
									++connectLen[1];
								else if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == opponent)
									connectLen[1] = 0;
								//right
								if(state.getSpace(i+h, j) ==  this.player)
									++connectLen[2];
								else if(state.getSpace(i+h, j) == opponent)
									connectLen[2] = 0;
							}
							for(int x = 0; x < 3; x++){
								for(int y = 0 ; y < connectLen[x]; y++)
									++kLenArray[y];
							connectLen = new int[8];	
							}
						}
						
						for(int h = 1; h < kLen; h++){
							//up
							if((j+h) < state.getHeight() && state.getSpace(i, j+h) == this.player)
								++connectLen[0];
							else if((j+h) < state.getHeight() && state.getSpace(i, j+h) == opponent)
								connectLen[0] = 0;
							if((i+h) < state.getWidth()){
								//upper right diagonal
								if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == this.player)
									++connectLen[1];
								else if ((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == opponent)
									connectLen[1] = 0;
								//right
								if(state.getSpace(i+h, j) == this.player)
									++connectLen[2];
								else if(state.getSpace(i+h, j) == opponent)
									connectLen[2] = 0;
							}
							if((j-h) >= 0){
								// lower right diagonal
								if((i+h) < state.getWidth() && state.getSpace(i+h, j-h) == this.player)
									++connectLen[3];
								else if((i+h) < state.getWidth() && state.getSpace(i+h, j-h) == opponent)
									connectLen[3] = 0;
								// down
								if(state.getSpace(i, j-h) == this.player)
									++connectLen[4];
								else if(state.getSpace(i, j-h) == opponent)
									connectLen[4] = 0;
							}
							
						}
						for(int x = 0; x < 5; x++)
							for(int y = 0; y < connectLen[x]; y++)
								++kLenArray[y];
						connectLen = new int[8];
					}
					
					//bottom edge
					else if(j == 0){
						for(int h = 1; h < kLen; h++){							
							//up
							if((j+h) < state.getHeight() && state.getSpace(i, j+h) == this.player)
								++connectLen[0];
							else if((j+h) < state.getHeight() && state.getSpace(i, j+h) == opponent)
								connectLen[0] = 0;
							if((i+h) < state.getWidth()){
								//upper right diagonal
								if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == this.player)
									++connectLen[1];
								else if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == opponent)
									connectLen[1] = 0;
								//right
								if(state.getSpace(i+h, j) == this.player)
									++connectLen[2];
								else if(state.getSpace(i+h, j) == opponent)
									connectLen[2] = 0;
							}
							//down
							if((j-h) >= 0 && state.getSpace(i, j-h) == this.player)
								++connectLen[4];
							else if((j-h) >= 0 && state.getSpace(i, j-h) == opponent)
								connectLen[4] = 0;
							if((i-h) >= 0){
								//left
								if(state.getSpace(i-h, j) == this.player)
									++connectLen[6];
								else if(state.getSpace(i-h, j) == opponent)
									connectLen[6] = 0;
								//upper left diagonal
								if((j+h) < state.getHeight() && state.getSpace(i-h, j+h) == this.player)
									++connectLen[7];
								else if((j+h) < state.getHeight() && state.getSpace(i-h, j+h) == this.player)
									connectLen[7] = 0;
							}
						}
						
						for(int x = 0; x < 8; x++)
							for(int y = 0; y < connectLen[x]; y++)
								if(x != 3 || x != 5)
									++kLenArray[y];
								else
									continue;
						connectLen = new int[8];
						
					}else{
						for(int h = 1; h < kLen; h++){
							//up
							if((j+h) < state.getHeight() && state.getSpace(i, j+h) == this.player)
								++connectLen[0];
							else if((j+h) < state.getHeight() && state.getSpace(i, j+h) == opponent)
								connectLen[0] = 0;
							if((i+h) < state.getWidth()){
								//upper right diagonal
								if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == this.player)
									++connectLen[1];
								else if((j+h) < state.getHeight() && state.getSpace(i+h, j+h) == opponent)
									connectLen[1] = 0;
								//right
								if(state.getSpace(i+h, j) == this.player)
									++connectLen[2];
								else if(state.getSpace(i+h, j) == opponent)
									connectLen[2] = 0;
								//lower right diagonal
								if((j-h) >= 0 && state.getSpace(i+h, j-h) == this.player)
									++connectLen[3];
								else if((j-h) >= 0 && state.getSpace(i+h, j-h) == opponent)
									connectLen[3] = 0;
							}							
							//down
							if((j-h) >= 0 && state.getSpace(i, j-h) == this.player)
								++connectLen[4];
							else if((j-h) >= 0 && state.getSpace(i, j-h) == opponent)
								connectLen[4] = 0;
							if((i-h) >= 0){
								//lower left diagonal
								if((j-h) >= 0 && state.getSpace(i-h, j-h) == this.player)
									++connectLen[5];
								else if((j-h) >= 0 && state.getSpace(i-h, j-h) == opponent)
									connectLen[5] = 0;
								//left
								if(state.getSpace(i-h, j) == this.player)
									++connectLen[6];
								else if(state.getSpace(i-h, j) == opponent)
									connectLen[6] = 0;
								//upper left diagonal
								if((j+h) < state.getHeight() && state.getSpace(i-h, j+h) == this.player)
									++connectLen[7];
								else if((j+h) < state.getHeight() && state.getSpace(i-h, j+h) == opponent)
									connectLen[7] = 0;
							}
						}
						for(int x = 0; x < 8; x++)
							for(int y = 0; y < connectLen[x]; y++){
								++kLenArray[y];
							}
						connectLen = new int[8];
					}					
				}
		for(int m = 0; m < kLen; m++){
			double lengthValue = 1/(kLen - m);
			value += lengthValue * kLenArray[m];
		}
		return value;
	}
	
	private Point iDS_ABPruning(BoardModel state){
		gameTree = new Tree(state);
		for(int d = 0; !timeIsUp() && d < Integer.MAX_VALUE; d++)
			alphaBetaPruning(gameTree.root, d);
	
		return result.newlyPlacedPoint;
	}
	
	private void alphaBetaPruning(Node node, int depth){
		double v = maxValue(node, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		for(int x = 0; x < node.children.size(); x++){
			if(v == node.children.get(x).value){
				result = node.children.get(x);
			}
		}	
	}
	
	private double maxValue(Node node, int depth, double alpha, double beta){		
		if(depth == 0 || quiescence(node.state)){
			return eval(node.state);
		}
		val = Double.NEGATIVE_INFINITY;
		generateChildren(node, this.player);		
		
		// sort into descending order so highest value first
		Collections.sort(node.children);
		
		while(!timeIsUp() && node.children.size() > 0){
			val =  Math.max(val, minValue(node.children.get(0), --depth, alpha, beta));
			if(val >= beta){
				if(!node.equals(gameTree.root))
					node.children = null;
				return val;
			}
			if(alpha < val)
				result = node.children.get(0);
			
			alpha = Math.max(alpha, val);
			node.children.remove(0);
		}
		if(!node.equals(gameTree.root))
			node.children = null;
		return val;
	}
	
	private double minValue(Node node,  int depth, double alpha, double beta){
		if(depth == 0 || quiescence(node.state))
			return eval(node.state);
		val = Double.POSITIVE_INFINITY;
		generateChildren(node, opponent);
		
		// sort into descending order so highest value first
		Collections.sort(node.children);
		
		while(!timeIsUp() && node.children.size() > 0){	
			val = Math.min(val, maxValue(node.children.get(node.children.size() - 1), --depth, alpha, beta));
			if(val <= alpha){
				node.children = null;
				return val;
			}
				
			beta = Math.min(beta, val);
			node.children.remove(node.children.size() - 1);
		}
		node.children = null;
		return val;		
	}
	
	private void generateChildren(Node node, byte plyr){
		for(int i=0; i < node.state.getWidth(); ++i)
			for(int j=0; j < node.state.getHeight(); ++j)
				if(node.state.getSpace(i, j) == 0){
					BoardModel st = node.state;
					Point nPP = new Point(i, j);
					st = st.placePiece(nPP, plyr);
					Node m = new Node(st);
					m.newlyPlacedPoint = nPP;
					m.value = eval(st);
					node.children.add(m);
				}
	}
	
	private boolean quiescence(BoardModel state){
		BoardModel s = state.clone();
		return s.winner() == opponent;
	}
	
	private boolean timeIsUp(){
		return System.currentTimeMillis() >= stopTime;
	}
	
}