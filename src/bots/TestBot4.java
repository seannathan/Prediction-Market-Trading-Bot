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
public class TestBot4 extends AbsPredictionMarketAgent {

	private double buyValue;
	private int stage; // stage 0 is buying, stage 1 is selling, stage 2 is doing nothing.
	private double sellValue;
		
	public TestBot4(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
	}

	@Override
	public void onMarketStart() {
		buyValue = 20;
		stage = 0;
		sellValue = 100;
	}
	

	@Override
	public void onMarketRequest(CallMarketChannel channel) {
		if (stage == 0) {
			cancel(buyValue, true, channel);
			buy(buyValue + 5, 1, channel);
			//System.out.println("buyValue: " + buyValue);
			buyValue += 5;
		} else if (stage == 1) {
			//System.out.println("sellValue: " + sellValue);
			sell(sellValue, 1, channel);
			stage = stage + 1; // Ensures that we only post one sell.
		}
	}

	@Override
	public void onTransaction(int quantity, double price) {		
		if (stage == 0) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Transaction made: price is " + price);
			sellValue = (-price) + 1;
		} else {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Sell made: price is        " + price);
		}
		stage = stage + 1; // For when we finally get someone to sell us a contract.
	}

	@Override
	public double getHighestBuy() {
		// TODO upper bound you would buy at
		return buyValue;
	}

	@Override
	public double getLowestSell() {
		return sellValue;
	}

	public static void main(String[] args) throws AgentCreationException {
		new TestBot4("localhost", 2121, "bot4");
		for (int i = 0; i < 5; i++) new RandomAgent("localhost", 2121, "rand" + i);

		while (true) {
		}
	}
}