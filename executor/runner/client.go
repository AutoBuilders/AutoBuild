package runner

import (
	"context"
	"os"

	"dagger.io/dagger"
)

func SetupDaggerClient() *DaggerRunner {
	ctx := context.Background()
	// 1. Initialize the Dagger client
	client, err := dagger.Connect(ctx, dagger.WithLogOutput(os.Stdout))
	if err != nil {
		panic(err)
	}
	// return ctx, client

	return NewDaggerRunner(client)
}
