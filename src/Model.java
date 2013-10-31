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

	public static Plan computeBFS(Vehicle vehicle, TaskSet tasks,TaskSet carried) {
		Plan plan = null;

		ArrayList<Task> availableTasks = new ArrayList<Task>(tasks);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);

		ArrayDeque<State> Q = new ArrayDeque<State>(); // States we will have to visit
		ArrayList<State> C = new ArrayList<State>(); // States we have already visited, prevent cycles

		City initialCity = vehicle.getCurrentCity();
		State initialState = new State(initialCity, availableTasks, carriedTasks, vehicle);

		Q.add(initialState);

		boolean foundFinalState = false;
		State finalState = null;

		while (!foundFinalState) {
			
			if (Q.isEmpty()) {
				foundFinalState = true;
				
			} else {
				State visitingState = Q.poll();

				if (visitingState.isFinal()) {
					finalState = visitingState;
					foundFinalState = true;
				}

				if (!C.contains(visitingState)) {
					C.add(visitingState);
					Q.addAll(visitingState.next()); // Hopefully at the end of the list
				}

			}

		}

		if (finalState != null) {
			plan = new Plan(vehicle.getCurrentCity(), finalState.actionList);
		}

		return plan;
	}

	public static Plan computeAStar(Vehicle vehicle, TaskSet tasks, TaskSet carried) {
		Plan plan = null;

		ArrayList<Task> availableTasks = new ArrayList<Task>(tasks);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);

		PriorityQueue<State> Q = new PriorityQueue<State>(1, new StateComparator()); // States we will have to visit

		ArrayList<State> C = new ArrayList<State>(); // States we have already visited, prevent cycles

		City initialCity = vehicle.getCurrentCity();
		State initialState = new State(initialCity, availableTasks, carriedTasks, vehicle);

		Q.add(initialState);

		boolean foundFinalState = false;
		State finalState = null;

		while (!foundFinalState) {
			
			if (Q.isEmpty()) {
				foundFinalState = true;
				
			} else {
				State visitingState = Q.poll();

				if (visitingState.isFinal()) {
					finalState = visitingState;
					foundFinalState = true;
				}

				if (!C.contains(visitingState)) {
					C.add(visitingState);
					Q.addAll(visitingState.next()); // Hopefully at the end of the list
				}

			}

		}

		if (finalState != null) {
			plan = new Plan(vehicle.getCurrentCity(), finalState.actionList);
		}

		return plan;
	}

}

class State {
	
	City currentCity;
	ArrayList<Task> availableTasks;
	ArrayList<Task> carriedTasks;
	ArrayList<Action> actionList;
	Vehicle vehicle;
	// Plan plan;

	double totalCost;
	int weightCarried;

	public State(City currentCity, ArrayList<Task> availableTasks, ArrayList<Task> carriedTasks, Vehicle vehicle) {
		this.currentCity = currentCity;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		this.carriedTasks = new ArrayList<Task>(carriedTasks);
		this.actionList = new ArrayList<Action>();
		this.vehicle = vehicle;

		totalCost = 0.;
		weightCarried = 0;
	}

	public State(City currentCity, ArrayList<Task> availableTasks,
			ArrayList<Task> pickedUpTasks, ArrayList<Action> actionList,
			Vehicle vehicle, double totalCost, int weightCarried) {

		this.currentCity = currentCity;
		// this.availableTasks = availableTasks;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		// this.pickedUpTasks = pickedUpTasks;
		this.carriedTasks = new ArrayList<Task>(pickedUpTasks);
		// this.actionList = actionList;
		this.actionList = new ArrayList<Action>(actionList);
		this.vehicle = vehicle;

		this.totalCost = totalCost;
		this.weightCarried = weightCarried;

	}

	@Override
	public boolean equals(Object obj) {
		State state = (State) obj;
		return currentCity.equals(state.currentCity)
				&& carriedTasks.equals(state.carriedTasks)
				&& availableTasks.equals(state.availableTasks);
	}

	public boolean isFinal() {
		return availableTasks.isEmpty() && carriedTasks.isEmpty();
	}
	
