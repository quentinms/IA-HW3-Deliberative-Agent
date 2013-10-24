import java.util.ArrayList;

import logist.plan.Action;
import logist.task.Task;
import logist.topology.Topology.City;

public class Model {

}

class State {
	City currentCity;
	ArrayList<Task> availableTasks;
	ArrayList<Task> pickedUpTasks;

	ArrayList<Action> actionList;

	int totalCost;
	int weightCarried;

	public State(City currentCity, ArrayList<Task> availableTasks) {
		this.currentCity = currentCity;
		this.availableTasks = new ArrayList<Task>(availableTasks);
		this.pickedUpTasks = new ArrayList<Task>();

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
}
