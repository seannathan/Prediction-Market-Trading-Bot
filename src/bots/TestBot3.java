package bots; // TODO: change this to your package

import brown.agent.AbsPredictionMarketAgent;
import brown.agent.library.FixedAgent;
import brown.agent.library.RandomAgent;
import brown.agent.library.UpdateAgent;
import brown.channels.library.CallMarketChannel;
import brown.exceptions.AgentCreationException;

public class TestBot3 extends AbsPredictionMarketAgent {

	private int observedValue;
	private int decoys;
	private double fairValue;
	private double window;
	private double updateStep;
		
	public TestBot3(String host, int port, String name) throws AgentCreationException {
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
		
		window = decoys * 3;
		updateStep = decoys;
	}

	@Override
	public void onMarketRequest(CallMarketChannel channel) {
		
		double highestBuy = fairValue + window;
		int buyQuantity = 1;
		if (getOrderBook().getBuys().peek() != null) {
			highestBuy = getOrderBook().getBuys().peek().price;
			buyQuantity = getOrderBook().getBuys().peek().quantity;
		}
		
		double lowestSell = fairValue - window;
		int sellQuantity = 1;
		if (getOrderBook().getSells().peek() != null) {
			lowestSell = getOrderBook().getSells().peek().price;
			sellQuantity = getOrderBook().getSells().peek().quantity;
		}
		
		if (highestBuy >= fairValue + window) {
			sell(highestBuy, buyQuantity, channel);
		}
		
		if (lowestSell <= fairValue - window) {
			buy(lowestSell, sellQuantity, channel);
		}
	}

	@Override
	public void onTransaction(int quantity, double price) {
		// TODO anything your bot should do after a trade it's involved
		// in is completed
		//justBought = !justBought;
		if (quantity > fairValue) {
			fairValue += updateStep;
			if (fairValue > 100) {
				fairValue = 100;
			}
			System.out.println("Updated fair value to: " + fairValue);
		} else if (quantity < fairValue) {
			fairValue -= updateStep;
			if (fairValue < 0) {
				fairValue = 0;
			}
			System.out.println("Updated fair value to: " + fairValue);
		}
	}

	@Override
	public double getHighestBuy() {
		// TODO upper bound you would buy at
		return fairValue - window;
	}

	@Override
	public double getLowestSell() {
		return fairValue + window;
	}

	public static void main(String[] args) throws AgentCreationException {
		new TestBot3("localhost", 2121, "bot3");
		for (int i = 0; i < 5; i++) new FixedAgent("localhost", 2121, "rand" + i);

		while (true) {
		}
	}
}