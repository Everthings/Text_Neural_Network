package andy.neural_network;

import java.util.ArrayList;
import java.util.Random;


//import org.apache.commons.math3.linear.BlockFieldMatrix;

import org.ejml.simple.SimpleMatrix;

import andy.utilities.MnistReader;

public class Network {
	
	private int[] dimensions;
	private int numLayers;
	private SimpleMatrix[] weights;
	private SimpleMatrix[] biases;
	private Random random = new Random();
	private Data[] testData;
	private Data[] trainData;
	private ArrayList<FeedforwardListener> listenerList;
	
	public Network(int[] dimensions){
		
		this.dimensions = dimensions;
		numLayers = dimensions.length;
		
		listenerList = new ArrayList<FeedforwardListener>();
		
		double[][][] tempBiases = new double[numLayers - 1][][];
		for(int i = 1; i < numLayers; i++){
			tempBiases[i-1] = new double[1][dimensions[i]];
			for(int j = 0; j < dimensions[i]; j++){
				tempBiases[i-1][0][j] = random.nextGaussian();
			}
		}
		
		biases = new SimpleMatrix[numLayers - 1];
		for(int i = 0; i < biases.length; i++){
			biases[i] = new SimpleMatrix(tempBiases[i]);
		}
		
		double[][][] tempWeights = new double[numLayers - 1][][];
		for(int i = 1; i < numLayers; i++){
			tempWeights[i-1] = new double[dimensions[i]][dimensions[i - 1]];
			for(int a = 0; a < dimensions[i]; a++){
				for(int b = 0; b < dimensions[i - 1]; b++){
					tempWeights[i-1][a][b] = random.nextGaussian();
				}
			}
		}
		
		weights = new SimpleMatrix[numLayers - 1];
		for(int i = 0; i < weights.length; i++){
			weights[i] = new SimpleMatrix(tempWeights[i]);
		}
		
		/*=
		for(int i = 0; i < weights.length; i++){
			System.out.println("Biases: " + biases[i]);
			System.out.println("Weights: " + weights[i]);
		}
		*/
		
		//INIT DATA
		int[] testLabels = MnistReader.getLabels("/Users/XuMan/Documents/Language_Neural_Network/data/t10k-labels-idx1-ubyte");
		ArrayList<double[][]> testImages = (ArrayList<double[][]>) MnistReader.getImages("/Users/XuMan/Documents/Language_Neural_Network/data/t10k-images-idx3-ubyte");
		
		testData = new Data[testLabels.length];
		
		for(int i = 0; i < testLabels.length; i++){
			testData[i] = new Data(new SimpleMatrix(packIntoFirstRow(testImages.get(i))), testLabels[i]);
		}
		
		int[] trainLabels = MnistReader.getLabels("/Users/XuMan/Documents/Language_Neural_Network/data/train-labels-idx1-ubyte");
		ArrayList<double[][]> trainImages = (ArrayList<double[][]>) MnistReader.getImages("/Users/XuMan/Documents/Language_Neural_Network/data/train-images-idx3-ubyte");
		
		trainData = new Data[trainLabels.length];
		for(int i = 0; i < trainLabels.length; i++){
			trainData[i] = new Data(new SimpleMatrix(packIntoFirstRow(trainImages.get(i))), trainLabels[i]);
		}
	}
	
	public void addListener(FeedforwardListener listener){
		listenerList.add(listener);
	}
	
	public void fireListener(SimpleMatrix[] activations){
		for(FeedforwardListener l: listenerList){
			l.fire(activations);
		}
	}
	
	public SimpleMatrix[] getWeights(){
		return weights;
	}
	
	public SimpleMatrix[] getBiases(){
		return biases;
	}
	
	public Data[] getTestingData(){
		
		shuffleData(testData);
		
		Data[] ret = new Data[10000];
		for(int i = 0; i < 10000; i++){
			ret[i] = testData[i];
		}
		
		return ret;
	}
	
