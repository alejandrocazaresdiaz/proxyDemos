package com.alejandrocazaresdiaz.demoproxy;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author AlexCazares
 */
@Component
public class ScheduledTasks {

    public static final int INTERVAL = 5000;//mili seconds
    public static Set<String> taskQueue = new HashSet<String>();

    @Scheduled(fixedRate = INTERVAL)
    public void jobFactory() {
        if (taskQueue != null && taskQueue.size() > 0) {
//            Date timeIn = new Date();
            for (String task : taskQueue) {
                if (task != null) {
                    switch (task) {
                        case "A":
//                            System.out.println("...");
                            break;
                        case "B":
//                            System.out.println("...");
                            break;
                    }
                }
            }
            taskQueue.clear();
//            System.out.println(timeIn + " ScheduledTasks.jobFactory() " + (new Date().getTime() - timeIn.getTime()) + " ms ");
        }
    }

    @Scheduled(fixedRate = 30000)
    public void sessionVerification() {
        taskQueue.add("A");
    }
    
    @PostConstruct
    public void postConstruct(){
        System.out.println("ScheduledTasks.jobFactory() will run every " + INTERVAL + " mili seconds ");
    }

}
