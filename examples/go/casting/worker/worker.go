package main

import (
	"log"
)

func StringMethod(value string) string {
	log.Printf("Executing string-method with value: %s", value)
	return value
}

func IntMethod(value int) int {
	result := value * 2
	log.Printf("Executing int-method with value: %d, doubling to: %d", value, result)
	return result
}

func DoubleMethod(value float64) float64 {
	result := value * 0.9
	log.Printf("Executing double-method with value: %f, reducing to: %f", value, result)
	return result
}

func BoolMethod(value bool) bool {
	result := !value
	log.Printf("Executing bool-method with value: %t, toggling to: %t", value, result)
	return result
}
