package common

import (
	"errors"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"golang.org/x/net/context"
)

type LHClient struct {
	config   *LHConfig
	grpcStub model.LHPublicApiClient
}

func NewLHClient(config *LHConfig) (*LHClient, error) {
	stub, err := config.GetGrpcClient()
	if err != nil {
		return nil, err
	}

	return &LHClient{
		config:   config,
		grpcStub: *stub,
	}, nil
}

func (l *LHClient) GetExternalEventDef(name string) (*model.ExternalEventDefPb, error) {
	reply, err := l.grpcStub.GetExternalEventDef(
		context.Background(),
		&model.ExternalEventDefIdPb{
			Name: name,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetTaskDef(name string) (*model.TaskDefPb, error) {
	reply, err := l.grpcStub.GetTaskDef(
		context.Background(),
		&model.TaskDefIdPb{
			Name: name,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetWfSpec(name string, version *int32) (*model.WfSpec, error) {
	var reply *model.GetWfSpecReplyPb
	var err error
	if version == nil {
		reply, err = l.grpcStub.GetLatestWfSpec(
			context.Background(),
			&model.GetLatestWfSpecPb{
				Name: name,
			},
		)
	} else {
		reply, err = l.grpcStub.GetWfSpec(
			context.Background(),
			&model.WfSpecIdPb{
				Name:    name,
				Version: *version,
			},
		)
	}

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetNodeRun(wfRunId string, threadRunNumber, position int32) (*model.NodeRun, error) {
	reply, err := l.grpcStub.GetNodeRun(
		context.Background(),
		&model.NodeRunIdPb{
			WfRunId:         wfRunId,
			ThreadRunNumber: threadRunNumber,
			Position:        position,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetVariable(
	wfRunId string, threadRunNumber int32, name string,
) (*model.VariablePb, error) {
	reply, err := l.grpcStub.GetVariable(
		context.Background(),
		&model.VariableIdPb{
			WfRunId:         wfRunId,
			ThreadRunNumber: threadRunNumber,
			Name:            name,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetExternalEvent(
	wfRunId, externalEventDefName, guid string,
) (*model.ExternalEventPb, error) {
	reply, err := l.grpcStub.GetExternalEvent(
		context.Background(),
		&model.ExternalEventIdPb{
			WfRunId:              wfRunId,
			ExternalEventDefName: externalEventDefName,
			Guid:                 guid,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

func (l *LHClient) GetWfRun(id string) (*model.WfRun, error) {
	reply, err := l.grpcStub.GetWfRun(
		context.Background(),
		&model.WfRunIdPb{
			Id: id,
		},
	)

	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		return nil, nil
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	}
	// other cases not valid for GET requests

	return nil, nil
}

type WfArg struct {
	Name string
	Arg  interface{}
}

func (l *LHClient) RunWf(
	wfSpecName string, wfSpecVersion *int32, wfRunId *string, args ...WfArg,
) (*string, error) {
	request := &model.RunWfPb{
		Id:            wfRunId,
		WfSpecName:    wfSpecName,
		WfSpecVersion: wfSpecVersion,
		Variables:     make(map[string]*model.VariableValue),
	}

	for _, arg := range args {
		varValArg, err := InterfaceToVarVal(arg.Arg)
		if err != nil {
			return nil, err
		}
		request.Variables[arg.Name] = varValArg
	}

	reply, err := l.grpcStub.RunWf(context.Background(), request)
	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.WfRunId, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the WfSpec wasn't found
		return nil, errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return nil, errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_ALREADY_EXISTS_ERROR:
		return wfRunId, nil
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return nil, errors.New("event was recorded but not yet processed")
	}
	// other cases not valid for Run WF Request
	return nil, nil
}

func (l *LHClient) PutExternalEvent(
	externalEventDefName, wfRunId string,
	content interface{},
	guid *string, threadRunNumber *int32,
) (*model.ExternalEventIdPb, error) {
	contentVal, err := InterfaceToVarVal(content)
	if err != nil {
		return nil, err
	}
	reply, err := l.grpcStub.PutExternalEvent(
		context.Background(),
		&model.PutExternalEventPb{
			ExternalEventDefName: externalEventDefName,
			WfRunId:              wfRunId,
			Guid:                 guid,
			ThreadRunNumber:      threadRunNumber,
			Content:              contentVal,
		},
	)
	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return &model.ExternalEventIdPb{
			WfRunId:              wfRunId,
			ExternalEventDefName: externalEventDefName,
			Guid:                 reply.Result.Guid,
		}, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return nil, errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return nil, errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_ALREADY_EXISTS_ERROR:
		return &model.ExternalEventIdPb{
			WfRunId:              wfRunId,
			ExternalEventDefName: externalEventDefName,
			Guid:                 *guid,
		}, nil
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return nil, errors.New("event was recorded but not yet processed")
	}
	return nil, nil
}

func (l *LHClient) PutExternalEventDef(
	request *model.PutExternalEventDefPb, swallowAlreadyExistsError bool,
) (*model.ExternalEventDefPb, error) {
	reply, err := l.grpcStub.PutExternalEventDef(
		context.Background(),
		request,
	)
	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return nil, errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return nil, errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_ALREADY_EXISTS_ERROR:
		if swallowAlreadyExistsError {
			getEEDReply, err := l.GetExternalEventDef(request.Name)
			if err != nil {
				return nil, nil
			}
			return getEEDReply, nil
		} else {
			return nil, errors.New("ExternalEventDef Already Exists")
		}
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return nil, errors.New("event was recorded but not yet processed")
	}
	return nil, nil
}

func (l *LHClient) PutTaskDef(
	request *model.PutTaskDefPb, swallowAlreadyExistsError bool,
) (*model.TaskDefPb, error) {
	reply, err := l.grpcStub.PutTaskDef(
		context.Background(),
		request,
	)
	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return nil, errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return nil, errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_ALREADY_EXISTS_ERROR:
		if swallowAlreadyExistsError {
			taskDef, err := l.GetTaskDef(request.Name)
			if err != nil {
				return nil, nil
			}
			return taskDef, nil
		} else {
			return nil, errors.New("TaskDef Already Exists")
		}
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return nil, errors.New("event was recorded but not yet processed")
	}
	return nil, nil
}

func (l *LHClient) PutWfSpec(request *model.PutWfSpecPb) (*model.WfSpec, error) {
	reply, err := l.grpcStub.PutWfSpec(
		context.Background(),
		request,
	)
	if err != nil {
		return nil, err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return reply.Result, nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return nil, errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return nil, errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return nil, errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return nil, errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return nil, errors.New("event was recorded but not yet processed")
	}
	return nil, nil
}

func (l *LHClient) StopWfRun(id string, threadRunNumber int32) error {
	reply, err := l.grpcStub.StopWfRun(
		context.Background(),
		&model.StopWfRunPb{
			WfRunId:         id,
			ThreadRunNumber: threadRunNumber,
		},
	)
	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}

func (l *LHClient) ResumeWfRun(id string, threadRunNumber int32) error {
	reply, err := l.grpcStub.ResumeWfRun(
		context.Background(),
		&model.ResumeWfRunPb{
			WfRunId:         id,
			ThreadRunNumber: threadRunNumber,
		},
	)
	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}

func (l *LHClient) DeleteWfRun(id string) error {
	reply, err := l.grpcStub.DeleteWfRun(
		context.Background(),
		&model.DeleteWfRunPb{
			WfRunId: id,
		},
	)

	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}

func (l *LHClient) DeleteWfSpec(name string, version int32) error {
	reply, err := l.grpcStub.DeleteWfSpec(
		context.Background(),
		&model.DeleteWfSpecPb{
			Name:    name,
			Version: version,
		},
	)

	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}

func (l *LHClient) DeleteTaskDef(name string) error {
	reply, err := l.grpcStub.DeleteTaskDef(
		context.Background(),
		&model.DeleteTaskDefPb{
			Name: name,
		},
	)

	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}

func (l *LHClient) DeleteExternalEventDef(name string) error {
	reply, err := l.grpcStub.DeleteExternalEventDef(
		context.Background(),
		&model.DeleteExternalEventDefPb{
			Name: name,
		},
	)

	if err != nil {
		return err
	}

	switch reply.Code {
	case model.LHResponseCodePb_OK:
		return nil
	case model.LHResponseCodePb_CONNECTION_ERROR:
		return errors.New("connection error: " + *reply.Message)
	case model.LHResponseCodePb_NOT_FOUND_ERROR:
		// Means that the ExternalEventDef wasn't found
		return errors.New("not found: " + *reply.Message)
	case model.LHResponseCodePb_BAD_REQUEST_ERROR:
		return errors.New("bad request: " + *reply.Message)
	case model.LHResponseCodePb_VALIDATION_ERROR:
		return errors.New("invalid: " + *reply.Message)
	case model.LHResponseCodePb_REPORTED_BUT_NOT_PROCESSED:
		return errors.New("event was recorded but not yet processed")
	}
	return nil
}
