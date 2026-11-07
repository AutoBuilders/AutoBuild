package handler

import "net/http"

type HttpHandler struct {
	HttpMethod string
	Url string
	HandlerFunc func(w http.ResponseWriter, r * http.Request)
}

var availableHandlers = []HttpHandler{
	{
		HttpMethod: executePipelineHttpMethod,
		Url: executePipelineUrl,
		HandlerFunc: handleExecutePipeline,
	},
}

func GetAllHandlers() []HttpHandler {
	return availableHandlers
}
