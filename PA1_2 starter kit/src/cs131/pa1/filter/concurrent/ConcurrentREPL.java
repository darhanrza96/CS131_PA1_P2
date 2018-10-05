package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class ConcurrentREPL {

	static String currentWorkingDirectory;
	private static int commandNumber = 1;
	private static Map<Integer, String> indexToCommand = new HashMap<>();
	private static Map<Integer, LinkedList<Thread>> indexToThreadList = new HashMap<>();
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
			cleanMap(true);
			return true;
		} else if(command.equals("repl_jobs")) {
			replJobs();
		} else if(command.trim().startsWith("kill")) {
			String[] splitted = command.split("\\s");
			if (splitted.length < 2){
				System.out.print(Message.REQUIRES_PARAMETER.with_parameter(command));
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
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		
		//if has ampersand read command again
		if (ampersand){
			if (!curThreads.isEmpty()){
				//fill index to threadlist hashmap
				indexToThreadList.put(commandNumber,curThreads);
				indexToCommand.put(commandNumber, oldcommand);
				commandNumber++;
				readCommand();
			}
		} else {	
			//for terminating threads
			for(Thread thread : curThreads){
				try {
					thread.join();
	
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cleanMap(false);	
		}


	}



	public static void cleanMap(boolean all) {
//		System.out.println("check");
		Set<Entry<Integer, LinkedList<Thread>>> threadSet = indexToThreadList.entrySet();
		Iterator<Entry<Integer, LinkedList<Thread>>> iterator = threadSet.iterator();
		while(iterator.hasNext()) {
			Map.Entry<Integer, LinkedList<Thread>> mentry = (Map.Entry)iterator.next();
			//can I compare using == ??
			LinkedList<Thread> threadList = mentry.getValue();
			boolean interrupted = threadList.getLast().getState() == Thread.State.TERMINATED;
//			for (Thread thread : threadList){
//				System.out.println(thread.getState());
//			}
//			System.out.println("check" + mentry.getKey());
			//System.out.println(threadList.getLast().isInterrupted());

//			for (Thread thread : mentry.getValue()){
//				try {
//					thread.join();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			if (interrupted || all){
			Integer key = mentry.getKey();
//			System.out.println("remove" + key);
			indexToCommand.remove(key);
			iterator.remove();
			}
		}
		
//		indexToCommand.clear();
//		indexToThreadList.clear();
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
//		if (threadList == null){
//			replJobs();
//		}
//		return;
		for (Thread thread : threadList){
			thread.interrupt();
		}
		indexToCommand.remove(commandNumber);
	}


}
