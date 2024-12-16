package main

import (
	"fmt"
)

// VariableValue is a placeholder for the actual type you need.
type VariableValue struct {
	// Add fields as necessary
}

// LHTaskException defines a custom error type.
type LHTaskException struct {
	Name    string
	Message string
	Content *VariableValue // Pointer to VariableValue, can be nil
}

// Error implements the error interface.
func (e *LHTaskException) Error() string {
	return e.Message // Return only the message for the error interface
}

// NewLHTaskException creates a new LHTaskException with no content.
func NewLHTaskException(name, message string) *LHTaskException {
	return &LHTaskException{
		Name:    name,
		Message: message,
		Content: nil, // No content provided
	}
}

// NewLHTaskExceptionWithContent creates a new LHTaskException with content.
func NewLHTaskExceptionWithContent(name, message string, content *VariableValue) *LHTaskException {
	return &LHTaskException{
		Name:    name,
		Message: message,
		Content: content,
	}
}

// Example usage
func main() {
	// Example of throwing an error
	err := NewLHTaskException("MyErrorName", "Something went wrong")
	handleError(err)

	// Example of throwing an error with content
	content := &VariableValue{} // Assuming you have some content to pass
	errWithContent := NewLHTaskExceptionWithContent("MyErrorNameWithContent", "Something went wrong with content", content)
	handleError(errWithContent)
}

// handleError processes the error and extracts the Name and Message
func handleError(err error) {
	if err != nil {
		// Print the error message
		fmt.Println("Error Message:", err.Error())

		// Try to assert the error to *LHTaskException
		if lhtErr, ok := err.(*LHTaskException); ok {
			fmt.Println("Error Name:", lhtErr.Name)
			fmt.Println("Error Message:", lhtErr.Message)
			// Handle Content if necessary
			if lhtErr.Content != nil {
				fmt.Println("Content is present")
			} else {
				fmt.Println("No content provided")
			}
		} else {
			fmt.Println("Error is not of type LHTaskException")
		}
	}
}