	public ArrayList<State> next() {
		ArrayList<State> nextStates = new ArrayList<State>();

		/*
		 * The only interesting next stops are the ones where we either
		 * pick up a task or deliver one.
		 */

		/* We go pick up a task */
		
		/*
		for (Task task : availableTasks) {

			if (weightCarried + task.weight < this.vehicle.capacity()) {
			
				City newCurrentCity = task.pickupCity;
				
				ArrayList<Task> newAvailableTasks = new ArrayList<Task>(availableTasks);
				newAvailableTasks.remove(task);
	
				ArrayList<Task> newCarriedTasks = new ArrayList<Task>(carriedTasks);
				newCarriedTasks.add(task);
	
				ArrayList<Action> newActionList = new ArrayList<Action>(actionList);
	
				// Move can only go to a neighbouring city, so we add a move for all
				// cities in the path.
				for (City city : currentCity.pathTo(task.pickupCity)) {
					newActionList.add(new Move(city));
				}
	
				newActionList.add(new Pickup(task));
	
				double newTotalCost = totalCost + currentCity.distanceTo(newCurrentCity) * vehicle.costPerKm();
				int newWeightCarried = weightCarried + task.weight;
	
				State state = new State(newCurrentCity, newAvailableTasks,
						newCarriedTasks, newActionList, this.vehicle,
						newTotalCost, newWeightCarried);
				nextStates.add(state);
				
			}
			
		}*/

		/* We go deliver a task */
		/*
		for (Task task : carriedTasks) {

			City newCurrentCity = task.deliveryCity;

			ArrayList<Task> newAvailableTasks = new ArrayList<Task>(availableTasks);
			
			ArrayList<Task> newCarriedTasks = new ArrayList<Task>(carriedTasks);
			newCarriedTasks.remove(task);

			ArrayList<Action> newActionList = new ArrayList<Action>(actionList);

			for (City city : currentCity.pathTo(task.deliveryCity)) {
				newActionList.add(new Move(city));
			}

			newActionList.add(new Delivery(task));

			double newTotalCost = totalCost; // We don't really care about cost for the BFS
			int newWeightCarried = weightCarried - task.weight;

			State state = new State(newCurrentCity, newAvailableTasks,
					newCarriedTasks, newActionList, this.vehicle,
					newTotalCost, newWeightCarried);
			nextStates.add(state);

		}*/
		
		for(City c: currentCity.neighbors()){
			
			City newCurrentCity = c;

			ArrayList<Task> newAvailableTasks = new ArrayList<Task>(availableTasks);
			
			ArrayList<Task> newCarriedTasks = new ArrayList<Task>(carriedTasks);
			
			ArrayList<Action> newActionList = new ArrayList<Action>(actionList);

			newActionList.add(new Move(c));
			double newTotalCost = totalCost + currentCity.distanceTo(newCurrentCity) * vehicle.costPerKm();;
			int newWeightCarried = weightCarried;
			
			ArrayList<Task> delivered = new ArrayList<Task>();
			for(Task t: newCarriedTasks){
				if(t.deliveryCity.equals(c)){
					newActionList.add(new Delivery(t));
					newWeightCarried = newWeightCarried - t.weight;
					delivered.add(t);
				}
			}
			
			newCarriedTasks.removeAll(delivered);
			
			//TODO Weight
			
			ArrayList<Task> pickedUp = new ArrayList<Task>();
			for(Task t: newAvailableTasks){
				if(t.pickupCity.equals(c)){
					newActionList.add(new Pickup(t));
					newWeightCarried = newWeightCarried + t.weight;
					pickedUp.add(t);
				}
			}
			
			newAvailableTasks.removeAll(pickedUp);
			newCarriedTasks.addAll(pickedUp);

	
			State state = new State(newCurrentCity, newAvailableTasks,
					newCarriedTasks, newActionList, this.vehicle,
					newTotalCost, newWeightCarried);
			
			nextStates.add(state);

			
		}

		return nextStates;
	}
}

class StateComparator implements Comparator<State> {
	//TODO Heuristic
	@Override
	public int compare(State s1, State s2) {

		/*if (s1.totalCost > s2.totalCost)
			return 1;
		else
			return -1;
		*/
		
		double futureCostS1 = 0;
		City c1 = s1.currentCity;
		for (Task t: s1.availableTasks){
			
		}
		
		double futureCostS2 = 0;
		City c2 = s2.currentCity;
		for (Task t: s2.availableTasks){
			
		}
		
		
		
		return (s1.totalCost + futureCostS1> s2.totalCost + futureCostS2) ? 1 : -1;
	}

}
