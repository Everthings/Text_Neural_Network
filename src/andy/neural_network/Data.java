package andy.neural_network;

import org.ejml.simple.SimpleMatrix;

public class Data {
	private SimpleMatrix image;
	private int label;
	
	public Data(SimpleMatrix image, int label){
		this.image = image;
		this.label = label;
	}
	
	public SimpleMatrix getImage(){
		return image;
	}
	
	public int getLabel(){
		return label;
	}
}
