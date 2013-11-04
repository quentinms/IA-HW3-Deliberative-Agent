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

	/**
	 * Breadth-first search algorithm
	 * @param vehicle the considered vehicle
	 * @param available the available tasks of the world
	 * @param carried the tasks picked up and currently carried by the vehicle
	 * @return a plan (list of actions)
	 */
	public static Plan computeBFS(Vehicle vehicle, TaskSet available, TaskSet carried) {
		
		Plan plan = null;

		ArrayList<Task> availableTasks = new ArrayList<Task>(available);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);

		ArrayDeque<State> Q = new ArrayDeque<State>(); // States we will have to visit
		ArrayList<State> C = new ArrayList<State>(); // States we have already visited, prevent cycles

		City initialCity = vehicle.getCurrentCity();
		State initialState = new State(initialCity, availableTasks, carriedTasks, new ArrayList<Action>(), vehicle, 0, 0);

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

				else if (!C.contains(visitingState)) {
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

	
	/**
	 * A* algorithm
	 * @param vehicle the considered vehicle
	 * @param available the available tasks of the world
	 * @param carried the tasks picked up and currently carried by the vehicle
	 * @return a plan (list of actions)
	 */
	public static Plan computeAStar(Vehicle vehicle, TaskSet available, TaskSet carried) {
		Plan plan = null;

		ArrayList<Task> availableTasks = new ArrayList<Task>(available);
		ArrayList<Task> carriedTasks = new ArrayList<Task>(carried);

		PriorityQueue<State> Q = new PriorityQueue<State>(1, new StateComparator()); // States we will have to visit
		ArrayList<State> C = new ArrayList<State>(); // States we have already visited, prevent cycles

		City initialCity = vehicle.getCurrentCity();
		State initialState = new State(initialCity, availableTasks, carriedTasks, new ArrayList<Action>(), vehicle, 0, 0);

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

				else if (!C.contains(visitingState)
					  || (C.contains(visitingState) && C.get(C.indexOf(visitingState)).heuristicValue > visitingState.heuristicValue)) {
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
	
	Vehicle vehicle;
	City currentCity;
	ArrayList<Task> availableTasks;
	ArrayList<Task> carriedTasks;
	ArrayList<Action> actionList;

	double totalCost;
	double heuristicValue;
	int weightCarried;

	public State(City currentCity, ArrayList<Task> availableTasks,
			ArrayList<Task> pickedUpTasks, ArrayList<Action> actionList,
			Vehicle vehicle, double totalCost, int weightCarried) {

		this.currentCity = currentCity;
		this.availableTasks = availableTasks;
		this.carriedTasks = pickedUpTasks;
		this.actionList = actionList;
		
		this.vehicle = vehicle;

		this.totalCost = totalCost;
		this.weightCarried = weightCarried;
		
		
		double futureCost = 0;
		for (Task task : availableTasks) {
			double taskCost = (currentCity.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity)) * vehicle.costPerKm();
			if (taskCost > futureCost) {
				futureCost = taskCost;
			}
		}
		for (Task task : carriedTasks) {
			double taskCost = currentCity.distanceTo(task.deliveryCity) * vehicle.costPerKm();
			if (taskCost > futureCost) {
				futureCost = taskCost;
			}
		}
		
		
		heuristicValue = totalCost + futureCost;

	}

	@Override
	public boolean equals(Object obj) {
		State state = (State) obj;
		return currentCity.equals(state.currentCity)
			&& availableTasks.equals(state.availableTasks)
			&& carriedTasks.equals(state.carriedTasks);
	}

	public boolean isFinal() {
		return availableTasks.isEmpty() && carriedTasks.isEmpty();
	}
	
	public ArrayList<State> next() {
		
		ArrayList<State> nextStates = new ArrayList<State>();

		/*
		 * The only interesting next stops are the ones where we either
		 * pick up or deliver tasks.
		 */
		
		ArrayList<City> interestingCities = new ArrayList<City>();
		
		for (Task carriedtask : carriedTasks) {
			if(!interestingCities.contains(carriedtask.deliveryCity)) {
				interestingCities.add(carriedtask.deliveryCity);
			}
		}
		
		for (Task availabletask : availableTasks) {
			if(!interestingCities.contains(availabletask.pickupCity)) {
				interestingCities.add(availabletask.pickupCity);
			}
		}
		
		
		for (City nextCity : interestingCities) {
			
			City newCurrentCity = nextCity;

			ArrayList<Task> newAvailableTasks = new ArrayList<Task>(availableTasks);
			ArrayList<Task> newCarriedTasks = new ArrayList<Task>(carriedTasks);
			ArrayList<Action> newActionList = new ArrayList<Action>(actionList);

			for (City city : currentCity.pathTo(nextCity)) {
				newActionList.add(new Move(city));
			}
			
			
			double newTotalCost = totalCost + currentCity.distanceTo(newCurrentCity) * vehicle.costPerKm();
			int newWeightCarried = weightCarried;
			
			ArrayList<Task> deliveredTasks = new ArrayList<Task>();
			for (Task task : newCarriedTasks) {
				if (task.deliveryCity.equals(nextCity)) {
					newActionList.add(new Delivery(task));
					newWeightCarried = newWeightCarried - task.weight;
					deliveredTasks.add(task);
				}
			}
			
			newCarriedTasks.removeAll(deliveredTasks);
			
			ArrayList<Task> pickedUpTasks = new ArrayList<Task>();
			for (Task task : newAvailableTasks) {
				if (task.pickupCity.equals(nextCity) && vehicle.capacity() >= newWeightCarried + task.weight) {
					newActionList.add(new Pickup(task));
					newWeightCarried = newWeightCarried + task.weight;
					pickedUpTasks.add(task);
				}
			}
			
			newAvailableTasks.removeAll(pickedUpTasks);
			newCarriedTasks.addAll(pickedUpTasks);

			
			State state = new State(newCurrentCity, newAvailableTasks,
					newCarriedTasks, newActionList, this.vehicle,
					newTotalCost, newWeightCarried);
			
			nextStates.add(state);
			
		}

		return nextStates;
		
	}
}

class StateComparator implements Comparator<State> {
	
	@Override
	public int compare(State s1, State s2) {
		return s1.heuristicValue > s2.heuristicValue ? 1 : -1;
	}

}
