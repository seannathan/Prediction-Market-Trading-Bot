package bots; // TODO: change this to your package

import brown.agent.AbsPredictionMarketAgent;
import brown.agent.library.FixedAgent;
import brown.agent.library.RandomAgent;
import brown.agent.library.UpdateAgent;
import brown.channels.library.CallMarketChannel;
import brown.exceptions.AgentCreationException;

/*
 * Starts fair value at 25 and tries to buy at fair value.
 * Slowly increases fair value until it buys a contract.
 * Sells at a 
 */
public class TestBot5b extends AbsPredictionMarketAgent {

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

	
		
	public TestBot5b(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
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
		new TestBot5b("localhost", 2121, "bot4");
		for (int i = 0; i < 5; i++) new FixedAgent("localhost", 2121, "rand" + i);

		while (true) {
		}
	}
}