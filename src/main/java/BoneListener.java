import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.List;

public class BoneListener extends ListenerAdapter {
    List<Thread> threads;
    public BoneListener(List<Thread> threads) {
        this.threads=threads;
    }

    public void printCommands(TextChannel c) {
        String out ="Hello, I am steve.\n!steve [day] [meal]\nday - today, tomorrow or tm\nmeal - breakfast, lunch, dinner or all\n";
        postPostToChannel(c, out);
    }
    public void postPostToChannel(TextChannel c,String content){
        c.sendMessage(content).complete();
    }
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        TextChannel c;
        if(!BoneBot.hasBotChannel(event.getGuild(),"steve-menu-feed")){
            c=event.getGuild().createTextChannel("steve-menu-feed").complete();
        }else{
            c= event.getGuild().getTextChannelsByName("steve-menu-feed",false).get(0);
        }
        Thread postMeals=new Thread(() -> {

            int oneDay=1000*60*60*24;
            while(true){
                try {

                    DateTime date = new DateTime();
                    int hour = date.getHourOfDay();
                    if(hour==8){
                        break;
                    }
                    Thread.sleep(1000*60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true){
                c.sendMessage(BoneParser.printAllMeals(false)).complete();
                try {
                    Thread.sleep(oneDay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        postMeals.run();
    }

    public void sendMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }
    public boolean isValidCommand(String day, String meal){
        boolean dayValid=day.equals("today")||day.equals("tomorrow");
        boolean mealValid=meal.equals("breakfast")||meal.equals("lunch")||meal.equals("dinner")||meal.equals("all");
        return dayValid && mealValid;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content.contains("!steve")&&content.indexOf("!steve")==0) {

            System.out.println("Got message: "+content);
            if(content.equals("!steve")){
                User user = event.getAuthor();
                printCommands((TextChannel) event.getChannel());
                System.out.println("Printing intro");
                return;
            }
            try {

                String input = content.substring(7);
                String[] args = input.split(" ");
                String day = args[0].toLowerCase().replaceAll("tm", "tomorrow");
                if (args.length ==2) {
                    String mealStr = args[1].toLowerCase();

                    if(!isValidCommand(day,mealStr)){
                        sendMessage(event.getAuthor(),"That is not a valid command, here is the format of a !steve command");
                        printCommands((TextChannel) event.getChannel());
                        System.out.println("Not valid command, printing intro");
                        return;
                    }
                    boolean tomorrow = day.equals("tomorrow");
                    if(mealStr.toLowerCase().equals("all")){
                        System.out.println("Printing all");
                        sendMessage(event.getAuthor(),BoneParser.printAllMeals(tomorrow));
                        return;
                    }
                    BoneParser.Meal meal = BoneParser.Meal.valueOf(mealStr.toUpperCase());
                    System.out.println("Printing single meal");
                    String mealsOut = BoneParser.printSingleMeal(meal,tomorrow);
                    sendMessage(event.getAuthor(),mealsOut);
                }
                else{
                    printCommands((TextChannel) event.getChannel());
                    System.out.println("Not valid command, printing intro");
                }

            } catch (Exception e) {
                sendMessage(event.getAuthor(),"That is not a valid command, here is the format of a !steve command");
                System.out.println("Not valid command, printing intro");
                printCommands((TextChannel) event.getChannel());
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
