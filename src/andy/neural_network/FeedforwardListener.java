package andy.neural_network;

import org.ejml.simple.SimpleMatrix;

public interface FeedforwardListener {
	void fire(SimpleMatrix[] activations);
}
