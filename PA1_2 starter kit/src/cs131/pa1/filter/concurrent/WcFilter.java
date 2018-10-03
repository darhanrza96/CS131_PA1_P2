package cs131.pa1.filter.concurrent;

public class WcFilter extends ConcurrentFilter {
	private int linecount;
	private int wordcount;
	private int charcount;
	private boolean isDone = false;
	
	public WcFilter() {
		super();
	}
	
	public void process() {
		if(isDone()) {
			output.add(processLine(null));
		} else {
			super.process();
		}
	}
	
	public String processLine(String line) {
		//prints current result if ever passed a null
		if(line == null) {
			return linecount + " " + wordcount + " " + charcount;
		}
		
		if(isDone()) {
			String[] wct = line.split(" ");
			wordcount += wct.length;
			String[] cct = line.split("|");
			charcount += cct.length;
			return ++linecount + " " + wordcount + " " + charcount;
		} else {
			linecount++;
			String[] wct = line.split(" ");
			wordcount += wct.length;
			String[] cct = line.split("|");
			charcount += cct.length;
			return null;
		}
	}
	
	
	@Override
	public void run() {
		try {
			String temp = input.take();
			while(!temp.equals("poison pill")) {
				String processedLine = processLine(temp);
				temp = input.take();
			}
			output.put(processLine(null));
			output.put("poison pill");
			

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isDone(){
		return isDone;
	}
	
	
}
