import java.io.*;

public class MASTER {
    public static void main(String[] args) throws InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("ls", "-al", "/tmp");
        pb.redirectErrorStream(true);
        Thread.sleep(10000);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString();
            System.out.print(result);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
