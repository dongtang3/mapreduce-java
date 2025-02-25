# MapReduce Java Project

This is a simple MapReduce implementation in Java that performs a word count on input text files.

## Project Structure

- **pom.xml**: Maven configuration file.
- **run.sh**: Shell script to run the application.
- **input/**: Directory containing input text files.
- **mr-tmp/**: Directory for storing intermediate MapReduce outputs.
- **src/main/java/**: Source code for the project, which includes:
  - `Coordinator.java`: Handles task coordination between map and reduce phases.
  - `KeyValue.java`: Represents intermediate key/value pairs in the MapReduce process.
  - `MainCoordinator.java`: Entry point for launching the Coordinator process.
  - `MainWorker.java`: Entry point for launching Worker processes.
  - `MapFunction.java`: Interface defining the Map function.
  - `ReduceFunction.java`: Interface defining the Reduce function.
  - `WordCount.java`: Implements both Map and Reduce functionality for word counting.
  - `Worker.java`: Executes map and reduce tasks as assigned by the Coordinator.

## How to Run

### On Linux/WSL

Use the `run.sh` script to compile and run the program:

1. Ensure the script has execute permissions:
   ```bash
   chmod +x run.sh
   ```
2. Run the script:
   ```bash
   ./run.sh
   ```

The script will compile the project using Maven, start the Coordinator, and launch the Worker processes. Logs are stored in the `logs/` directory, and intermediate MapReduce outputs are placed in the `mr-tmp/` directory.

## Additional Notes

- The Map phase produces intermediate files with key/value pairs, and the Reduce phase aggregates these intermediate results. The final reduced data is stored in the `mr-tmp/` directory.
- If more verbose logging for the Reduce phase is needed, consider modifying the Worker or Coordinator code to output additional debug information.


Happy coding!
