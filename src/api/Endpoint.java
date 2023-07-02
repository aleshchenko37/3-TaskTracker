package api;

public enum Endpoint {
        POST_ADD_TASK, 
        POST_UPDATE_TASK, 
        GET_LIST_OF_TASKS, 
        DELETE_TASK, 
        DELETE_ALL_TASKS, 
        GET_TASK, 

        POST_ADD_EPIC, 
        POST_UPDATE_EPIC, 
        GET_LIST_OF_EPICS, 
        GET_LIST_OF_EPIC_SUBTASKS, 
        DELETE_EPIC, 
        DELETE_ALL_EPICS, 
        GET_EPIC, 

        POST_ADD_SUBTASK, 
        POST_UPDATE_SUBTASK, 
        GET_LIST_OF_SUBTASKS, 
        DELETE_SUBTASK, 
        DELETE_ALL_SUBTASKS, 
        GET_SUBTASK, 

        GET_HISTORY, 
        GET_PRIORITIZED_TASKS, 
        NO_SUCH_ENDPOINT
}
