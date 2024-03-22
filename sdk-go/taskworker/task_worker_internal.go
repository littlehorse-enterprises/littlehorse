package taskworker

import (
	"context"
	"log"
	"reflect"
	"strconv"
	"sync"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"google.golang.org/protobuf/types/known/timestamppb"
)

const TOTAL_RETRIES = 5

func (tw *LHTaskWorker) registerTaskDef() error {
	ptd := &model.PutTaskDefRequest{
		Name:      tw.taskDefId.Name,
		InputVars: make([]*model.VariableDef, 0),
	}

	for i, arg := range tw.taskSig.Args {
		ptd.InputVars = append(ptd.InputVars, &model.VariableDef{
			Name: strconv.Itoa(i) + "-" + arg.Name,
			Type: common.ReflectTypeToVarType(arg.Type),
		})
	}

	_, err := (*tw.grpcStub).PutTaskDef(context.Background(), ptd)

	return err
}

func (tw *LHTaskWorker) start() error {
	tw.manager.start()
	return nil
}

func (tw *LHTaskWorker) close() error {
	return tw.manager.close()
}

// ///////////////////////////////////////////////////////////
// Below is equivalent to LHServerConnection.java
// ///////////////////////////////////////////////////////////
type serverConnection struct {
	manager        *serverConnectionManager
	host           *model.LHHostInfo
	running        bool
	pollTaskClient *model.LittleHorse_PollTaskClient
	grpcClient     *model.LittleHorseClient
}

func newServerConnection(
	manager *serverConnectionManager, host *model.LHHostInfo,
) (*serverConnection, error) {
	grpcClient, err := manager.tw.config.GetGrpcClientForHost(
		host.Host + ":" + strconv.Itoa(int(host.Port)),
	)
	if err != nil {
		return nil, err
	}

	stream, err := (*grpcClient).PollTask(context.Background())
	if err != nil {
		return nil, err
	}

	out := &serverConnection{
		host:           host,
		manager:        manager,
		running:        true,
		pollTaskClient: &stream,
		grpcClient:     grpcClient,
	}

	stream.Send(&model.PollTaskRequest{
		ClientId:          manager.tw.config.TaskWorkerId,
		TaskDefId:         manager.tw.taskDefId,
		TaskWorkerVersion: &manager.tw.config.TaskWorkerVersion,
	})

	// Receive work and schedule tasks
	go func() {
		for {
			pollTaskReply, err := stream.Recv()
			if err != nil {
				out.running = false
				manager.onConnectionClosed(out)
				return
			}

			if pollTaskReply != nil && pollTaskReply.Result != nil {
				task := pollTaskReply.Result
				log.Default().Print(
					"Received a task for " + common.GetWfRunIdFromTaskSource(task.Source).GetId(),
				)
				manager.submitTaskForExecution(task, out.grpcClient)
			} else {
				log.Default().Print("Didn't get task")
			}

			if out.running {
				req := model.PollTaskRequest{
					ClientId:          manager.tw.config.TaskWorkerId,
					TaskDefId:         manager.tw.taskDefId,
					TaskWorkerVersion: &manager.tw.config.TaskWorkerVersion,
				}
				log.Default().Print("Asking for another task to run")
				stream.Send(&req)
			} else {
				return
			}
		}
	}()

	go func() {
		ctx := stream.Context()
		<-ctx.Done()
		out.running = false
		if err := ctx.Err(); err != nil {
			log.Println(err)
		}
		manager.onConnectionClosed(out)
	}()

	return out, nil
}

func (c *serverConnection) close() {
	c.running = false
}

/////////////////////////////////////////////////////////////
// Below is equivalent to LHServerConnectionManager.java
/////////////////////////////////////////////////////////////

type serverConnectionManager struct {
	tw             *LHTaskWorker
	connections    []*serverConnection
	taskChannel    chan *taskExecutionInfo
	wg             *sync.WaitGroup
	running        bool
	clusterHealthy bool
	workerHealthy  bool
}

