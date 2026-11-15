package customerror

type NotFoundError struct {
	Message string
}

func (e *NotFoundError) Error() string {
	return e.Message
}

func NewNotFoundError(msg string) *NotFoundError {
	return &NotFoundError{Message: msg}
}

// Is allows errors.Is() to match any *NotFoundError regardless of Message value.
func (e *NotFoundError) Is(target error) bool {
	_, ok := target.(*NotFoundError)
	return ok
}
