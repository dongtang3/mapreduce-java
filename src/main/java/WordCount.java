import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCount implements MapFunction, ReduceFunction {
    @Override
    public List<KeyValue> map(String filename, String contents) {
        List<KeyValue> kvs = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(contents.toLowerCase());
        while (matcher.find()) {
            kvs.add(new KeyValue(matcher.group(), "1"));
        }
        return kvs;
    }

    @Override
    public String reduce(String key, List<String> values) {
        return String.valueOf(values.size());
    }
}