package config

import (
	"net/http"
	"os"
	"time"

	"github.com/AutoBuilders/AutoBuild/executor/handler"
)

func InitServer() (string, *http.Server) {
	mux := attachHTTPHandlers()

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

func attachHTTPHandlers() *http.ServeMux {
	mux := http.NewServeMux()

	// Define routes
	// mux.HandleFunc("GET /", func(w http.ResponseWriter, r *http.Request) {
	// 	fmt.Fprintln(w, "Welcome to the home page!")
	// })
	//
	// mux.HandleFunc("GET /api/status", func(w http.ResponseWriter, r *http.Request) {
	// 	w.Header().Set("Content-Type", "application/json")
	// 	fmt.Fprintln(w, `{"status": "OK"}`)
	// })

	availableHandlers := handler.GetAllHandlers()
	for _, availableHandler := range availableHandlers {
		urlWithMethod := availableHandler.HttpMethod + " " + availableHandler.Url
		mux.HandleFunc(urlWithMethod, availableHandler.HandlerFunc)
	}
	return mux
}

