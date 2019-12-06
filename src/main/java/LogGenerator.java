import org.apache.log4j.Logger;

public class LogGenerator {
    private static Logger logger = Logger.getLogger(LogGenerator.class);
    public static void main(String[] args) throws InterruptedException {

        while (true){
            Thread.sleep(5000);
            logger.info("hello word");
            logger.info("hello hadoop");
            logger.info("hello spark");
        }
    }

}
