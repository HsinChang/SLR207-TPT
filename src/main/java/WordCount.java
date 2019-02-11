import java.util.*;
import java.io.*;

public class WordCount {
    public static void main(String[] args) throws FileNotFoundException {
        // open the file
        Scanner console = new Scanner(System.in);
        System.out.print("What is the name of the text file? ");
        String fileName = console.nextLine();
        Scanner input = new Scanner(new File(fileName));

        // count occurrences
        Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
        while (input.hasNext()) {
            String next = input.next().replaceAll("\\.", "").replaceAll(",", "").replaceAll(";","").replaceAll(":","").toLowerCase();
            if (!wordCounts.containsKey(next)) {
                wordCounts.put(next, 1);
            } else {
                wordCounts.put(next, wordCounts.get(next) + 1);
            }
        }

        // get cutoff and report frequencies
        Map<String, Integer> countSortedByNumber = sortByCountValue(wordCounts);
        for (String word : countSortedByNumber.keySet()) {
            int count = countSortedByNumber.get(word);
            System.out.println(word + "\t" + count);
        }
    }

    public static Map<String, Integer> sortByCountValue(
            Map<String, Integer> wordCount) {

        // get entrySet from HashMap object
        Set<Map.Entry<String, Integer>> setOfWordEntries =
                wordCount.entrySet();

        // convert HashMap to List of Map entries
        List<Map.Entry<String, Integer>> listOfwordEntry =
                new ArrayList<Map.Entry<String, Integer>>(
                        setOfWordEntries);

        // sort list of entries using Collections.sort(ls, cmptr);
        Collections.sort(listOfwordEntry,
                new Comparator<Map.Entry<String, Integer>>() {
                    public int compare(Map.Entry<String, Integer> es1,
                                       Map.Entry<String, Integer> es2) {
                        return es2.getValue().compareTo(es1.getValue());
                    }
                });

        // store into LinkedHashMap for maintaining insertion
        Map<String, Integer> wordLHMap =
                new LinkedHashMap<String, Integer>();

        // iterating list and storing in LinkedHahsMap
        for(Map.Entry<String, Integer> map : listOfwordEntry){
            wordLHMap.put(map.getKey(), map.getValue());
        }

        return wordLHMap;
    }
}