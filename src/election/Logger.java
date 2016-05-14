package election;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger extends Thread {
    private static final Logger INSTANCE = new Logger();
    private static ArrayList<String> allNashEquilibrium;
    private static int currentIndex = 0;
    // private static BufferedWriter out;
    
    private Logger() {
    	allNashEquilibrium = new ArrayList<String>();
    	/*
    	try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(java.io.FileDescriptor.out), "ASCII"), 512);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
    	Runnable runnable = new Runnable() {
    	    public void run() {
				while (!isInterrupted()) {
					flush();
				}
    	    }
    	};
    	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    	executor.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
    }
    
    public static void flush() {
    	// try {			
		for (int i = currentIndex; i < allNashEquilibrium.size(); i++) {
			String np = allNashEquilibrium.get(i);
			if (np != null && ! np.isEmpty() && !np.startsWith("null"))
				System.out.print(np);
			currentIndex++;
		}
			
			// out.flush();
		/*
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	*/
    	
    }
    
    public static Logger getInstance() {
        return INSTANCE;
    }
    
    public static void add(String np) {
    	allNashEquilibrium.add(np);
    }
    
    public static void reset() {
    	currentIndex = 0;
    	allNashEquilibrium.clear();
    }
    
    public static int getSize() {
    	return allNashEquilibrium.size();
    }
}