	public Data[] getTrainingData(){
		//TODO: add implementation
		
		return trainData;
	}
	
	public double[][] packIntoFirstRow(double[][] array){// must have rectangular shape
		double[][] ret = new double[1][array.length * array[0].length];
		
		for(int r = 0; r < array.length; r++){
			for(int c = 0; c < array[0].length; c++){
				ret[0][r * c + c] = array[r][c];
			}
		}
		
		return ret;
	}
	
	public SimpleMatrix convertNumberintoMatrix(int n){
		SimpleMatrix vector = new SimpleMatrix(1, 10);
		vector.zero();
		
		vector.set(n, 1);
		
		return vector;
	}
	
	public void SGD(int epochs, int batchSize, double learningRate){
		
		Data[] train = getTrainingData();
		
		for(int n = 0; n < epochs; n++){
			shuffleData(train);
			
			for(int i = 0; i < train.length; i += batchSize){
				
				Data[] miniBatch =  new Data[batchSize];
				
				for(int index = 0; index < batchSize; index++){
					miniBatch[index] = train[i + index]; 
				}
				
				updateMiniBatch(miniBatch, learningRate);
			}
			
			Data[] test = getTestingData();
			System.out.println("Epoch " + (n + 1) + " complete: " + eval(test) + " out of " + test.length);
		}
	}
	
	public void updateMiniBatch(Data[] miniBatch, double learningRate){
		SimpleMatrix[] biasGradients = zerosMatrix(biases);
		SimpleMatrix[] weightGradients = zerosMatrix(weights);
		
		for(int i = 0; i < miniBatch.length; i++){
			SimpleMatrix[][] gradients = backpropagation(miniBatch[i]);
			for(int a = 0; a < biasGradients.length; a++){
				biasGradients[a] = biasGradients[a].plus(gradients[a][0]); 
				weightGradients[a] = weightGradients[a].plus(gradients[a][1]);
			}
		}
		
		/*
		for(int a = 0; a < biasGradients.length; a++){
			System.out.println("Bias_D: " + biasGradients[a]);
			System.out.println("Weights_D: " + weightGradients[a]);
		}
		*/
		
		for(int a = 0; a < biases.length; a++){
			biases[a] = biases[a].plus(-learningRate/miniBatch.length, biasGradients[a]); //really minus, but ejml does have minus function with scale functionality
			weights[a] = weights[a].plus(-learningRate/miniBatch.length, weightGradients[a]); //really minus, but ejml does have minus function with scale functionality
		}
		
		/*
		for(int i = 0; i < biases.length; i++)
			System.out.println("Bias: " + biases[i]);
		
		for(int i = 0; i < weights.length; i++)
			System.out.println("Weight: " + weights[i]);
		*/
	}
	
