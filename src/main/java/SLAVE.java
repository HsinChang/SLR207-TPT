import java.util.*;
import java.io.*;

public class SLAVE {
    private static String rootFolder = "/tmp/xizhang/";
    private int opCode;
    private String fileToMap;
    private String outputFile;
    private String shuffleKey;
    private String reduceKey;
    private String reduceInput;

    public SLAVE(String[] args) throws IOException{
        this.opCode = Integer.parseInt(args[0]);
        if (this.opCode == 0){
            // Mode mapping
            this.fileToMap = args[1];
            this.map();
        } else if (this.opCode == 1) {
            this.shuffleKey = args[1];
            this.outputFile = args[2];
            this.shuffle(args);
        } else if (this.opCode == 2) {
            this.reduceKey = args[1];
            this.reduceInput = args[2];
            this.outputFile = args[3];
            this.reduce();
        } else {
            System.err.println("Error: wrong operation code.");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("[ERR] Wrong grammar, not enough arguments");
            return;
        }
        new SLAVE(args);
    }

    private void map() {
        System.out.println("[MAP]");
        String[] items = fileToMap.split("/");
        String numberOfFile = items[items.length-1].split("")[1];
        String fileOutput = rootFolder+"maps/UM"+numberOfFile+".txt";
        Set<String> keys = new HashSet<>();

        BufferedReader reader;
        BufferedWriter writer;
        try {
            reader = new BufferedReader(new FileReader(fileToMap));
            writer = new BufferedWriter(new FileWriter(fileOutput));
            String line = reader.readLine();

            String[] splited = line.split(" ");
            for (int i = 0; i < splited.length; ++i) {
                keys.add(splited[i]);
                writer.write(splited[i] + " 1\n");
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            System.out.println(next);
        }
    }

    private void shuffle(String[] args) {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (int i = 3; i < args.length; ++i) {
                reader = new BufferedReader(new FileReader(args[i]));

                String line = reader.readLine();
                while (line != null) {
                    if (line.contains(shuffleKey)) {
                        System.out.println(line);
                        writer.write(line + "\n");
                    }
                    // read next line
                    line = reader.readLine();
                }
                reader.close();

            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reduce() {
        BufferedReader reader;
        BufferedWriter writer;
        int nKey = 0;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            reader = new BufferedReader(new FileReader(reduceInput));

            while (reader.readLine() != null)
                nKey++;

            writer.write(reduceKey + " " + nKey + "\n");

            reader.close();
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
