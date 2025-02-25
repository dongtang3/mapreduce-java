public class MainWorker {
    public static void main(String[] args) throws Exception {
        // Create a WordCount instance that implements both MapFunction and ReduceFunction
        WordCount wordCount = new WordCount();
        Worker worker = new Worker(wordCount, wordCount);
        worker.run();
    }
}