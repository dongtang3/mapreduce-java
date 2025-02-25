
import java.io.File;

public class MainCoordinator {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java MainCoordinator <input files>");
            System.exit(1);
        }

        // Ensure mr-tmp directory exists
        new File("mr-tmp").mkdirs();
        
        Coordinator coordinator = new Coordinator(args, 10); // 10 reduce tasks
        coordinator.start();
    }
}