func newServerConnectionManager(tw *LHTaskWorker) *serverConnectionManager {
	channel := make(chan *taskExecutionInfo, 1)
	var wg sync.WaitGroup
	return &serverConnectionManager{
		tw:             tw,
		connections:    make([]*serverConnection, 0),
		taskChannel:    channel,
		wg:             &wg,
		running:        false,
		clusterHealthy: true,
		workerHealthy:  true,
	}
}

func (controller *serverConnectionManager) notifyCallFailure() {
	controller.workerHealthy = false
}

func (controller *serverConnectionManager) notifyCallSuccess(response *model.RegisterTaskWorkerResponse) {
	if response.IsClusterHealthy != nil {
		controller.clusterHealthy = *response.IsClusterHealthy
	} else {
		controller.clusterHealthy = true
	}

	controller.workerHealthy = true
}

func (m *serverConnectionManager) start() {
	m.running = true

	// start worker threads
	log.Default().Print("Worker threads: ", m.tw.config.NumWorkerThreads)
	for i := 0; i < int(m.tw.config.NumWorkerThreads); i++ {
		m.wg.Add(1)
		go func() {
			log.Default().Print("Starting worker thread")
			for taskToExec := range m.taskChannel {
				m.doTask(taskToExec)
			}
			log.Default().Print("Releasing waitgroup")
			m.wg.Done()
		}()
	}

	// This is the rebalance/heartbeat thread
	for m.running {
		reply, err := (*m.tw.grpcStub).RegisterTaskWorker(
			context.Background(),
			&model.RegisterTaskWorkerRequest{
				TaskDefId:    m.tw.taskDefId,
				TaskWorkerId: m.tw.config.TaskWorkerId,
				ListenerName: m.tw.config.ServerConnectListener,
			},
		)
		if err != nil {
			m.notifyCallFailure()
			time.Sleep(time.Duration(time.Second * 8))
			continue
		}
		m.notifyCallSuccess(reply)
		for _, host := range reply.YourHosts {
			if !m.isAlreadyRunning(host) {
				newConn, err := newServerConnection(m, host)
				if err != nil {
					log.Println("Failed adding a new host: " + err.Error())
				} else {
					m.connections = append(m.connections, newConn)
					log.Println(
						"Added new connection to " + host.Host + ":" + strconv.Itoa(int(host.Port)),
					)
				}
			}
		}

		for i := 0; i < len(m.connections); i++ {
			conn := m.connections[i]
			if !m.shouldBeRunning(conn, reply.YourHosts) {
				log.Println(
					"Stopping connection for ", conn.host.Host, ":", conn.host.Port,
				)
				conn.close()

				// Remove the element from connections
				copy(m.connections[i:], m.connections[i+1:])
				m.connections = m.connections[:len(m.connections)-1]
				i--
			}
		}

		time.Sleep(time.Duration(time.Second * 8))
	}

}

func (m *serverConnectionManager) isAlreadyRunning(host *model.LHHostInfo) bool {
	for _, connection := range m.connections {
		if connection.host.Host == host.Host && connection.host.Port == host.Port {
			return true
		}
	}
	return false
}

func (m *serverConnectionManager) shouldBeRunning(
	conn *serverConnection, hosts []*model.LHHostInfo,
) bool {
	for _, host := range hosts {
		if conn.host.Host == host.Host && conn.host.Port == host.Port {
			return true
		}
	}
	return false
}

func (m *serverConnectionManager) close() error {
	close(m.taskChannel)
	m.running = false
	m.wg.Wait()
	return nil
}

func (m *serverConnectionManager) onConnectionClosed(conn *serverConnection) {
	// Just remove it from the list
	for i := 0; i < len(m.connections); i++ {
		if m.connections[i] == conn {
			copy(m.connections[i:], m.connections[i+1:])
			m.connections = m.connections[:len(m.connections)-1]
			return
		}
	}
	log.Print("This should be impossible: tried to remove it but didn't find it!")
}

// stores the info related to the task and which stub it should connect to
type taskExecutionInfo struct {
	specificStub *model.LittleHorseClient
	task         *model.ScheduledTask
}

func (m *serverConnectionManager) submitTaskForExecution(task *model.ScheduledTask, specificStub *model.LittleHorseClient) {
	taskToExecution := &taskExecutionInfo{
		specificStub: specificStub,
		task:         task,
	}
	m.taskChannel <- taskToExecution
	log.Default().Print("Put task in channel for " + common.GetWfRunIdFromTaskSource(task.Source).Id)
}

