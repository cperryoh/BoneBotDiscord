import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.joda.time.DateTime;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BoneParser {
    enum Meal {BREAKFAST, LUNCH, DINNER}

    public static List<String> getMealList(Meal meal, boolean tm) {
        CleanerProperties props = new CleanerProperties();

// set some properties to non-default values
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);

// do parsing
        try {
            TagNode tagNode;
            if (tm) {
                Date dt = new Date();
                DateTime dtOrg = new DateTime(dt);
                Date tomorrow = dtOrg.plusDays(1).toDate();
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String dateFormatted = simpleDateFormat.format(tomorrow);
                tagNode = new HtmlCleaner(props).clean(
                        new URL("https://rose-hulman.cafebonappetit.com/cafe-2/" + dateFormatted)
                );
            } else {
                tagNode = new HtmlCleaner(props).clean(
                        new URL("https://rose-hulman.cafebonappetit.com/")
                );
            }
            TagNode node = tagNode.findElementByAttValue("data-url-key", meal.toString().toLowerCase(), true, true).findElementByAttValue("data-loop-index", "1", true, true);
            List<? extends TagNode> wellBeingFood = node.getElementListByAttValue("class", "site-panel__daypart-item site-panel__daypart-item--has-well-being", true, true);
            List<? extends TagNode> food = node.getElementListByAttValue("class", "site-panel__daypart-item", true, true);
            List<String> foodDescriptions = new ArrayList<>();
            for (TagNode f : wellBeingFood) {
                TagNode titleOfFood = (TagNode) f.evaluateXPath("div/header/button")[0];
                String text = titleOfFood.getText().toString().replaceAll("\n", "");
                text = text.substring(4, text.length() - 7);
                foodDescriptions.add(text);
            }
            for (TagNode f : food) {
                TagNode titleOfFood = (TagNode) f.evaluateXPath("div/header/button")[0];
                String text = titleOfFood.getText().toString().replaceAll("\n", "");
                text = text.substring(4, text.length() - 7);
                foodDescriptions.add(text);
            }
            return foodDescriptions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String superCase(String str) {
        char[] chars = str.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    public static String printAllMeals(boolean tomorrow) {
        String out = "__**Hello gamers here are the**__ " + ((tomorrow) ? "__**meals for tomorrow**__" : "__**meals for today**__") + "\n\n";
        for (int i = 0; i < 3; i++) {
            Meal value = Meal.values()[i];
            List<String> breakfast = getMealList(value, tomorrow);
            if (value != Meal.DINNER)
                out += "**" + superCase(value.toString()) + "**\n";
            else
                out += "**FLOOOOOOOOOOOORRRRRRRRRR DINNNNNNNNEERRRRRR**\n";
            for (String s : breakfast) {
                out += " -" + s + "\n";
            }
            out += "\n";
        }
        return out;
    }

    public static String printSingleMeal(Meal meal, boolean tomorrow) {
        String out ="";
        for (int i = 0; i < 3; i++) {
            Meal value = Meal.values()[i];
            if (meal == value) {
                List<String> breakfast = getMealList(value, tomorrow);
                if (value != Meal.DINNER)
                    out += "**" + superCase(value.toString()) + "**\n";
                else
                    out += "**FLOOOOOOOOOOOORRRRRRRRRR DINNNNNNNNEERRRRRR**\n";
                for (String s : breakfast) {
                    out += " -" + s + "\n";
                }
                out += "\n";
            }
        }
        return out;
    }

    public static void main(String[] args) {
        List<String> breakfast = getMealList(Meal.BREAKFAST, true);
        System.out.println("Breakfast");
        for (String s : breakfast) {
            System.out.println(" -" + s);
        }
    }
}
