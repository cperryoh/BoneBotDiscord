import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.joda.time.DateTime;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class BoneBot {
    public static boolean hasBotChannel(Guild g,String name){
        for(Channel c:g.getChannels()){
            if(c.getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) {
        try {

            //get day
            BoneParser.DayOfWeek d = BoneParser.getDayOfWeek(new Date());

            //store list of running threads
            List<Thread> threads = new ArrayList<>();

            //build and connect bot
            JDA builder = null;
            String authKey;
            boolean exists = Files.exists(Path.of(System.getProperty("user.dir")+File.separator+"key.txt"));

            //if auth key is not found in files ask for one
            if(!exists){
                Scanner scn = new Scanner(System.in);
                System.out.print("Enter bot auth key: ");
                authKey = scn.nextLine();
                PrintWriter printer=  new PrintWriter("key.txt");
                printer.print(authKey);
                printer.close();
            }else{
                //if found, read it
                Scanner scn =new Scanner(new File("key.txt"));
                authKey=scn.next();
            }

            //build bot
            try {
                builder = JDABuilder.createDefault(authKey).build().awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //add commands to bot
            CommandListUpdateAction commands = builder.updateCommands();
            commands.addCommands(
                    Commands.slash("howard", "Ask howard to fetch the bone meals for today")
                            .addOptions(new OptionData(INTEGER, "day", "the day to get the meals for") // USER type allows to include members of the server or other users by id
                                    .setRequired(true).addChoice("today",0).addChoice("tomorrow",1)) // This command requires a parameter
                            .addOptions(new OptionData(INTEGER, "meal", "the meal(s) to fetch").addChoice("breakfast",0).addChoice("brunch",1).addChoice("lunch",2).addChoice("dinner",3).addChoice("all",4)) // optional reason
            );
            commands.queue();

            //add listener to bot
            builder.addEventListener(new BoneListener(threads));
            List<Guild> guilds = builder.getGuilds();


            //loop through all guilds the bot is a member of and ensure they all have a steve channel
            //also create a thread for each guild to periodically post meal updates
            for(Guild g:guilds){
                TextChannel c;
                if(!hasBotChannel(g,"bone-menu")){
                    c=g.createTextChannel("bone-menu").complete();
                }else{
                    c= g.getTextChannelsByName("bone-menu",false).get(0);
                }

                //add thread
                Thread postMeals=new Thread(() -> {
                    TextChannel channel=c;
                    int oneDay=1000*60*60*24;
                    boolean waitForTime=false;

                    //wait till decided comes(10am)
                    while(!waitForTime){
                        try {

                            DateTime date = new DateTime();
                            int hour = date.getHourOfDay();
                            if(hour==10){
                                waitForTime=true;
                            }else {
                                Thread.sleep(1000 * 60);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //when it comes, print meal message to steve channel and wait 24hrs to print agian
                    while (true){
                        System.out.println("Sending out daily update to guild: "+g.getName());
                        channel.sendMessage(BoneParser.getAllMeals(false,true)).complete();
                        try {
                            Thread.sleep(oneDay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                //start and add thread
                postMeals.start();
                threads.add(postMeals);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
