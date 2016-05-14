import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import election.Election;
import election.Logger;

public class Main {

	public static void main(String[] args) {
		try {
			Runnable r = new Runnable() {
				public void run() {
					Logger.getInstance();
				}
			};

			Thread loggerThread = new Thread(r);
			loggerThread.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();
			String party1 = br.readLine();
			String party2 = br.readLine();

			// long startTime = System.nanoTime();
			Election election = new Election(s, party1, party2);
			// long stopTime = System.nanoTime();
			// double elapsedTime = (double) TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS)
			//		/ 1000;
			// System.out.println("# Time for initialization(s): " + elapsedTime);

			// startTime = System.nanoTime();
			election.generateAllProfilePairs();
			// stopTime = System.nanoTime();
			// elapsedTime = (double) TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS) / 1000;
			// System.out.println("# Time for partition generation and sorting(s): " + elapsedTime);

			// startTime = System.nanoTime();
			election.generateAllNashEquilibrium();
			// stopTime = System.nanoTime();
			// elapsedTime = (double) TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS) / 1000;
			// System.out.println("# Time for generating all nash equilibrium(s): " + elapsedTime);			
			Logger.flush();
			loggerThread.interrupt();
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
