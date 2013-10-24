import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/* import table */

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeAgent implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			System.out.println("A*");
			plan = bfsPlan(vehicle, tasks);
			System.out.println("The plan's cost is:" + plan.totalDistance()*vehicle.costPerKm());
			break;
		case BFS:
			// ...
			System.out.println("BFS");
			plan = bfsPlan(vehicle, tasks);
			System.out.println("The plan's cost is:" + plan.totalDistance()*vehicle.costPerKm());
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	
	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		return Model.computeBFS(vehicle, tasks, TaskSet.create(new Task[0]));
	}
	
	private Plan aStarPlan(Vehicle vehicle, TaskSet tasks) {
		return Model.computeAStar(vehicle, tasks, TaskSet.create(new Task[0]));
	}

	//TODO what happens if plan is cancelled
	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
