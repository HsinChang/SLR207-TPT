import java.io.*;
import java.util.*;

public class MASTER {

    private static String userPrefix = "xizhang@";
    private static String domain = ".enst.fr";
    private static String splitsPath = "/tmp/xizhang/splits/";
    private static String mapsPath = "/tmp/xizhang/maps";
    private static String slavePath = "/tmp/xizhang/slave.jar";

    private MASTER(){}

    public static void main(String[] args) throws IOException, InterruptedException {
        MASTER master = new MASTER();
        master.foo();
    }

    private void foo() throws IOException, InterruptedException {
        BufferedReader reader;
        String line;

        //target machines
        Map<String, Integer> targetMachines = new HashMap<>();
        targetMachines.put("c128-17", 0);
        targetMachines.put("c128-22", 1);
        targetMachines.put("c128-24", 2);

        // Define the MR map
        Map<String, List<String>> keysUMxMap = new HashMap<>();

        String[] machines = targetMachines.keySet().toArray(new String[targetMachines.keySet().size()]);

        ProcessBuilder[] mappersProcessBuilder = new ProcessBuilder[machines.length];
        Process[] mappersProcess = new Process[machines.length];
        for (int j = 0; j < machines.length; j++) {
            mappersProcessBuilder[j] = new ProcessBuilder("scp", splitsPath + "S" + targetMachines.get(machines[j]) + ".txt ", machines[j] +":"+splitsPath);
            mappersProcess[j] = mappersProcessBuilder[j].start();
            System.out.println("[OK] file distributed on machine " + machines[j]);
        }
        for (int j = 0; j < machines.length; j++) {
            mappersProcessBuilder[j] = new ProcessBuilder("ssh", machines[j], "java -jar " + slavePath + " 0 " + splitsPath + "S" + targetMachines.get(machines[j]) + ".txt");
            mappersProcess[j] = mappersProcessBuilder[j].start();
            System.out.println("[OK] Launched slave.jar MAP MODE on machine " + machines[j]);
        }
        // Wait for their completion and read the outputs
        for (int j = 0; j < machines.length; j++) mappersProcess[j].waitFor();
        // Receive the outputs of the mappers
        for (int j = 0; j < machines.length; j++) {
            reader = new BufferedReader(new InputStreamReader(mappersProcess[j].getInputStream()));
            while ((line = reader.readLine()) != null) {
                if (keysUMxMap.containsKey(line)) {
                    keysUMxMap.get(line).add("UM" + targetMachines.get(machines[j]));
                } else {
                    List<String> newList = new ArrayList<>();
                    newList.add("UM" + targetMachines.get(machines[j]));
                    keysUMxMap.put(line, newList);
                }
            }
        }
        for (String r : keysUMxMap.keySet())
            System.out.println(r + " - <" + keysUMxMap.get(r).toString() + ">");

        //shuffle
        


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
