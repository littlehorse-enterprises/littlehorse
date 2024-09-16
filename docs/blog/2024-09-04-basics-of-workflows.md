# Basics Workflow concepts

## What is a workflow engine?
A workflow engine, in the simplest way is a way to run a series of steps, 
wait for input or output,
how to handle problems, exceptions or retries
and keep state of where in the steps the flow is.
### Concepts
#### Workflow specifications
A workflow specification is the configuration, or metadata object, that tells the engine what Tasks to run,
what order to run the tasks,
**how to handle exceptions or failures,**
what variables are to be passed from task to task,
and what inputs and outputs are required to run the workflow.

In LittleHorse the Workflow Spec is submitted to and held by the LittleHorse server. 
It is a written in Code, using the LittleHorse SDK.  
In the background LittleHorse server takes the submitted spec from the SDK, and compiles a protobuf object that is submitted to the LittleHorse server.
<!-- TODO: simpler example -->
Example:
```declarative
public class QuickstartWorkflow {

    public static final String WF_NAME = "quickstart";
    public static final String GREET = "greet";

    /*
     * This method defines the logic of our workflow
     */
    public void quickstartWf(WorkflowThread wf) {
        // Create an input variable, make it searchable
        WfRunVariable name = wf.addVariable("input-name", VariableType.STR).searchable();

        // Execute a task and pass in the variable.
        wf.execute(GREET, name);
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WF_NAME, this::quickstartWf);
    }
}
```
#### Tasks
Tasks are the unit of work for a work flow engine.  
It's best to think in examples:
* Change lower case letters to upper case letters.
* Call an API with an input variable and pass along the output.
* Wait for user input or an event to happen.

#### Task workers
Task workers are the places where tasks are executed.  
There can be one or many more task workers per task, this allows for scalability and reliability when executing tasks.

### Advantages of Workflow engines
#### Failure handling
When automating a business process what to do when something doesn't happen, happens with an unexpected outcome, or plain fails is a core challenge.  
More often than not this is hard to reason about, leading to uncaught exceptions that lead to questioning completeness of a workflow or plain incorrect data.  
A workflow engine standardizes how to throw an exception, where the exception is logged, and the logic around when/how to retry.
#### Microservices
Colt McNealy talks about how to wrangle Microservices with a workflow engine, https://littlehorse.dev/blog/microservices-and-workflow.  
The inherent properties of a workflow engine enable developers, and operators, to regain control of their microservices deployments.

#### API gateway <!-- improve this....it feels bad -->
If we look at the properties of an API gateway and how they are used, a workflow engine makes sense.  
<!--TODO: insert example API gateway architecture -->
The usage of an API gateway is to have a single layer that abstracts further endpoints.  
In practice this most often means calling the same API gateway multiple times, receiving the requested data, and doing some date manipulation or calculations at the application layer.
A workflow engine performs all of the most common actions, and includes things like centralized security, possible data obscurity, failure handling, observability and allows for operators to scale compute.
All while still maintaining a central plane that can be shared across an entire orginization.  
Additionally a workflow engine still allows for the standard CRUD(Create, Read, Update, Delete) operations that an API gateway provides.

#### User tasks
A user task allows a developer to inject a human into the process.  Using UserTasks you can assign a human to do something.
Often times this means having a human approve a process, or reject a request.  
#### Integrations

### Common use cases
#### Process Orchestration or Microservices 
#### Data manipulation

