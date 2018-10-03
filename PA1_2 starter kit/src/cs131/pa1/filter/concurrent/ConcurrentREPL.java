package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;

import java.util.LinkedList;
import java.util.Scanner;

public class ConcurrentREPL {

	static String currentWorkingDirectory;
	
	public static void main(String[] args){
		currentWorkingDirectory = System.getProperty("user.dir");
		Scanner s = new Scanner(System.in);
		System.out.print(Message.WELCOME);
		String command;
		while(true) {
			//obtaining the command from the user
			System.out.print(Message.NEWCOMMAND);
			command = s.nextLine();
			if(command.equals("exit")) {
				break;
			} else if(!command.trim().equals("")) {
				processCommand(command);
			}
		}
		s.close();
		System.out.print(Message.GOODBYE);
	}
	
	public static void processCommand(String command){
		//building the filters list from the command
		ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
		LinkedList<Thread> curThreads = new LinkedList<Thread>();
		
		while(filterlist != null) {
			Thread newThread = new Thread(filterlist);
			newThread.start();
			curThreads.add(newThread);
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		
		for(Thread thread : curThreads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	

}
