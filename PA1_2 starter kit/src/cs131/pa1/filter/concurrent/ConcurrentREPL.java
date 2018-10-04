package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ConcurrentREPL {

	static String currentWorkingDirectory;
	private static int commandNumber = 1;
	private static final Map<Integer, String> indexToCommand = new HashMap<>();
	private static final Map<Integer, LinkedList<Thread>> indexToThreadList = new HashMap<>(); 
	
	public static void main(String[] args){  //everything can be done in this method
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
			} else if(command.equals("repl_jobs")) {
				replJobs();
			} else if(command.contains("kill")) {
				int i = command.indexOf(' ');
				String word = command.substring(0, i);
				int temp = Integer.parseInt(word.substring(i));
				kill(temp);
			} else if(!command.trim().equals("")) {
				indexToCommand.put(commandNumber, command);
				processCommand(command);
			}
		}
		s.close();
		System.out.print(Message.GOODBYE);
	}
	
	public static void processCommand(String command){
		if(command.contains("&")) {
			System.out.print(Message.NEWCOMMAND);
			command = s.nextLine();
		}
		//building the filters list from the command
		ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
		LinkedList<Thread> curThreads = new LinkedList<Thread>();
		

		//fills thread linked list
		while(filterlist != null) {
			Thread newThread = new Thread(filterlist);
			newThread.start();
			curThreads.add(newThread);
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		//fill index to threadlist hashmap
		indexToThreadList.put(commandNumber,curThreads);
		commandNumber++;
		
		//for terminating threads
		for(Thread thread : curThreads){
			try {
				thread.join();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cleanMap(curThreads);
		
			
	}
	
	
	
	public static void cleanMap(LinkedList<Thread> threadsList) {
		Set set = indexToThreadList.entrySet();
	    Iterator iterator = set.iterator();
	    int index = 1;
	    while(iterator.hasNext()) {
	    		Map.Entry mentry = (Map.Entry)iterator.next();
	    		//can I compare using == ??
	    		if(mentry.getValue() == threadsList) {
	    			indexToThreadList.remove(index);
	    		}
	    		index++;
	    }
	}
	
	
	public static void replJobs(){
		Set set = indexToCommand.entrySet();
	    Iterator iterator = set.iterator();
	    while(iterator.hasNext()) {
	    		Map.Entry mentry = (Map.Entry)iterator.next();
	        System.out.print(mentry.getKey() + ". ");
	        System.out.println(mentry.getValue());
	    }
		
	}
	
	public static void kill(int commandNumber){
		indexToThreadList.remove(commandNumber);
	}
	

}
