/**
 * 
 */
package com.cs.sbm;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import com.cs.sbm.api.OrderBoard;
import com.cs.sbm.api.OrderBookSummary;
import com.cs.sbm.api.OrderType;
import com.cs.sbm.impl.OrderBoardImpl;

/**
 * A simple console based user interface for OrderBoard
 * Note that this provided just to interact with the main {@link OrderBoardImpl} class as such complicated usecase scenarios have not been considered.
 * @author Nat
 *
 */
public class OrderBoardUI {
	
	/** Command to register an order */
	static final String REGISTER="REGISTER";
	/** Command to cancel an order */
	static final String CANCEL="CANCEL";
	/** Command to get the current orderbook summary */
	static final String SUMMARY="SUMMARY";
	/** Command to exit this program */
	static final String EXIT="EXIT";
	/** static set of supported commands */
	static Set<String> COMMANDS = new LinkedHashSet<>();
	static{
		COMMANDS.add(REGISTER);
		COMMANDS.add(CANCEL);
		COMMANDS.add(SUMMARY);
		COMMANDS.add(EXIT);
	}
	/**
	 * Main method to start this program.
	 * @param args
	 */
	public static void main(String[] args) {
		//initialise the orderboard.
		OrderBoard orderboard = new OrderBoardImpl();
		printUsage();
		Scanner console = new Scanner(System.in);
		while(true){
			System.out.println("ENTER A COMMAND:");
			//read input from user. This will block until user enters a text.
			String line = console.nextLine();
			//parse the input.
			String[] parts = line.split(" ");
			if(parts.length ==0){
				printUsage();
				continue;
			}
			if(!COMMANDS.contains(parts[0].toUpperCase())){
				System.out.println("Invalid command "+parts[0]);
				printUsage();
			}
			if(EXIT.equalsIgnoreCase(parts[0])){
				System.out.println("Exiting...");
				console.close();
				System.exit(0);
			}
			if(CANCEL.equalsIgnoreCase(parts[0])){
				if(parts.length !=2){
					System.out.println("Invalid data for CANCEL command. Usage: CANCEL <ORDERID>");
					continue;
				}
				try{
					int orderId = Integer.parseInt(parts[1]);
					boolean success = orderboard.cancelOrder(orderId);
					if(success){
						System.out.println("Order "+orderId+" cancelled successfully.");
					}else{
						System.out.println("Failed to cancel order "+orderId);
					}
					printSummary(orderboard.getSummary());
				}catch(NumberFormatException ex){
					System.out.println(parts[1]+" is not a valid orderId.");
				}
			}
			if(REGISTER.equalsIgnoreCase(parts[0])){
				if(parts.length != 5){
					//registerOrder(String userName, OrderType orderType, int orderPrice, BigDecimal quantity);
					System.out.println("Invalid data for REGISER command. Usage: REGISTER  <USERNAME> <BUY|SELL> <PRICE> <QUANTITY>");
					continue;
				}
				/*
				 * We need to perform validation on the incoming data, but for simplicity its been omitted.
				 */
				String username = parts[1];
				OrderType type = OrderType.valueOf(parts[2].toUpperCase());
				int price = Integer.parseInt(parts[3]);
				BigDecimal qty = new BigDecimal(parts[4]);
				int orderId = orderboard.registerOrder(username, type, price, qty);
				System.out.println("Order registered successfully with order id "+orderId);
				printSummary(orderboard.getSummary());
			}
			if(SUMMARY.equalsIgnoreCase(parts[0])){
				printSummary(orderboard.getSummary());
			}
		}
	}
	
	
	private static void printUsage(){
		StringBuilder sb = new StringBuilder();
		sb.append("Enter a supported command. Supported commands are "+COMMANDS).append("\n");
		sb.append("Sample usages:").append("\n");
		sb.append("REGISTER Nat BUY 136 1.5").append("\n");
		sb.append("CANCEL 27").append("\n");
		sb.append("SUMMARY").append("\n");
		sb.append("EXIT").append("\n");
		System.out.println(sb);
		
	}
	
	private static void printSummary(OrderBookSummary summary){
		System.out.println("=====BUYS==========");
		summary.getBuys().forEach(entry -> {
			System.out.println(entry.getCumulativeQuantity()+" for "+entry.getPrice());
		});
		System.out.println("===================");
		
		System.out.println("=====SELLS==========");
		summary.getSells().forEach(entry -> {
			System.out.println(entry.getCumulativeQuantity()+" for "+entry.getPrice());
		});
		System.out.println("===================");
	}
	
}
