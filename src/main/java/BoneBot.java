import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import org.joda.time.DateTime;

import java.util.*;

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
            List<Thread> threads = new ArrayList<>();
            JDA builder = null;
            try {
                builder = JDABuilder.createDefault("OTU4NTIyNzg4MjgzNTU1OTEx.YkOj6Q.uTjssv-7E9ox7b9It37BH8Qm1eg").build().awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            builder.addEventListener(new BoneListener(threads));
            List<Guild> guilds = builder.getGuilds();
            for(Guild g:guilds){
                TextChannel c;
                if(!hasBotChannel(g,"steve-menu-feed")){
                    c=g.createTextChannel("steve-menu-feed").complete();
                }else{
                    c= g.getTextChannelsByName("steve-menu-feed",false).get(0);
                }
                Thread postMeals=new Thread(() -> {
                    TextChannel channel=c;
                    int oneDay=1000*60*60*24;
                    boolean waitForTime=false;
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
                    while (true){
                        System.out.println("Sending out daily update to guild: "+g.getName());
                        channel.sendMessage(BoneParser.printAllMeals(false)).complete();
                        try {
                            Thread.sleep(oneDay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                postMeals.start();
                threads.add(postMeals);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
