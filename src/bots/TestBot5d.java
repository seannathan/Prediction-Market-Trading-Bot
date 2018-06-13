package bots; // TODO: change this to your package

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brown.agent.AbsPredictionMarketAgent;
import brown.agent.library.FixedAgent;
import brown.agent.library.RandomAgent;
import brown.agent.library.UpdateAgent;
import brown.channels.library.CallMarketChannel;
import brown.exceptions.AgentCreationException;

/*
 * Identical to TestBot5b except that it collects data about what happened in the game.
 */
public class TestBot5d extends AbsPredictionMarketAgent {

	private double buyValue;
	
	/*
	 * Stage 0: the bot tries to buy at buyValue and sell at sellValue, 
	 * given that it has not made a buy or sell yet. Slowly increases the buyValue and decreases 
	 * the sellValue until it makes a buy or sell. Once it gets one, it moves to stage 1.
	 * 
	 * Stage 1: If we bought in stage 0, we're now trying to sell. We set a sell at finalSellValue 
	 * which is a fixed amount above the value of our contract. Similar strategy if we sold in stage 0.
	 * 
	 * Stage 2: 
	 */
	
	private int stage; // stage 0 is buying, stage 1 is selling, stage 2 is doing nothing.
	private double sellValue;
	private int stepSize;
	private boolean boughtFirst;
	
	private double finalBuyValue;
	private double finalSellValue;
	private int finalStepSize;


	private List<Double> transactionHistory;
	private List<Double> profitHistory;
	private int numSuccessful;
	private int numUnsuccessful;
	private int totalRounds;
	private double totalMade;
	private double totalLost;
	
	/*
	 * The key is the multiple of 10 that the profits round to, i.e. if the profit for the 
	 * round was 25 then it's 2. The value is the value that the original posted price was.
	 */
	// private Map<Integer, Double> originalPostedValues; // Maybe do this later.
	
		
	public TestBot5d(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
		//originalPostedValues = new HashMap<>();

		numSuccessful = 0;
		numUnsuccessful = 0;
		totalRounds = 0;
		totalMade = 0;
		totalLost = 0;
		profitHistory = new ArrayList<>();
	}

	@Override
	public void onMarketStart() {
		buyValue = 20;
		stage = 0;
		sellValue = 80;
		stepSize = 5;
		finalStepSize = 2;
		finalBuyValue = 20;
		finalSellValue = 80;
		
		transactionHistory = new ArrayList<>();
		
		System.out.println("\n\n\nData: ");
		System.out.println(numSuccessful);
		System.out.println(numUnsuccessful);
		System.out.println(totalRounds);
		System.out.println(totalMade);
		System.out.println(totalLost);
		System.out.println(profitHistory);
		
		System.out.println("\n\n\n");


	}
	

	@Override
	public void onMarketRequest(CallMarketChannel channel) {		
		if (stage == 0) {
			// Cancels current buy, makes new buy.
			cancel(buyValue, true, channel);
			buy(buyValue + stepSize, 1, channel);
			buyValue += stepSize;

			// Cancels and makes new sell.
			cancel(sellValue, false, channel);
			sell(sellValue - stepSize, 1, channel);
			sellValue -= stepSize;		
		
			// A (kind of) hack that makes sure the buyValue never goes over the sellValue.
			if (buyValue > sellValue) {
				double temp = buyValue;
				buyValue = sellValue;
				sellValue = temp;
			}
		} else if (stage == 1) {
			if (boughtFirst) {
				// Sets initial final sell.
				cancel(sellValue, false, channel);
				sell(finalSellValue, 1, channel);
			} else {
				// Sets initial final buy.
				cancel(buyValue, true, channel);
				buy(finalBuyValue, 1, channel);
			}
		} else if (stage == 2) {
			if (boughtFirst) {
				// Cancels current final sell, then sets new sell.
				cancel(finalSellValue, false, channel);
				sell(finalSellValue - finalStepSize, 1, channel);
				finalSellValue -= finalStepSize;
			} else {
				// Cancels current final buy, then sets new buy.
				cancel(finalBuyValue, true, channel);
				buy(finalBuyValue + finalStepSize, 1, channel);
				finalBuyValue += finalStepSize;
			}
		}
		
		if (stage == 1) {
			stage = 2;
		}
	}

	@Override
	public void onTransaction(int quantity, double price) {		
		// Bookkeeping.
		if (!transactionHistory.isEmpty() && (Math.abs(price) > 0.0001)) {
			double profit = price + transactionHistory.get(transactionHistory.size() - 1);
			if (profit > 0) {
				numSuccessful += 1;
				totalMade += profit;
			} else {
				numUnsuccessful += 1;
				totalLost += profit;
			}
			totalRounds += 1;
			transactionHistory = new ArrayList<>(); // This should really just be an optional.
			profitHistory.add(profit);
			System.out.println("############################################################ Profit: " + profit);
		}
		transactionHistory.add(price);
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Transaction made: price is " + price);
		if (stage == 0) {
			if (price < 0) {
				boughtFirst = true;
				finalSellValue = (-price) + 10;
			} else {
				boughtFirst = false;
				finalBuyValue = price - 10;
			}
		}
		stage = stage + 1;
	}

	@Override
	public double getHighestBuy() {
		return (stage == 0) ? buyValue : finalBuyValue;
	}

	@Override
	public double getLowestSell() {
		return (stage == 0) ? sellValue : finalSellValue;
	}

	public static void main(String[] args) throws AgentCreationException {
		new TestBot5d("localhost", 2121, "bot4");
		for (int i = 0; i < 5; i++) new FixedAgent("localhost", 2121, "rand" + i);

		while (true) {
		}
	}
}