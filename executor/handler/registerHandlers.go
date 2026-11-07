package handler

import "net/http"

func RegisterHandlers(mux *http.ServeMux) {
	mux.HandleFunc(executePipelineHttpMethod + " " + executePipelineUrl, handleExecutePipeline)
}

