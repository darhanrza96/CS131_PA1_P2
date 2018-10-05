package cs131.pa1.filter.concurrent;

public class PrintFilter extends ConcurrentFilter {
	public PrintFilter() {
		super();
	}
	
	public void process() {
		while(!isDone()) {
			processLine(input.poll());
		}
	}
	
	public String processLine(String line) {
		System.out.println(line);
		return null;
	}
	
	@Override
	public void run() {
		try {
			String temp = input.take();
			while(!temp.equals("poison pill")){
				processLine(temp);
			temp = input.take();
			}
			Thread.currentThread().interrupt();
			if (Thread.currentThread().isInterrupted()){
				System.out.println("yes");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
