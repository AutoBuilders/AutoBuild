package config

import (
	"net/http"
	"os"
	"time"
)

func InitServer(mux *http.ServeMux) (string, *http.Server) {

	// Configure server settings explicitly
	serverPortAddr := ":" + os.Getenv("EXECUTOR_PORT")
	server := &http.Server{
		Addr:           serverPortAddr,
		Handler:        mux,
		ReadTimeout:    10 * time.Second, // Max duration reading the entire request
		WriteTimeout:   10 * time.Second, // Max duration writing the response
		MaxHeaderBytes: 1 << 20,          // 1 MB max header size
	}
	return serverPortAddr, server
}

