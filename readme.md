POST http://localhost:8084/api/processes/Process_hims_op/start

GET http://localhost:8084/api/instances/{instanceid}/tasks

POST http://localhost:8084/api/tasks/{taskid}/complete
{
  "lab" : true,
  "pharmacy" : true
  
}


POST /api/tasks/{taskId}/due-date
{
  "dueDateTime": "2025-09-01T12:00:00Z"
}

