package sportliga;

import java.util.LinkedList;

public class SimpleLogger {
    private static LinkedList<String> logList = new LinkedList<>();
    
    public static void log(String l){
        logList.addLast(l);
    }
    
    public static LinkedList<String> getLog() {
        LinkedList<String> logListReturned = logList;
        logList = new LinkedList<>();
        return logListReturned;
    }

}
