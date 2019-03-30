import java.io.*;
import java.util.concurrent.*;

public class MASTER {

    private static String userPrefix = "xizhang@";
    private static String domain = ".enst.fr";
    private static String splitsPath = "/tmp/xizhang/splits/";
    private static String slavePath = "/tmp/xizhang/slave.jar";

    public static void main(String[] args) {
        final LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/xizhang/slave.jar");

        //solution 1 chosen
        pb.redirectErrorStream(true);

        //the thread to get the terminal output of slave
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread streamThread = new Thread(
                    () -> {
                        try{
                            String line;
                            while ((line = reader.readLine()) != null) {
                                lbq.add(line);
                                System.out.print(line);
                            }
                            reader.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
            );
            streamThread.start();

            //set timeout for master
            if (lbq.poll(2, TimeUnit.SECONDS) == null) {
                System.err.println("timeout");
                process.destroy();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
