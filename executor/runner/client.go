package runner

import (
	"context"
	"os"

	"dagger.io/dagger"
)

func SetupDaggerClient() (context.Context, *dagger.Client) {
	ctx := context.Background()
	// 1. Initialize the Dagger client
	client, err := dagger.Connect(ctx, dagger.WithLogOutput(os.Stdout))
	if err != nil {
		panic(err)
	}
	return ctx, client
}
