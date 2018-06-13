package bots; // TODO: change this to your package

import brown.agent.AbsPredictionMarketAgent;
import brown.agent.library.RandomAgent;
import brown.agent.library.UpdateAgent;
import brown.channels.library.CallMarketChannel;
import brown.exceptions.AgentCreationException;

public class TestBot2 extends AbsPredictionMarketAgent {

	private int observedValue;
	private int decoys;
	private double fairValue;
	
	private boolean justBought;
	private double lastPrice;
	
	public TestBot2(String host, int port, String name) throws AgentCreationException {
		super(host, port, name);
	}

	@Override
	public void onMarketStart() {
		justBought = false;
		observedValue = getCoin() ? 1 : 0;
		decoys = getNumDecoys();

		double pGivenHeads = (decoys + 2.0) / ((2 * decoys) + 2);
		fairValue = getCoin() ? 100 * pGivenHeads : 100 * (1 - pGivenHeads);
		lastPrice = fairValue;
		
		System.out.println("Fair value: " + fairValue);
		System.out.println("Decoys: " + decoys);
	}

	@Override
	public void onMarketRequest(CallMarketChannel channel) {
		double highestBuy = getOrderBook().getBuys().peek().price;
		double lowestSell = getOrderBook().getSells().peek().price;
		
		if (justBought) {
			if (highestBuy < lastPrice) {
				sell(highestBuy, 10, channel);
				lastPrice = highestBuy;
				justBought = !justBought;
			}
		} else {
			if (lowestSell > lastPrice) {
				sell(lowestSell, 10, channel);
				lastPrice = lowestSell;
				justBought = !justBought;
			}
		}
	}

	@Override
	public void onTransaction(int quantity, double price) {
		// TODO anything your bot should do after a trade it's involved
		// in is completed
		//justBought = !justBought;
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
		new TestBot2("localhost", 2121, "dummy bot");
		new RandomAgent("localhost", 2121, "rand1");
		new RandomAgent("localhost", 2121, "rand2");
		new RandomAgent("localhost", 2121, "rand3");
		new RandomAgent("localhost", 2121, "rand4");
		new RandomAgent("localhost", 2121, "rand5");
		while (true) {
		}
	}
}