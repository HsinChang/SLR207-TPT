import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.List;


public class DEPLOY {
	
	private static String splitsPath = "/tmp/xizhang/splits/";
    private static String mapsPath = "/tmp/xizhang/maps/";
    private static String reducesPath = "/tmp/xizhang/reduces/";

	public static void main(String[] args) {
		BufferedReader br = null, input = null;
		List<String> machines = null;
		ProcessBuilder pb = null;
		Process p = null;
		String l, slavePath = "/tmp/xizhang/", userPrefix = "xizhang@", slaveFile = "slave.jar", domain = ".enst.fr";

		try {
			br = new BufferedReader(new FileReader("./machines.txt"));
			machines = new ArrayList<>();
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) machines.add(userPrefix+sCurrentLine);

			br.close();

			for (String m : machines) {
				// ProcessBuilder for checking connection with hostname
				pb = new ProcessBuilder("ssh", m, "hostname");
				pb.redirectErrorStream(true);
				// Process
				p = pb.start();
				// Get output
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				l = userPrefix+input.readLine()+domain;
				if (!l.equals(m)) {
					System.out.println("[ERR] Can't connect to machine "+m);
					continue;
				}
				System.out.println("[OK] Connection available with machine "+m);

				// ProcessBuilder for mkdir
				pb = new ProcessBuilder("ssh", m, "mkdir -p "+slavePath);
				pb.redirectErrorStream(true);
				// Process
				p = pb.start();
				System.out.println("[OK] Created dir on machine "+m);
				
				pb = new ProcessBuilder("ssh", m, "mkdir -p "+splitsPath+" "+mapsPath+" "+reducesPath);
                p = pb.start();

                System.out.println("[OK] Created subdirs on machine " + m);


				// ProcessBuilder for copying slave to remote
				pb = new ProcessBuilder("scp", slavePath+slaveFile, m+":"+slavePath);
				pb.redirectErrorStream(true);
				// Process
				p = pb.start();
				System.out.println("[OK] Copied slaveFile on machine "+m);

			}		

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Clean the file openers
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}