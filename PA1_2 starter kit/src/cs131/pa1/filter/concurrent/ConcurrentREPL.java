package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Filter;
import java.util.Scanner;
import java.util.Set;

public class ConcurrentREPL {

	static String currentWorkingDirectory;
	private static int commandNumber = 1;
	private static Map<Integer, String> indexToCommand = new HashMap<>();
	private static Map<Integer, LinkedList<Thread>> indexToThreadList = new HashMap<>();
	private static Map<Integer, ConcurrentFilter> indexToFilter = new HashMap<>();
	private static Scanner s;
	//private static boolean toggle = true;

	public static void main(String[] args){  //everything can be done in this method
		currentWorkingDirectory = System.getProperty("user.dir");
		s = new Scanner(System.in);
		System.out.print(Message.WELCOME);
		boolean end = false;
		while(!end) {
			end = readCommand();
		}
		s.close();
		System.out.print(Message.GOODBYE);
	}

	public static boolean readCommand(){
		//if there is & then join the threads
		String command;

		//obtaining the command from the user
		System.out.print(Message.NEWCOMMAND);
		command = s.nextLine();
//		String[] commandArr = command.split("\\s");
		command = command.trim();

		if(command.equals("exit")) {
			return true;
		} else if(command.equals("repl_jobs")) {
			replJobs();
		} else if(command.trim().startsWith("kill")) {
			String[] splitted = command.split("\\s");
			if (splitted.length < 2){
				System.out.print(Message.REQUIRES_PARAMETER);
				return false;
			}
			try{
				int i = Integer.parseInt(splitted[1]);
				kill(i);
				//toggle= false;
			} catch (NumberFormatException e) {
				System.out.print(Message.INVALID_PARAMETER.with_parameter(command));
				return false;
			}
		} else if(!command.trim().equals("")) {
			processCommand(command);
		}
		return false;
	}



	public static void processCommand(String command){
		//building the filters list from the command
		boolean ampersand = command.charAt(command.length()-1) == '&';
		String oldcommand = command;
		if (ampersand){
			command = command.substring(0, command.length()-2);
		}
		ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
		LinkedList<Thread> curThreads = new LinkedList<Thread>();
		
		//fills thread linked list
		while(filterlist != null) {
			Thread newThread = new Thread(filterlist);
			newThread.start();
			curThreads.add(newThread);
			if (filterlist.getNext() == null){
				indexToFilter.put(commandNumber, filterlist);
			}
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		
		//fill index to threadlist hashmap
		indexToThreadList.put(commandNumber,curThreads);
		indexToCommand.put(commandNumber, oldcommand);
		
		int currIndex = commandNumber;
		commandNumber++;
		
		
		//if has ampersand read command again
		if (ampersand){
			readCommand();
			cleanMap(-1);	
		} else {
			cleanMap(currIndex);
		}
		
		//for terminating current threads
//		for(Thread thread : curThreads){
//			try {
//				thread.join();
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
	}



	public static void cleanMap(int toKill) {
		Set<Integer> keySet = indexToThreadList.keySet();
		Set<Integer> setDelete = new HashSet<Integer>();
		
		if(toKill != -1){
			setDelete.add(toKill);
		} else {
		
	    for (Integer key : keySet) {
			boolean clean = indexToFilter.get(key).isDone();
			if(clean){
				for (Thread thread : indexToThreadList.get(key)){
					try {
						thread.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				setDelete.add(key);
			}
		}
		}
	    for (Integer key : setDelete){
			indexToFilter.remove(key);
			indexToThreadList.remove(key);
			indexToCommand.remove(key);
	    }
		
		if (indexToThreadList.isEmpty()){
			commandNumber = 1;
		}
	
	}


	public static void replJobs(){
		Set set = indexToCommand.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			System.out.print("\t" + mentry.getKey() + ". ");
			System.out.println(mentry.getValue());
		}
	}

	public static void kill(int commandNumber){
		LinkedList<Thread> threadList = indexToThreadList.remove(commandNumber);
		if (threadList == null){
			replJobs();
		}
		for (Thread thread : threadList){
			thread.interrupt();
		}
		indexToCommand.remove(commandNumber);
	}


}
