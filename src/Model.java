import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class Model {
	
	
	public static Plan computeBFS(Vehicle vehicle, TaskSet tasks, TaskSet carried){
		Plan plan = null;
		
		ArrayList<Task> availableTasks = new ArrayList<Task>(tasks);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);
		
		ArrayDeque<State> Q = new ArrayDeque<State>(); //States we will have to visit
		
		ArrayList<State> C = new ArrayList<State>(); //States we have already visited, prevent cycles
		
		City initialCity = vehicle.getCurrentCity();
		State initialNode = new State(initialCity, availableTasks, carriedTasks);
		
		Q.add(initialNode);
		
		
		boolean foundFinalState = false;
		State finalState = null;
		
		while(!foundFinalState){
			//System.out.println(Q.size());
			if(Q.isEmpty()){
				foundFinalState = true;
			} else {
				State n = Q.poll();
				
				if(n.isFinal()){
					finalState = n;
					foundFinalState = true;
				}
				
				if(!C.contains(n)){
					C.add(n);
					
					//TODO not sure if it adds it to the end of the queue. check offer()
					Q.addAll(n.next());
				}
			
			}
			
		}
		
		if(finalState != null){
			plan = new Plan(vehicle.getCurrentCity(), finalState.actionList);
		}
		
		return plan;
	}
	
	public static Plan computeAStar(Vehicle vehicle, TaskSet tasks, TaskSet carried){
		Plan plan = null;
		
		ArrayList<Task> availableTasks = new ArrayList<Task>(tasks);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);
		
		PriorityQueue<State> Q = new PriorityQueue<State>(1,new StateComparator()); //States we will have to visit
		
		ArrayList<State> C = new ArrayList<State>(); //States we have already visited, prevent cycles
		
		City initialCity = vehicle.getCurrentCity();
		State initialNode = new State(initialCity, availableTasks, carriedTasks);
		
		Q.add(initialNode);
		
		
		boolean foundFinalState = false;
		State finalState = null;
		
		while(!foundFinalState){
			//System.out.println(Q.size());
			if(Q.isEmpty()){
				foundFinalState = true;
			} else {
				State n = Q.poll();
				
				if(n.isFinal()){
					finalState = n;
					foundFinalState = true;
				}
				
				if(!C.contains(n)){
					C.add(n);
					
					//TODO check that the order after insertion is correct
					Q.addAll(n.next());
				}
			
			}
			
		}
		
		if(finalState != null){
			plan = new Plan(vehicle.getCurrentCity(), finalState.actionList);
		}
		
		return plan;
	}

}

class State {
	City currentCity;
	ArrayList<Task> availableTasks;
	ArrayList<Task> pickedUpTasks;

	ArrayList<Action> actionList;
	//Plan plan;

	int totalCost;
	int weightCarried;

	public State(City currentCity, ArrayList<Task> availableTasks, ArrayList<Task> carriedTasks) {
		this.currentCity = currentCity;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		this.pickedUpTasks = new ArrayList<Task>(carriedTasks);
		
		this.actionList = new ArrayList<Action>();

		totalCost = 0;
		weightCarried = 0;
	}

	public State(City currentCity, ArrayList<Task> availableTasks,
			ArrayList<Task> pickedUpTasks, ArrayList<Action> actionList,
			int totalCost, int weightCarried) {

		this.currentCity = currentCity;
		// this.availableTasks = availableTasks;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		// this.pickedUpTasks = pickedUpTasks;
		this.pickedUpTasks = new ArrayList<Task>(pickedUpTasks);
		// this.actionList = actionList;
		this.actionList = new ArrayList<Action>(actionList);

		this.totalCost = totalCost;
		this.weightCarried = weightCarried;

	}
	
	@Override
	public boolean equals(Object obj) {
		State s = (State) obj;
		return currentCity.equals(s.currentCity) && pickedUpTasks.equals(s.pickedUpTasks) 
				&& availableTasks.equals(s.availableTasks);
	}
	
	public boolean isFinal(){
		return availableTasks.isEmpty() && pickedUpTasks.isEmpty();
	}
	
	public ArrayList<State> next(){
		ArrayList<State> nextStates = new ArrayList<State>();
		
		/*The only interesting next stops are the one where we either pick up a task or deliver one.*/
		//TODO take weight into account
		
		/*We go pick up a task*/
		for(Task t: availableTasks){
			
			City currentCity_new = t.pickupCity;
			ArrayList<Task> availableTasks_new = new ArrayList<Task>(availableTasks);
			availableTasks_new.remove(t);
			
			ArrayList<Task> pickedUpTasks_new = new ArrayList<Task>(pickedUpTasks);
			pickedUpTasks_new.add(t);
			
			ArrayList<Action> actionList_new = new ArrayList<Action>(actionList);
			
			
			//Move can only go to a neighbouring city, so we add a move for all cities in the path.
			for (City city : currentCity.pathTo(t.pickupCity)){
				actionList_new.add(new Move(city));
			}
			
			actionList_new.add(new Pickup(t));
			
			
			//TODO needs topology + vehicle (Probably better to do it in BFS/A* and use a setter)
			int totalCost_new = totalCost + 0;
			int weightCarried_new = weightCarried + t.weight;
			
			
			State s = new State(currentCity_new, availableTasks_new, pickedUpTasks_new, actionList_new, totalCost_new, weightCarried_new);
			nextStates.add(s);
		}
		
		
		/*We go deliver a task*/
		for(Task t: pickedUpTasks){
			
			City currentCity_new = t.deliveryCity;
		
			ArrayList<Task> availableTasks_new = new ArrayList<Task>(availableTasks);
			
			ArrayList<Task> pickedUpTasks_new = new ArrayList<Task>(pickedUpTasks);
			pickedUpTasks_new.remove(t);
			
			ArrayList<Action> actionList_new = new ArrayList<Action>(actionList);
	
			for (City city : currentCity.pathTo(t.deliveryCity)){
				actionList_new.add(new Move(city));
			}
			
			actionList_new.add(new Delivery(t));
			
			
			//TODO needs topology + vehicle
			int totalCost_new = totalCost + 0;
			int weightCarried_new = weightCarried - t.weight;
			
			
			State s = new State(currentCity_new, availableTasks_new, pickedUpTasks_new, actionList_new, totalCost_new, weightCarried_new);
			nextStates.add(s);
			
		}
		
		return nextStates;
	}
}


//TODO Implement heuristics
class StateComparator implements Comparator<State>{

	@Override
	public int compare(State o1, State o2) {
		
		//TODO Check if > means 1 or -1 (Check Sokoban if doc not clear)
		if(o1.totalCost > o2.totalCost) return 1;
		else return -1;
	}
	
}
