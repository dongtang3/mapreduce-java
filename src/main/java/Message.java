
import com.google.gson.Gson;
public class Message {
    private String type;         // "TaskRequest", "TaskAssignment", "TaskCompletion"
    private String workerId;     // Unique worker identifier
    private String taskType;     // "Map" or "Reduce"
    private int taskId;          // Task identifier
    private String inputFile;    // Input file for Map tasks
    private int reduceNum;       // Number of reduce tasks (for Map tasks)

    // Constructors
    public Message() {}

    public Message(String type, String workerId) {
        this.type = type;
        this.workerId = workerId;
    }

    public Message(String type, String taskType, int taskId, String inputFile, int reduceNum) {
        this.type = type;
        this.taskType = taskType;
        this.taskId = taskId;
        this.inputFile = inputFile;
        this.reduceNum = reduceNum;
    }

    public Message(String type, String workerId, String taskType, int taskId, String inputFile, int reduceNum) {
        this.type = type;
        this.workerId = workerId;
        this.taskType = taskType;
        this.taskId = taskId;
        this.inputFile = inputFile;
        this.reduceNum = reduceNum;
    }

    public Message(String type, String workerId, String taskType, int taskId) {
        this.type = type;
        this.workerId = workerId;
        this.taskType = taskType;
        this.taskId = taskId;
        this.inputFile = null;
        this.reduceNum = 0;
    }

    // Getters and setters


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public int getReduceNum() {
        return reduceNum;
    }

    public void setReduceNum(int reduceNum) {
        this.reduceNum = reduceNum;
    }
    // Serialization
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
}