	public SimpleMatrix[][] backpropagation(Data d){
		//TODO: add implementation
		SimpleMatrix[][] gradients = new SimpleMatrix[numLayers - 1][2];//[0] is the bias gradient, [1] is the weight gradient
		
		//SimpleMatrix activation = matrixSigmoid(new SimpleMatrix(d.getImage()));//idk if right
		SimpleMatrix activation = new SimpleMatrix(d.getImage());
		SimpleMatrix[] activations = new SimpleMatrix[numLayers];
		activations[0] = activation.copy();
		SimpleMatrix[] zs = new SimpleMatrix[numLayers - 1];
		
		for(int a = 0; a < weights.length; a++){
			//SimpleMatrix z = weights[a].mult(activation);
			SimpleMatrix waDotProduct = new SimpleMatrix(1, weights[a].numRows());
			for(int i = 0; i < weights[a].numRows(); i++){
				waDotProduct.set(i, getRow(i, weights[a]).dot(activation));
			}
			/*
			if(a != 0)
				System.out.println("waDotProduct: " + waDotProduct);
			*/
			
			SimpleMatrix z = waDotProduct.plus(biases[a]);
			
			zs[a] = z.copy();
			
			
		//	System.out.println("BEFORE: \n"
		//			+ "Bias_D: " + z);
			
			activation = matrixSigmoid(z);
			
			
			//System.out.println("AFTER: \n"
			//		+ "Bias_D: " + activation);
		
		
			activations[a + 1] = activation.copy();
		}
		
		SimpleMatrix error = costDerivative(activation, convertNumberintoMatrix(d.getLabel())).elementMult(sigmoidPrime(zs[zs.length - 1]));
		//SimpleMatrix error = costDerivativeScalar(activation, d.getLabel()).elementMult(sigmoidPrime(zs[zs.length - 1]));
		gradients[gradients.length - 1][0] = error.copy();
		
		/*
		System.out.println("Activation: " + activation);
		System.out.println();
		System.out.println("Label: " + convertNumberintoMatrix(d.getLabel()));
		System.out.println();
		System.out.println("Sigmoid_Prime: " + sigmoidPrime(zs[zs.length - 1]));
		System.out.println();
		System.out.println("Error: " + error);
		System.out.println("--------------------------------------------");
		*/
		
		SimpleMatrix activationVector = activations[activations.length - 2].copy();
		SimpleMatrix errorVector = error.copy();
		
		//TODO: check if this works(might need transpose)
		SimpleMatrix weightGradient = new SimpleMatrix(errorVector.numCols(), activationVector.numCols());
		for(int a = 0; a < errorVector.numCols(); a++){
			for(int b = 0; b < activationVector.numCols(); b++){
				weightGradient.set(a, b, errorVector.get(a) * activationVector.get(b));
			}
		}
		gradients[gradients.length - 1][1] = weightGradient.copy();
		
		for(int index = 2; index < numLayers; index++){
			SimpleMatrix z = zs[zs.length - index];
			SimpleMatrix sigPrime = sigmoidPrime(z);
			
			SimpleMatrix newError = new SimpleMatrix(1, weights[weights.length - index + 1].numCols());
		
			SimpleMatrix w = weights[weights.length - index + 1].transpose();//double check for correctness
			for(int a = 0; a < w.numRows(); a++){
				newError.set(a, getRow(a, w).dot(error));
			}
			error = newError.elementMult(sigPrime);
			gradients[gradients.length - index][0] = error.copy();// bias
	
			weightGradient = new SimpleMatrix(error.numCols(), activations[activations.length - index - 1].numCols());
			for(int a = 0; a < error.numCols(); a++){
				for(int b = 0; b < activations[activations.length - index - 1].numCols(); b++){
					weightGradient.set(a, b, error.get(a) * activations[activations.length - index - 1].get(b));
				}
			}
			gradients[gradients.length - index][1] = weightGradient.copy();// weights
			//System.out.println("r: " + weightGradient.numRows() + " c: " + weightGradient.numCols());
		}
		
		/*
		for(int i = 0; i < gradients.length; i++){
			System.out.println("Bias_D: " + gradients[i][0]);
			System.out.println("Weight_D: " + gradients[i][1]);
		}
		*/
		
		/*
		System.out.println("Weights_D: ");
		for(int i = 0; i < gradients.length; i++){
			for(int a = 0; a < gradients[i][1].getNumElements(); a++){
				//System.out.println(gradients[i][0].get(a));
				System.out.println(gradients[i][1].get(a));
			}
		}
		System.out.println("-------------------------");
		*/

		return gradients;
	}
	
	public SimpleMatrix getRow(int row, SimpleMatrix mat){
		SimpleMatrix ret = new SimpleMatrix(1, mat.numCols());
		
	//	System.out.println("matrix expected");
	//	for(int i = 0; i < mat.numCols(); i++){
	//		System.out.print(mat.get(row, i));
	//	}
	//	System.out.println();
	//	System.out.println("\nrow-" + row);
		
		for(int i = 0; i < mat.numCols(); i++){
			ret.set(i, mat.get(row * mat.numCols() + i));
		}
		
	//	System.out.println();
	//	System.out.println("matrix returned");
	//	ret.print();
	//	System.out.println();
	//	System.out.println("-------------------------------------------");
		
		return ret;
	}
	
