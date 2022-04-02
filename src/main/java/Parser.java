import java.util.List;

public class Parser {
    ParserThread parser;
    public BoneParser.Meal meal;
    Thread thread;

    public Parser(BoneParser.Meal meal, boolean tomorrow) {
        this.meal = meal;
        parser = new ParserThread(meal, tomorrow);
        thread = new Thread(parser);
        thread.start();
    }

    public List<String> join() throws InterruptedException {
        thread.join();
        return parser.getMealList();

    }
}
