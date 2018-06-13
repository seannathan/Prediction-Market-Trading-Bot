package bots; // TODO: change this to your package

import brown.agent.AbsPredictionMarketAgent;
import brown.agent.library.RandomAgent;
import brown.agent.library.UpdateAgent;
import brown.channels.library.CallMarketChannel;
import brown.exceptions.AgentCreationException;

public class TestBot1 extends AbsPredictionMarketAgent {

	private int observedValue;
	private int decoys;
	private double fairValue;
	
	public TestBot1(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
	}

	@Override
	public void onMarketStart() {
		observedValue = getCoin() ? 1 : 0;
		decoys = getNumDecoys();

		double pGivenHeads = (decoys + 2.0) / ((2 * decoys) + 2);
		fairValue = getCoin() ? 100 * pGivenHeads : 100 * (1 - pGivenHeads);

		System.out.println("Fair value: " + fairValue);
		System.out.println("Decoys: " + decoys);
	}

	@Override
	public void onMarketRequest(CallMarketChannel channel) {
		// TODO decide if you want to bid/offer or not
		buy(fairValue - 5, 10, channel);
		sell(fairValue + 5, 10, channel);	
	}

	@Override
	public void onTransaction(int quantity, double price) {
		// TODO anything your bot should do after a trade it's involved
		// in is completed
	}

	@Override
	public double getHighestBuy() {
		// TODO upper bound you would buy at
		return fairValue - 5;
	}

	@Override
	public double getLowestSell() {
		return fairValue + 5;
	}

	public static void main(String[] args) throws AgentCreationException {
		new TestBot1("localhost", 2121, "dummy bot");
		for (int i = 0; i < 5; i++) new RandomAgent("localhost", 2121, "rand" + i);
		
		while (true) {
		}
	}
}