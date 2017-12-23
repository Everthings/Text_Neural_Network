package andy.neural_network;

import org.ejml.simple.SimpleMatrix;

public class Test {
	public static void main(String args[]){
		int[] dimensions = {784, 30, 10};
		
		Network net = new Network(dimensions);
		
		final NetworkDisplay display = new NetworkDisplay(dimensions);
		display.setWeights(net.getWeights());
		display.setBiases(net.getBiases());
		
		net.addListener(new FeedforwardListener(){

			@Override
			public void fire(SimpleMatrix[] activations) {
				display.setActivations(activations);
			}
			
		});
		
		net.SGD(30, 10, 3.0);
	}
}
