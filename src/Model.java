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
		State initialState = new State(initialCity, availableTasks, carriedTasks);

		Q.add(initialState);

		boolean foundFinalState = false;
		State finalState = null;

		while (!foundFinalState) {
			// System.out.println(Q.size());
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

					// TODO not sure if it adds it to the end of the queue.
					// check offer()
					Q.addAll(visitingState.next());
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
		State initialState = new State(initialCity, availableTasks, carriedTasks);

		Q.add(initialState);

		boolean foundFinalState = false;
		State finalState = null;

		while (!foundFinalState) {
			// System.out.println(Q.size());
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

					// TODO check that the order after insertion is correct
					Q.addAll(visitingState.next());
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
	// Plan plan;

	int totalCost;
	int weightCarried;

	public State(City currentCity, ArrayList<Task> availableTasks, ArrayList<Task> carriedTasks) {
		this.currentCity = currentCity;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		this.carriedTasks = new ArrayList<Task>(carriedTasks);
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
		this.carriedTasks = new ArrayList<Task>(pickedUpTasks);
		// this.actionList = actionList;
		this.actionList = new ArrayList<Action>(actionList);

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
		// TODO take weight into account

		/* We go pick up a task */
		for (Task task : availableTasks) {

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

			// TODO needs topology + vehicle (Probably better to do it in BFS/A*
			// and use a setter) for cost
			int newTotalCost = totalCost + 0;
			int newWeightCarried = weightCarried + task.weight;

			State state = new State(newCurrentCity, newAvailableTasks,
					newCarriedTasks, newActionList, newTotalCost,
					newWeightCarried);
			nextStates.add(state);
		}

		/* We go deliver a task */
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

			// TODO needs topology + vehicle for cost
			int newTotalCost = totalCost + 0;
			int newWeightCarried = weightCarried - task.weight;

			State state = new State(newCurrentCity, newAvailableTasks,
					newCarriedTasks, newActionList, newTotalCost,
					newWeightCarried);
			nextStates.add(state);

		}

		return nextStates;
	}
}

// TODO Implement heuristics
class StateComparator implements Comparator<State> {

	@Override
	public int compare(State o1, State o2) {

		// TODO Check if > means 1 or -1 (Check Sokoban if doc not clear)
		if (o1.totalCost > o2.totalCost)
			return 1;
		else
			return -1;
	}

}