	public int eval(Data[] testData){
		//TODO: add implementation
		
		int counter = 0;
		
		for(int i = 0; i < testData.length; i++){
			//System.out.println(feedForward(testData[i]));
			if(maxValueIndex(feedForward(testData[i])) == testData[i].getLabel())
				counter++;
		}
		
		return counter;
	}
	
	public int maxValueIndex(SimpleMatrix mat){
		double max = Integer.MIN_VALUE;
		int index = -1;
		
		for(int i = 0; i < mat.getNumElements(); i++){
			if(mat.get(i) > max){
				max = mat.get(i);
				index = i;
			}
		}
		
		return index;
	}
	
	public SimpleMatrix feedForward(Data d){
		
		SimpleMatrix[] activations = new SimpleMatrix[numLayers];
		
		SimpleMatrix a = new SimpleMatrix(d.getImage());
		activations[0] = a;
		
		for(int layer = 0; layer < numLayers - 1; layer++){
			SimpleMatrix waDotProduct = new SimpleMatrix(1, weights[layer].numRows());
			for(int i = 0; i < weights[layer].numRows(); i++){
				//System.out.println("Weights: " + getRow(i, weights[layer]));
				//System.out.println();
				//System.out.println("Activation: " + a);
				//System.out.println();
				waDotProduct.set(i, getRow(i, weights[layer]).dot(a));
				
				//System.out.println("Dot Product: " + waDotProduct);
				//System.out.println("------------------------------");
				
			}
			
			SimpleMatrix z = waDotProduct.plus(biases[layer]);
			a = matrixSigmoid(z);
			
			activations[layer + 1] = a;
		}
		
		fireListener(activations);
		
		return a;// returns output matrix of neural network
	}
	
	public SimpleMatrix[] zerosMatrix(SimpleMatrix[] mats){
		
		SimpleMatrix[] zeroMats = new SimpleMatrix[mats.length];
		
		for(int i = 0; i < mats.length; i++){
			zeroMats[i] = new SimpleMatrix(mats[i].numRows(), mats[i].numCols());
			zeroMats[i].zero();
		}
		
		return zeroMats;
	}
	
	public SimpleMatrix costDerivative(SimpleMatrix output, SimpleMatrix expectedOutput){
		return output.minus(expectedOutput);
	}
	
	public SimpleMatrix costDerivativeScalar(SimpleMatrix output, double expectedOutput){
		return output.minus(expectedOutput);
	}
	
	public void shuffleData(Data[] data){
		for(int i = data.length - 1; i > 0; i--){
			int random = (int)(Math.random() * (i + 1));
			
			Data temp = data[i];
			data[i] = data[random];
			data[random] = temp;
		}
	}
	
	public SimpleMatrix matrixSigmoid(SimpleMatrix mat){
		
		SimpleMatrix copy = mat.copy();
		
		for(int index = 0; index < mat.getNumElements(); index++){
			copy.set(index, sigmoid(mat.get(index)));
		}
		
		return copy;
	}
	
	public double sigmoid(double input){
		//System.out.println("Before: " + input);
		double ret = (double)1/(double)(1 + Math.pow(Math.E, -input));
		//System.out.println("After: " + ret);
		return ret;
	}
	
	public SimpleMatrix sigmoidPrime(SimpleMatrix input){
		
		SimpleMatrix copy = input.copy();
		
		for(int index = 0; index < input.getNumElements(); index++){
			copy.set(index, sigmoid(input.get(index))*(1-sigmoid(input.get(index))));
		}
		
		return copy;
	}
}
