package dto

type StageExecutionState string

const (
    StageWaiting StageExecutionState = "WAITING"
    StageRunning StageExecutionState = "RUNNING"
    StageSuccess StageExecutionState = "SUCCESS"
    StageFailed StageExecutionState = "FAILED"
    StageStopped StageExecutionState = "STOPPED"
)


 type PipelineExecutionState string

 const (
     PipelineWaiting PipelineExecutionState = "WAITING"
     PipelineRunning PipelineExecutionState = "RUNNING"
     PipelineSuccess PipelineExecutionState = "SUCCESS"
     PipelineFailed PipelineExecutionState = "FAILED"
 )

