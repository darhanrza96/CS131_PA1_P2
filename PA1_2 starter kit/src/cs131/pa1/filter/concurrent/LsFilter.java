package cs131.pa1.filter.concurrent;
import java.io.File;

public class LsFilter extends ConcurrentFilter{
	int counter;
	File folder;
	File[] flist;
	
	public LsFilter() {
		super();
		counter = 0;
		folder = new File(ConcurrentREPL.currentWorkingDirectory);
		flist = folder.listFiles();
	}
	
	@Override
	public void process() {
		while(counter < flist.length) {
			output.add(processLine(""));
		}
	}
	
	@Override
	public String processLine(String line) {
		return flist[counter++].getName();
	}

	@Override
	public void run() {
		try {
			while(counter < flist.length) {
				output.put(processLine(""));
			}
			output.put("poison pill");
			Thread.currentThread().interrupt();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
