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
public class TestBot5 extends AbsPredictionMarketAgent {

	private double buyValue;
	private int stage; // stage 0 is buying, stage 1 is selling, stage 2 is doing nothing.
	private double sellValue;
	private int stepSize;
	private boolean boughtFirst;
	
	private double finalBuyValue;
	private double finalSellValue;
	
		
	public TestBot5(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
	}

	@Override
	public void onMarketStart() {
		buyValue = 20;
		stage = 0;
		sellValue = 80;
		stepSize = 5;
		finalBuyValue = 20;
		finalSellValue = 80;
	}
	

	@Override
	public void onMarketRequest(CallMarketChannel channel) {
		if (stage == 0) {
			cancel(buyValue, true, channel);
			buy(buyValue + stepSize, 1, channel);
			//System.out.println("buyValue: " + buyValue);
			buyValue += stepSize;

			cancel(sellValue, false, channel);
			sell(sellValue - stepSize, 1, channel);
			sellValue -= stepSize;		
		
			if (buyValue > sellValue) {
				double temp = buyValue;
				buyValue = sellValue;
				sellValue = temp;
			}
		} else if (stage == 1) {
			//System.out.println("sellValue: " + sellValue);
			if (boughtFirst) {
				cancel(sellValue, false, channel);
				sell(finalSellValue, 1, channel);
				stage = stage + 1; // Ensures that we only post one sell.
			} else {
				cancel(buyValue, true, channel);
				buy(finalBuyValue, 1, channel);
				stage = stage + 1; // Ensures that we only post one sell.
			}
		}
	}

	@Override
	public void onTransaction(int quantity, double price) {		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Transaction made: price is " + price);
		if (stage == 0) {
			if (price < 0) {
				boughtFirst = true;
				finalSellValue = (-price) + 1;
			} else {
				boughtFirst = false;
				finalBuyValue = price - 1;
			}
		}
		stage = stage + 1;
	}

	@Override
	public double getHighestBuy() {
		return buyValue;
	}

	@Override
	public double getLowestSell() {
		return sellValue;
	}

	public static void main(String[] args) throws AgentCreationException {
		new TestBot5("localhost", 2121, "bot4");
		for (int i = 0; i < 5; i++) new UpdateAgent("localhost", 2121, "rand" + i);

		while (true) {
		}
	}
}