package littlehorse

type LHTaskException struct {
	Name    string
	Message string
	Content interface{}
}

func (e *LHTaskException) Error() string {
	return e.Message
}
