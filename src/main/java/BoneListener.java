import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoneListener extends ListenerAdapter {
    List<Thread> threads;

    enum Day {today, tomorrow}

    public BoneListener(List<Thread> threads) {
        this.threads = threads;
    }

    public void printCommands(SlashCommandInteractionEvent c) {
        String out = "Hello, I am "+BoneBot.botName+".\n/howard [day] [meal]\nday - today, tomorrow or tm\nmeal - breakfast, lunch, dinner or all\n";
        c.getChannel().sendMessage(out).complete();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {


            switch (event.getName()) {

                //receive steve command
                case BoneBot.botName: {
                    //reply so message does not error out
                    event.reply("Hold on let me go look").complete();

                    //check that the user gave options for both meals
                    if (!(event.getOption("day") != null && event.getOption("meal") != null)) {
                        event.getChannel().sendMessage("That is not a valid command, here is the format of a /"+BoneBot.botName+" command");
                        printCommands(event);
                        System.out.println("Not valid command, printing intro");
                        return;
                    }

                    //pull user arguments
                    Day day = Day.values()[event.getOption("day").getAsInt()];
                    BoneParser.Meal meal = BoneParser.Meal.values()[event.getOption("meal").getAsInt()];


                    //determine if user is asking for tomorrow's meals
                    boolean tomorrow = day.toString().equals("tomorrow");



                    //if user asks for all meals, send them and return
                    if (meal.toString().equals("ALL")) {
                        System.out.println("Printing all");
                        LinkedHashMap<String,String> mealsOut = BoneParser.getAllMeals(tomorrow,true);
                        event.getChannel().sendMessageEmbeds(buildEmbed("Meals for "+((tomorrow)?"tomorrow":"today")+".",Color.red,mealsOut)).complete();

                        return;
                    }

                    Date dt = new Date();
                    DateTime dtOrg = new DateTime(dt);
                    int daysToAdd= (tomorrow)?1:0;
                    dt = dtOrg.plusDays(daysToAdd).toDate();
                    BoneParser.DayOfWeek dayOfWeek= BoneParser.getDayOfWeek(dt);
                    String dayRef= (tomorrow)?"tomorrow":"today";
                    if (dayOfWeek == BoneParser.DayOfWeek.SUNDAY||dayOfWeek == BoneParser.DayOfWeek.SATURDAY) {
                        if (dayOfWeek == BoneParser.DayOfWeek.SATURDAY) {
                            if (meal != BoneParser.Meal.BRUNCH) {
                                event.getChannel().sendMessage("Sorry, there is only brunch "+dayRef+".").complete();
                            } else {
                                ArrayList<String> mealsOut = BoneParser.printSingleMeal(meal, tomorrow);
                                event.getChannel().sendMessageEmbeds(buildEmbed(superCase(meal.toString())+" for "+((tomorrow)?"tomorrow":"today")+".",Color.blue, mealsOut.get(0), mealsOut.get(1))).complete();

                            }
                        } else if (dayOfWeek == BoneParser.DayOfWeek.SUNDAY) {
                            if (meal != BoneParser.Meal.BRUNCH && meal != BoneParser.Meal.DINNER) {
                                event.getChannel().sendMessage("Sorry, there is only brunch and dinner "+dayRef+".").complete();
                            } else {
                                ArrayList<String> mealsOut = BoneParser.printSingleMeal(meal, tomorrow);
                                event.getChannel().sendMessageEmbeds(buildEmbed(superCase(meal.toString())+" for "+((tomorrow)?"tomorrow":"today")+".",Color.blue, mealsOut.get(0), mealsOut.get(1))).complete();

                            }
                        }
                        return;
                    }else{
                        if(meal==BoneParser.Meal.BRUNCH){
                            event.getChannel().sendMessage("Sorry, there is no brunch "+dayRef+".").complete();
                            return;
                        }
                    }

                    //print that message was received
                    System.out.println("Got message: " + event.getCommandString());
                    try {

                        //print that we are sending a meal out
                        System.out.println("Printing single meal");

                        //get meal output
                        ArrayList<String> mealsOut = BoneParser.printSingleMeal(meal, tomorrow);

                        //send message

                        event.getChannel().sendMessageEmbeds(buildEmbed(superCase(meal.toString())+" for "+((tomorrow)?"tomorrow":"today")+".",Color.blue, mealsOut.get(0), mealsOut.get(1))).complete();

                    } catch (Exception e) {
                        event.getChannel().sendMessage("That is not a valid command, here is the format of a /"+BoneBot.botName+" command").complete();
                        System.out.println("Not valid command, printing intro");
                        printCommands(event);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String superCase(String str){
        char[] c = str.toLowerCase().toCharArray();
        c[0]=Character.toUpperCase(c[0]);
        return String.valueOf(c);
    }
    public static MessageEmbed buildEmbed(String title, Color color, String contentTitle, String content){
        // Create the EmbedBuilder instance
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(color);


        eb.addField(contentTitle, content, false);
        return eb.build();
    }
    public static MessageEmbed buildEmbed(String title, Color color, LinkedHashMap<String,String> fields){
        // Create the EmbedBuilder instance
        EmbedBuilder eb = new EmbedBuilder();


        eb.setTitle(title, null);

        eb.setColor(color);


        for(String k: fields.keySet()){
            String value=fields.get(k);
            eb.addField(k,value,false);
        }
        return eb.build();
    }
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {

        //check that guild has a steve message channel, if not make one
        TextChannel c;
        if (!BoneBot.hasBotChannel(event.getGuild(), "bone-menu")) {
            c = event.getGuild().createTextChannel("bone-menu").complete();
        } else {
            c = event.getGuild().getTextChannelsByName("bone-menu", false).get(0);
        }

        //thread to post meals every day at 10am
        Thread postMeals = new Thread(() -> {

            int oneDay = 1000 * 60 * 60 * 24;
            while (true) {

                //sync thread to 10am
                try {

                    DateTime date = new DateTime();
                    int hour = date.getHourOfDay();
                    if (hour == 8) {
                        break;
                    }
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //every 24hrs post message
            while (true) {
                LinkedHashMap<String,String> mealsOut = BoneParser.getAllMeals(false,true);
                c.sendMessageEmbeds(buildEmbed(superCase("Meals for today."),Color.red,mealsOut)).complete();


                try {
                    Thread.sleep(oneDay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //run thread
        postMeals.run();
    }


}
