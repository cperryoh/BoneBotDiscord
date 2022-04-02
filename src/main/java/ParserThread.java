import java.util.List;

public class ParserThread implements Runnable {
    List<String> mealList;
    BoneParser.Meal m;
    boolean tomorrow;
    public ParserThread(BoneParser.Meal m,boolean tomorrow){
        this.tomorrow=tomorrow;
        this.m=m;
    }
    @Override
    public void run() {
        mealList=BoneParser.getMealList(m,tomorrow);
    }
    public List<String> getMealList(){
        return mealList;
    }
}
