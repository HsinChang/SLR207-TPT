import java.io.*;
import java.util.*;

public class MASTER {

    private static String userPrefix = "xizhang@";
    private static String domain = ".enst.fr";
    private static String splitsPath = "/tmp/xizhang/splits/";
    private static String mapsPath = "/tmp/xizhang/maps/";
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
        Map<String, Integer> targetMachines = new LinkedHashMap<>();
        targetMachines.put("c125-07", 0);
        targetMachines.put("c125-09", 1);
        targetMachines.put("c125-11", 2);

        // Define the MR map
        Map<String, List<String>> keysUMxMap = new LinkedHashMap<>();

        String[] machines = targetMachines.keySet().toArray(new String[targetMachines.keySet().size()]);

        Map<String, String> machinesUMxMap = new LinkedHashMap<>();
        for (int j = 0; j < machines.length; j++)
            machinesUMxMap.put("UM"+j, machines[j]);


        for (int j = 0; j < machines.length; j++) {
            ProcessBuilder pb = new ProcessBuilder("scp", splitsPath + "S" + targetMachines.get(machines[j]) + ".txt", machines[j] +":"+splitsPath);
            Process p = pb.start();
            p.waitFor();
            System.out.println("[OK] file distributed on machine " + machines[j]);
        }
        ProcessBuilder[] mappersProcessBuilder = new ProcessBuilder[machines.length];
        Process[] mappersProcess = new Process[machines.length];
        for (int j = 0; j < machines.length; j++) {
            mappersProcessBuilder[j] = new ProcessBuilder("ssh", machines[j], "java -jar " + slavePath + " 0 " + splitsPath + "S" + targetMachines.get(machines[j]) + ".txt");
            mappersProcess[j] = mappersProcessBuilder[j].start();
            System.out.println("[OK] Launched slave.jar MAP MODE on machine " + machines[j]);
        }
        long startTime = System.currentTimeMillis();
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
        long endTime = System.currentTimeMillis();
        System.out.println("Phase MAP took " + (endTime - startTime)
                + " milliseconds");

        //shuffle
        String[] keys = keysUMxMap.keySet().toArray(new String[keysUMxMap.keySet().size()]);    // Obtain the keys
        int nReducers = machines.length < keys.length ? machines.length : keys.length;                      // Choose n_reducers
        Map<String, List<String>> machineKeysMap = new LinkedHashMap<>();// Map for machine-keys
        Map<String, String> keysMachineMap = new LinkedHashMap<>();
        Map<String, Integer> finalMap = new LinkedHashMap<>();

        int tempNumberProcess = 0;
        for (int i = 0; i < keys.length; i++) {
            String reducekey = keys[i];
            String reducerID = machines[i%nReducers];
            if (!machineKeysMap.containsKey(reducerID)) machineKeysMap.put(reducerID, new ArrayList<>());
            machineKeysMap.get(reducerID).add(reducekey);
            keysMachineMap.put(reducekey, reducerID);
        }
        System.out.println(machineKeysMap.toString());
        ProcessBuilder[] shuffleDistProcessBuilder = new ProcessBuilder[machines.length*machines.length];
        Process[] shuffleDistProcess = new Process[machines.length*machines.length];
        ProcessBuilder[] shuffleProcessBuilder = new ProcessBuilder[keys.length];
        Process[] shuffleProcess = new Process[keys.length];
        for (int j = 0; j < machines.length; j++) {
            List<String> tempUMs = new ArrayList<String>();
            List<String> tempKeys = machineKeysMap.get(machines[j]);
            for (String key : tempKeys){
                List<String> ttUM = keysUMxMap.get(key);
                for (String um : ttUM){
                    if (!tempUMs.contains(um)) tempUMs.add(um);
                }
            }
            tempUMs.remove("UM"+ j);
            for (String um :tempUMs){
                if(tempUMs.isEmpty()) {
                    continue;
                }
                shuffleDistProcessBuilder[tempNumberProcess] = new ProcessBuilder("scp", machinesUMxMap.get(um)+":"+mapsPath+um+".txt", machines[j]+":"+mapsPath);
                shuffleDistProcess[tempNumberProcess] = shuffleDistProcessBuilder[tempNumberProcess].start();
                tempNumberProcess += 1;
            }
            System.out.println("[OK] UMs files for SHUFFLE copied to " + machines[j]);
        }
        startTime = System.currentTimeMillis();
        for (int j = 0; j < tempNumberProcess; j++) {
            shuffleDistProcess[j].waitFor();
        }

        for (int j = 0; j < keys.length; j++) {
            List<String> tempUMs = new ArrayList<String>();
            for (String um : keysUMxMap.get(keys[j])) {
                tempUMs.add(mapsPath+um+".txt");
            }
            String machine = keysMachineMap.get(keys[j]);
            String UMs = String.join(" ", tempUMs);
            shuffleProcessBuilder[j] = new ProcessBuilder("ssh", machine, "java", "-jar", slavePath, "1", keys[j], mapsPath + "SM" + j + ".txt", UMs);
            shuffleProcess[j] = shuffleProcessBuilder[j].start();
        }
        for (int j = 0; j < keys.length; j++) {
            shuffleProcess[j].waitFor();
        }
        System.out.println("[OK] SHUFFLE finished ");
        endTime = System.currentTimeMillis();
        System.out.println("Phase SHUFFLE took " + (endTime - startTime)
                + " milliseconds");

        ProcessBuilder[] reduceProcessBuilder = new ProcessBuilder[keys.length];
        Process[] reduceProcess = new Process[keys.length];


        //reduce
        for (int j = 0; j < keys.length; j++) {
            String machine = keysMachineMap.get(keys[j]);
            reduceProcessBuilder[j] = new ProcessBuilder("ssh", machine, "java", "-jar", slavePath, "2", keys[j], mapsPath + "SM" + j + ".txt", splitsPath+"RM"+j+".txt");
            reduceProcess[j] = reduceProcessBuilder[j].start();
            System.out.println("[OK]reduce of key " + keys[j]+ " begins on machine "+machine);
        }
        startTime = System.currentTimeMillis();
        for (int j = 0; j < keys.length; j++) {
            reduceProcess[j].waitFor();
        }
        endTime = System.currentTimeMillis();
        System.out.println("Phase REDUCE took " + (endTime - startTime)
                + " milliseconds");

        //print
        startTime = System.currentTimeMillis();
        for (int j = 0; j<keys.length; j++) {
            String machine = keysMachineMap.get(keys[j]);
            ProcessBuilder pb = new ProcessBuilder("ssh", machine, "cat", splitsPath+"RM"+j+".txt");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Phase PRINT took " + (endTime - startTime)
                + " milliseconds");


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
