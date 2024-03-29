import java.util.Date;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/* import table */

/**
 * An optimal planner for one vehicle.
 */
public class DeliberativeAgent implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution taskDistribution;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	TaskSet carriedTasks;
	
	@Override
	public void setup(Topology topology, TaskDistribution taskDistribution, Agent agent) {
		this.topology = topology;
		this.taskDistribution = taskDistribution;
		this.agent = agent;
		this.carriedTasks = TaskSet.create(new Task[0]);
		
		// Initialize the planner
		// int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		Date start = new Date();

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		
		case ASTAR:
			System.out.println("Plan: A*");
			plan = aStarPlan(vehicle, tasks, carriedTasks);
			System.out.println(((new Date()).getTime() - start.getTime())/1000.0 + "s");
			System.out.println(vehicle.name()+" total cost is: " + (plan.totalDistance()+vehicle.getDistance()) * vehicle.costPerKm());
			break;
			
		case BFS:
			System.out.println("Plan: BFS");
			plan = bfsPlan(vehicle, tasks, carriedTasks);
			System.out.println(((new Date()).getTime() - start.getTime())/1000.0 + "s");
			System.out.println(vehicle.name()+" total cost is: " + (plan.totalDistance()+vehicle.getDistance()) * vehicle.costPerKm());
			break;
			
		default:
			throw new AssertionError("Should not happen.");
		}
		
		return plan;
	}
	
	
	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks, TaskSet carriedTasks) {
		return Model.computeBFS(vehicle, tasks, carriedTasks);
	}
	
	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks, TaskSet carriedTasks) {
		return Model.computeAStar(vehicle, tasks, carriedTasks);
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			this.carriedTasks = carriedTasks;
		}
	}
}
