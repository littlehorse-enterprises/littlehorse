package main

import (
	"log"
	"os"
	"os/signal"
	"syscall"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	usertasks "github.com/littlehorse-enterprises/littlehorse/examples/go/user-tasks"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	config, _ := examples.LoadConfigAndClient()
	worker, err := littlehorse.NewTaskWorker(config, usertasks.SendEmail, usertasks.EmailTaskName)
	if err != nil {
		log.Fatal(err)
	}
	if err := worker.RegisterTaskDef(); err != nil {
		log.Fatal(err)
	}
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	defer signal.Stop(stop)
	go func() {
		<-stop
		if err := worker.Close(); err != nil {
			log.Print(err)
		}
	}()
	log.Println("Starting send-email worker...")
	if err := worker.Start(); err != nil {
		log.Fatal(err)
	}
}
