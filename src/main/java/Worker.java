import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Worker {
    private static final Gson gson = new Gson();
    // Use environment variable or default value
    private final String host = System.getenv("MR_COORDINATOR_HOST") != null ? System.getenv("MR_COORDINATOR_HOST") : "localhost";
    private final int port = 12345;
    private final String workerId;
    private final MapFunction mapFunc;
    private final ReduceFunction reduceFunc;

    public Worker(MapFunction mapFunc, ReduceFunction reduceFunc) {
        this.workerId = "worker-" + UUID.randomUUID().toString();
        this.mapFunc = mapFunc;
        this.reduceFunc = reduceFunc;
        // Ensure the creation of mr-tmp directory
        try {
            Files.createDirectories(Paths.get("mr-tmp"));
        } catch (IOException e) {
            System.err.println("Warning: Could not create mr-tmp directory: " + e.getMessage());
        }
    }

    public void run() throws IOException, InterruptedException {
        while (true) {
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Request task
                Message request = new Message("TaskRequest", workerId);
                out.println(gson.toJson(request));

                // Receive task
                String response = in.readLine();
                if (response == null) {
                    System.err.println("Received null response from coordinator, retrying...");
                    Thread.sleep(1000);
                    continue;
                }
                
                Message task = gson.fromJson(response, Message.class);

                if ("Exit".equals(task.getTaskType())) {
                    break;
                }

                if ("Map".equals(task.getTaskType())) {
                    doMap(task.getTaskId(), task.getInputFile(), task.getReduceNum());
                } else if ("Reduce".equals(task.getTaskType())) {
                    doReduce(task.getTaskId());
                }

                // Report completion
                Message completion = new Message("TaskCompletion", null, task.getTaskType(), task.getTaskId(), null, 0);
                out.println(gson.toJson(completion));
            } catch (Exception e) {
                System.err.println("Worker error: " + e.getMessage());
                Thread.sleep(1000); // Wait before retrying
            }
        }
    }

    private void doMap(int taskId, String inputFile, int nReduce) throws IOException {
        // Read input file
        String content = new String(Files.readAllBytes(Paths.get(inputFile)));
        
        // Apply map function
        List<KeyValue> kvs = mapFunc.map(inputFile, content);

        // Partition into nReduce intermediate files
        Map<Integer, List<KeyValue>> buckets = new HashMap<>();
        for (KeyValue kv : kvs) {
            int bucket = Math.abs(kv.getKey().hashCode() % nReduce);
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(kv);
        }

        // Write intermediate files
        for (Map.Entry<Integer, List<KeyValue>> entry : buckets.entrySet()) {
            int reducerId = entry.getKey();
            String filename = String.format("mr-tmp/mr-%d-%d", taskId, reducerId);
            File tempFile = File.createTempFile("mr-", ".tmp", new File("mr-tmp"));
            try (PrintWriter writer = new PrintWriter(tempFile)) {
                for (KeyValue kv : entry.getValue()) {
                    writer.println(gson.toJson(kv));
                }
            }
            Files.move(tempFile.toPath(), new File(filename).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void doReduce(int reduceTaskId) throws IOException {
        // Collect all intermediate files for this reduce task
        Map<String, List<String>> keyToValues = new HashMap<>();
        
        File tmpDir = new File("mr-tmp");
        File[] files = tmpDir.listFiles((dir, name) -> name.matches("mr-\\d+-" + reduceTaskId));
        
        if (files != null) {
            for (File f : files) {
                List<String> lines = Files.readAllLines(f.toPath());
                for (String line : lines) {
                    KeyValue kv = gson.fromJson(line, KeyValue.class);
                    keyToValues.computeIfAbsent(kv.getKey(), k -> new ArrayList<>()).add(kv.getValue());
                }
            }
        }

        // Process each key
        List<String> keys = new ArrayList<>(keyToValues.keySet());
        Collections.sort(keys);

        // Write output
        File tempFile = File.createTempFile("mr-out-", ".tmp", new File("mr-tmp"));
        try (PrintWriter writer = new PrintWriter(tempFile)) {
            for (String key : keys) {
                String output = reduceFunc.reduce(key, keyToValues.get(key));
                writer.println(key + "\t" + output);
            }
        }
        String outputFile = "mr-tmp/mr-out-" + reduceTaskId;
        Files.move(tempFile.toPath(), new File(outputFile).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}