func (m *serverConnectionManager) doTask(taskToExec *taskExecutionInfo) {
	taskResult := m.doTaskHelper(taskToExec.task)
	_, err := (*taskToExec.specificStub).ReportTask(context.Background(), taskResult)
	if err != nil {
		m.retryReportTask(context.Background(), taskResult, TOTAL_RETRIES)
	}
}

func (m *serverConnectionManager) retryReportTask(ctx context.Context, taskResult *model.ReportTaskRun, retries int) {
	log.Println("Retrying reportTask rpc on wfRun {}", taskResult.TaskRunId.WfRunId)

	// TODO: Is this a Really Bad Idea? I forget whether this runs in the main
	// thread or in a goroutine.
	time.Sleep(500 * time.Millisecond)

	_, err := (*m.tw.grpcStub).ReportTask(context.Background(), taskResult)
	if err != nil {
		retriesLeft := (retries - 1)
		if retriesLeft > 0 {
			log.Println("ReportTask failed, enqueuing retry")
			m.retryReportTask(ctx, taskResult, retriesLeft)
		} else {
			log.Println("ReportTask failed, not enqueueing retry")
		}
	}
}

func (m *serverConnectionManager) doTaskHelper(task *model.ScheduledTask) *model.ReportTaskRun {
	var reflectArgs []reflect.Value
	taskResult := &model.ReportTaskRun{
		TaskRunId: task.TaskRunId,
	}

	workerContext := &common.WorkerContext{
		ScheduledTask: task,
		ScheduleTime:  task.GetCreatedAt(),
	}

	for _, taskFuncArg := range m.tw.taskSig.Args {
		goValue, err := taskFuncArg.Assign(task, workerContext)
		if err != nil {
			msg := "Failed calculating input variable " + taskFuncArg.Name + ": " + err.Error()
			taskResult.LogOutput = &model.VariableValue{
				Value: &model.VariableValue_Str{Str: msg},
			}
			taskResult.Status = model.TaskStatus_TASK_INPUT_VAR_SUB_ERROR
			return taskResult
		}
		reflectArgs = append(reflectArgs, *goValue)
	}

	if m.tw.taskSig.GetHasWorkerContextAtEnd() {
		reflectArgs = append(reflectArgs, reflect.ValueOf(workerContext))
	}

	fnPtr := reflect.ValueOf(m.tw.taskFunc)
	invocationResults := fnPtr.Call(reflectArgs)

	if m.tw.taskSig.HasOutput {
		taskOutputReflect := invocationResults[0]
		if taskOutputReflect.Interface() != nil {
			taskOutputVarVal, err := common.InterfaceToVarVal(taskOutputReflect.Interface())

			if err != nil {
				msg := "Failed to serialize task output: " + err.Error()
				if workerContext.GetLogOutput() != "" {
					msg += "\n\n\n\n" + workerContext.GetLogOutput()
				}
				taskResult.LogOutput = &model.VariableValue{
					Value: &model.VariableValue_Str{Str: msg},
				}
				taskResult.Status = model.TaskStatus_TASK_OUTPUT_SERIALIZING_ERROR
				return taskResult
			}
			taskResult.Result = &model.ReportTaskRun_Output{Output: taskOutputVarVal}
			if workerContext.GetLogOutput() != "" {
				msg := workerContext.GetLogOutput()
				taskResult.LogOutput = &model.VariableValue{
					Value: &model.VariableValue_Str{Str: msg},
				}
			}
			taskResult.Status = model.TaskStatus_TASK_SUCCESS
		}
	}

	if m.tw.taskSig.HasError {
		errorReflect := invocationResults[len(invocationResults)-1]

		if errorReflect.Interface() != nil {
			errorVarVal, err := common.InterfaceToVarVal(errorReflect.Interface())
			if err != nil {
				log.Println("WARN: was unable to serialize error")
			} else {
				taskResult.LogOutput = errorVarVal
			}
			taskResult.Status = model.TaskStatus_TASK_FAILED
		}
	}

	taskResult.Time = timestamppb.Now()

	return taskResult
}
