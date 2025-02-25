
import java.util.List;

public interface MapFunction {
    List<KeyValue> map(String filename, String contents);
}