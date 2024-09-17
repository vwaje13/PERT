/* Starter code for PERT algorithm (Project 4)
 * @author rbk
 */

// change to your netid
package vsw230001;

// replace sxa173731 with your netid below
import vsw230001.Graph;
import vsw230001.Graph.Vertex;
import vsw230001.Graph.Edge;
import vsw230001.Graph.GraphAlgorithm;
import vsw230001.Graph.Factory;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
    int CPL;

    public static class PERTVertex implements Factory {
		// Add fields to represent attributes of vertices here
		int es; //earlist start
		int ef; //earliest finish
		int ls; //latest start
		int lf; //latest finish
		int dur;
		int slack;
		int dagStatus; //1 = new; 2 = active; 3 = finished;
		int dfsStart;
		int dfsFinish;
		boolean dfsMark;
		public PERTVertex(Vertex u) {
			ef = 0;
			es = 0;
			lf = 0;
			ls = 0;
			dur = 0;
			slack = 0;
			dagStatus = 0;
			dfsStart = 0;
			dfsFinish = 0;
			dfsMark = false;
		}
		public PERTVertex make(Vertex u) {
			return new PERTVertex(u);
		}

    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
		super(g, new PERTVertex(null));
		finishList = new LinkedList<>();
    }

    public void setDuration(Vertex u, int d) {
		get(u).dur = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG. RETURN FALSE!!!
    public boolean pert() {
    	if(!isDAGAll()) {
    		return false;
		}
		LinkedList<Vertex> top = new LinkedList<>();
		topologicalOrder();
    	top.addAll(finishList);
		for (Vertex u : g) {
			get(u).es = 0;
		}
		for(Vertex u : top) { // L.I: u.es is found
			get(u).ef = get(u).es + get(u).dur;
			for (Edge e : g.outEdges(u)) {
				if(get(e.otherEnd(u)).es < get(u).ef) {
					get(e.otherEnd(u)).es = get(u).ef;
				}
			}
		}
		// max of all the nodes early finish is CPL (project finish time) CPL = max{u.ef}
		CPL = 0;
		for (Vertex u : g) {
			if(get(u).ef > CPL) {
				CPL = get(u).ef;
			}
		}
		for (Vertex u : g) {
			get(u).lf = CPL;
		}
		Collections.reverse(top);
		for(Vertex u : top) {
			get(u).ls = get(u).lf - get(u).dur;
			get(u).slack = get(u).lf - get(u).ef;
			for(Edge e : g.inEdges(u)) {
				Vertex v = e.otherEnd(u);
				if(get(v).lf > get(u).ls) {
					get(v).lf = get(u).ls;
				}
			}
		}
		return true;
    }

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
    	dfsAll();
    	return finishList;
    }

    public void dfsAll() {
		for (Vertex v : g) {
			get(v).dfsMark = false;
		}
		for(Vertex v : g) {
			if(get(v).dfsMark == false) {
				dfs(v);
			}
		}
	}

	private void dfs(Vertex v) {
		get(v).dfsMark = true;
		for(Edge e: g.outEdges(v)) {
			if(get(e.otherEnd(v)).dfsMark == false) {
				dfs(e.otherEnd(v));
			}
		}
		if(finishList.size() < g.size()) {
			finishList.addFirst(v);
		}
	}


    public boolean isDAGAll() {
    	for (Vertex v : g) {
    		get(v).dagStatus = 1;
		}
    	for (Vertex v: g) {
    		if(get(v).dagStatus == 1) {
    			if(!isDAG(v)) {
    				return false;
				}
			}
		}
    	return true;
	}

	private boolean isDAG(Vertex v) {
    	get(v).dagStatus = 2;
    	for(Edge e : g.incident(v)) {
    		if(get(e.otherEnd(v)).dagStatus == 2) {
    			return false;
			}
    		else if(get(e.otherEnd(v)).dagStatus == 1) {
    			if(!isDAG(e.otherEnd(v))) {
    				return false;
				}
			}
		}
    	get(v).dagStatus = 3;
    	return true;
	}

    // The following methods are called after calling pert().

	public int dur(Vertex u) {
    	return get(u).dur;
	}
    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
		return get(u).ef;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
		return get(u).lf;
    }

    // Slack of u
    public int slack(Vertex u) {
		return get(u).slack;
	}

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
		return CPL;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
		return slack(u) == 0;
    }

    // Number of critical vertices of g
    public int numCritical() {
		int num = 0;
    	for(Vertex u : g) {
			if(slack(u) == 0) {
				num++;
			}
		}
    	return num;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
		PERT p = new PERT(g);
		for(Vertex u: g) {
	    	p.setDuration(u, duration[u.getIndex()]);
		}
		// Run PERT algorithm.  Returns false if g is not a DAG
		if(p.pert()) {
	    	return p;
		} else {
	    	return null;
		}
    }
    
    public static void main(String[] args) throws Exception {
		String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
		Scanner in;
		// If there is a command line argument, use it as file from which
		// input is read, otherwise use input from string.
		in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
		Graph g = Graph.readDirectedGraph(in);
		g.printGraph(false);

		int[] duration = new int[g.size()];
		for(int i=0; i<g.size(); i++) {
	    	duration[i] = in.nextInt();
		}

		PERT p = pert(g, duration);

		if(p == null) {
		    System.out.println("Invalid graph: not a DAG");
		} else {
		    System.out.println("Number of critical vertices: " + p.numCritical());
		    System.out.println("u\tEC\tLC\tSlack\tCritical");
		    for(Vertex u: g) {
				System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t\t" + p.critical(u) + "\t" + p.dur(u) );
		    }
		}
    }
}

//DAGALL IS WORKING
