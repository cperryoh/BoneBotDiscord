import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.joda.time.DateTime;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class BoneParser {
    enum Meal {BREAKFAST, BRUNCH, LUNCH, DINNER, ALL}

    static Meal[] sat = {Meal.BRUNCH};
    static Meal[] sun = {Meal.BRUNCH, Meal.DINNER};
    static Meal[] normalDay = {Meal.BREAKFAST,Meal.LUNCH,Meal.DINNER};

    enum DayOfWeek {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}

    public static DayOfWeek getDayOfWeek(Date dt) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE");
        return DayOfWeek.valueOf(df.format(dt).toUpperCase());
    }

    public static List<String> getMealList(Meal meal, boolean tm) {
        //set up html grabber
        CleanerProperties props = new CleanerProperties();
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);


        try {

            //pull data for either today or tommorow
            TagNode tagNode;
            Date dt = new Date();
            DateTime dtOrg = new DateTime(dt);
            //if request is for tomorrow, adjust the date
            if (tm) {
                Date tomorrow = dtOrg.plusDays(1).toDate();
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String dateFormatted = simpleDateFormat.format(tomorrow);
                tagNode = new HtmlCleaner(props).clean(
                        new URL("https://rose-hulman.cafebonappetit.com/cafe-2/" + dateFormatted)
                );
            }
            //otherwise, pull data normally.
            else {
                tagNode = new HtmlCleaner(props).clean(
                        new URL("https://rose-hulman.cafebonappetit.com/")
                );
            }


            //get node that holds all items
            TagNode node = tagNode.findElementByAttValue("data-url-key", meal.toString().toLowerCase(), true, true).findElementByAttValue("data-loop-index", "1", true, true);

            //pull well-being food
            List<? extends TagNode> wellBeingFood = node.getElementListByAttValue("class", "site-panel__daypart-item site-panel__daypart-item--has-well-being", true, true);

            //pull normal food
            List<? extends TagNode> food = node.getElementListByAttValue("class", "site-panel__daypart-item", true, true);

            //place to store menu descriptions
            List<String> foodDescriptions = new ArrayList<>();

            //loop and parse well being food
            for (TagNode f : wellBeingFood) {
                TagNode titleOfFood = (TagNode) f.evaluateXPath("div/header/button")[0];
                String text = titleOfFood.getText().toString().replaceAll("\n", "");
                text = text.substring(4, text.length() - 7);
                foodDescriptions.add(text);
            }

            //loop and parse normal food
            for (TagNode f : food) {
                TagNode titleOfFood = (TagNode) f.evaluateXPath("div/header/button")[0];
                String text = titleOfFood.getText().toString().replaceAll("\n", "");
                text = text.substring(4, text.length() - 7);
                foodDescriptions.add(text);
            }

            //return food
            return foodDescriptions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //makes a string super case hello=>Hello
    public static String superCase(String str) {
        char[] chars = str.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }


    /**
     * Gets all meals based on what day is requested
     * @param tomorrow- get today(false) or tomorrow
     * @param intro- Add intro to string or not
     * @return String containing the summary of all meals that day
     */

    public static LinkedHashMap<String,String> getAllMeals(boolean tomorrow, boolean intro) {

        //Get day as an DayOfWeek variable
        Date dt = new Date();
        DateTime dateTime = new DateTime(dt);
        int daysToAdd = (tomorrow) ? 1 : 0;
        dt = dateTime.plusDays(daysToAdd).toDate();
        DayOfWeek day = getDayOfWeek(dt);
        Meal[] mealsToPrint;


        //as long as it is not saturday or sunday print meals normally
        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
            mealsToPrint=normalDay;
        }else{
            //if it is saturday or sunday, decide which list of meals to loop through
            mealsToPrint = (day==DayOfWeek.SUNDAY)?sun:sat;

        }
        LinkedHashMap<String,String> mealOutPuts=new LinkedHashMap<>();
        for (int i = 0; i < mealsToPrint.length; i++) {
            Meal value = mealsToPrint[i];
            List<String> mealList = getMealList(value, tomorrow);
            String header="";
            String out="";

            //if not dinner just make bold name of meal
            if (value != Meal.DINNER)
                header += "**" + superCase(value.toString()) + "**";

            //if dinner add floor dinner in bold
            else
                header += "**FLOOOOOOOOOOOORRRRRRRRRR DINNNNNNNNEERRRRRR**";

            //loop through value fetched from parsed and add them to the string
            for (String s : mealList) {
                out += " -" + s + "\n";
            }
            out += "\n";
            mealOutPuts.put(header,out);
        }
        return mealOutPuts;
    }

    public static ArrayList<String> printSingleMeal(Meal meal, boolean tomorrow) {

        //Same thing as all meals, but it filters which to print via the meal variable

        String header = "";
        String out="";
        for (int i = 0; i < Meal.values().length; i++) {
            Meal value = Meal.values()[i];
            if (meal == value) {
                List<String> mealList = getMealList(value, tomorrow);
                header += "**" + superCase(value.toString()) + "**\n";
                for (String s : mealList) {
                    out += " -" + s + "\n";
                }
                out += "\n";
            }
        }
        ArrayList<String> values = new ArrayList<>();
        values.add(header);
        values.add(out);
        return values;
    }

    //test
    public static void main(String[] args) {
        List<String> breakfast = getMealList(Meal.BREAKFAST, true);
        System.out.println("Breakfast");
        for (String s : breakfast) {
            System.out.println(" -" + s);
        }
    }
}
