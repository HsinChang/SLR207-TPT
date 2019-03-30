import java.io.*;
import java.util.*;

public class MASTER {

    private static String userPrefix = "xizhang@";
    private static String domain = ".enst.fr";
    private static String splitsPath = "/tmp/xizhang/splits/";
    private static String slavePath = "/tmp/xizhang/slave.jar";

    private MASTER(){}

    public static void main(String[] args) throws IOException {
        MASTER master = new MASTER();
        master.foo();
    }

    private void foo() throws IOException {
        ProcessBuilder pb;
        BufferedReader reader;
        Process p;
        String tm, line;
        int index;

        //target machines
        Map<String, Integer> targetMachines = new HashMap<>();
        targetMachines.put("c128-17", 0);
        targetMachines.put("c128-22", 1);
        targetMachines.put("c128-24", 2);
        Map<String, List<String>> results = new HashMap<>();


        for (String machine : targetMachines.keySet()) {
            tm = String.format("%s%s%s", userPrefix, machine, domain);
            index = targetMachines.get(machine);

            //Start connection
            System.out.println("[BEGIN] Starting connection to machine " + tm);
            pb = new ProcessBuilder("ssh", tm, "hostname");
            pb.redirectErrorStream(true);
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = reader.readLine();
            if (!line.equals(machine)) {
                System.out.println("[ERR] Can't connect to machine " + tm + " where l = " + line);
                continue;
            }
            System.out.println("[OK] Connection available with machine " + tm);

            //Create directory
            pb = new ProcessBuilder("ssh", tm, "mkdir -p " + splitsPath);
            pb.redirectErrorStream(true);
            pb.start();
            System.out.println("[OK] Created dir on machine " + tm);

            //file copy
            pb = new ProcessBuilder("scp", splitsPath + "S" + index +".txt",
                    tm + ":" + splitsPath + "*" + "S" + index + ".txt");
            pb.redirectErrorStream(true);
            pb.start();
            System.out.println("[OK] Copied targetFiles on machine " + tm);

            //run slave
            pb = new ProcessBuilder("java", "-jar", slavePath);
            p = pb.start();
            System.out.println("[OK] Launched slave.jar on machine " + tm);
            // Receive the output from the Slave currently running, update the map
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                if (results.containsKey(line)) {
                    results.get(line).add("UM" + targetMachines.get(machine));
                } else {
                    List<String> newList = new ArrayList<>();
                    newList.add("UM" + targetMachines.get(machine));
                    results.put(line, newList);
                }
            }
        }
    }
    /*
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

     */
}
