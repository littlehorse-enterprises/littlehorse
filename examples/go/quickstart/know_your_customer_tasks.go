package main

import (
	"errors"
	"math/rand/v2"
)

func VerifyIdentity(fullName, email string, ssn int) (string, error) {
	if rand.Float32() < 0.25 {
		return "", errors.New("the external identity verification API is down")
	}

	return "Successfully called external API to request verification for " + fullName + " at " + email, nil
}

func NotifyCustomerVerified(fullName, email string) string {
	return "Notification sent to customer " + fullName + " at " + email + " that their identity has been verified"
}

func NotifyCustomerNotVerified(fullName, email string) string {
	return "Notification sent to customer " + fullName + " at " + email + " that their identity has not been verified"
}
