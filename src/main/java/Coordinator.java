
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Coordinator {
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final List<String> inputFiles;
    private final int nReduce;
    private final Map<Integer, Task> mapTasks;
    private final Map<Integer, Task> reduceTasks;
    private final Map<Integer, Long> taskStartTimes;
    private boolean mapPhaseDone;
    private boolean allDone;

    private static class Task {
        String type;
        int taskId;
        String inputFile;
        boolean assigned;
        String workerId;

        Task(String type, int taskId, String inputFile) {
            this.type = type;
            this.taskId = taskId;
            this.inputFile = inputFile;
            this.assigned = false;
        }
    }

    public Coordinator(String[] files, int nReduce) throws IOException {
        this.serverSocket = new ServerSocket(12345);
        this.executorService = Executors.newFixedThreadPool(10);
        this.inputFiles = Arrays.asList(files);
        this.nReduce = nReduce;
        this.mapTasks = new HashMap<>();
        this.reduceTasks = new HashMap<>();
        this.taskStartTimes = new HashMap<>();
        this.mapPhaseDone = false;
        this.allDone = false;

        // Initialize Map tasks
        for (int i = 0; i < files.length; i++) {
            mapTasks.put(i, new Task("Map", i, files[i]));
        }
    }

    public void start() {
        System.out.println("Coordinator started on port 12345");
        while (!done()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleWorker(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleWorker(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String json = in.readLine();
            Message msg = Message.fromJson(json);

            if ("TaskRequest".equals(msg.getType())) {
                Task task = assignTask(msg.getWorkerId());
                if (task != null) {
                    Message response = new Message("TaskAssignment", task.type, task.taskId,
                            task.inputFile, nReduce);
                    out.println(response.toJson());
                    task.assigned = true;
                    task.workerId = msg.getWorkerId();
                    taskStartTimes.put(task.taskId, System.currentTimeMillis());
                } else if (done()) {
                    out.println(new Message("TaskAssignment", "Exit", -1, null, 0).toJson());
                }
            } else if ("TaskCompletion".equals(msg.getType())) {
                int taskId = msg.getTaskId();
                if (mapTasks.containsKey(taskId)) {
                    mapTasks.remove(taskId);
                    taskStartTimes.remove(taskId);
                    checkMapPhaseCompletion();
                } else if (reduceTasks.containsKey(taskId)) {
                    reduceTasks.remove(taskId);
                    taskStartTimes.remove(taskId);
                    if (reduceTasks.isEmpty()) {
                        allDone = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized Task assignTask(String workerId) {
        // Check for timed-out tasks first
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : taskStartTimes.entrySet()) {
            if (currentTime - entry.getValue() > 10000) { // 10 seconds timeout
                int taskId = entry.getKey();
                Task task = mapTasks.getOrDefault(taskId, reduceTasks.get(taskId));
                if (task != null && task.workerId != null && !task.workerId.equals(workerId)) {
                    task.assigned = false;
                    return task;
                }
            }
        }

        // Assign new task
        if (!mapPhaseDone) {
            for (Task task : mapTasks.values()) {
                if (!task.assigned) {
                    return task;
                }
            }
        } else {
            for (Task task : reduceTasks.values()) {
                if (!task.assigned) {
                    return task;
                }
            }
        }
        return null;
    }

    private synchronized void checkMapPhaseCompletion() {
        if (mapTasks.isEmpty() && !mapPhaseDone) {
            mapPhaseDone = true;
            // Initialize Reduce tasks
            for (int i = 0; i < nReduce; i++) {
                reduceTasks.put(i, new Task("Reduce", i, null));
            }
        }
    }

    public synchronized boolean done() {
        return mapPhaseDone && reduceTasks.isEmpty();
    }
}