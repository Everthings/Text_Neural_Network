package andy.neural_network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.ejml.simple.SimpleMatrix;

public class NetworkDisplay extends JPanel implements MouseListener, MouseMotionListener{
	JFrame frame;
	
	final int WIDTH = 1200;
	final int HEIGHT = 800;
	
	double circle_diameter = 20;
	double x_gap = 500;
	double y_gap = 100;
	
	SimpleMatrix[] weights;
	SimpleMatrix[] biases;
	SimpleMatrix[] activations;
	int[] dimensions;
	
	double view_x = 0;
	double view_y = 0;
	
	List<Integer> view_movements;
	int LEFT = 0;
	int RIGHT = 1;
	int UP = 2;
	int DOWN = 3;
	int ZOOM = 4;
	int UNZOOM = 5;
	
	int mx = 0;
	int my = 0;
	
	List<List<Integer>> selected_neurons;
	
	@Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

	
	public NetworkDisplay(int[] d){

		this.dimensions = d;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(WIDTH, HEIGHT);
		this.setVisible(true);
		frame.setContentPane(this);
		frame.pack();
		//frame.revalidate();
		
		
		view_movements = Collections.synchronizedList(new ArrayList<Integer>());
		selected_neurons = Collections.synchronizedList(new ArrayList<List<Integer>>(dimensions.length));
		for(int i = 0; i < dimensions.length; i++){
			selected_neurons.add(new ArrayList<Integer>());
		}
		
		setupKeyBindings();
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				
				for(int index = view_movements.size() - 1; index >= 0; index--){
					Integer i = view_movements.get(index);
					
					if(i.equals(LEFT)){
						view_x -= 3;
					}else if(i.equals(RIGHT)){
						view_x += 3;
					}else if(i.equals(UP)){
						view_y -= 3;
					}else if(i.equals(DOWN)){
						view_y += 3;
					}else if(i.equals(ZOOM)){
						y_gap *= 1.01;
						x_gap *= 1.01;
						circle_diameter *= 1.01;
						view_x *= 1.01;
						view_y *= 1.01;
					}else if(i.equals(UNZOOM)){
						y_gap  /= 1.01;
						x_gap  /= 1.01;
						circle_diameter /= 1.01;
						view_x /= 1.01;
						view_y /= 1.01;
					}
				}
				
				if(view_movements.size() != 0){
					repaint();
				}
			}
			
		}, 0, 10);
		
		t.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				repaint();
			}
			
		}, 0, 100);
	}
	
	public void setupKeyBindings(){
		
		InputMap inMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
		KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
		KeyStroke upKey = KeyStroke.getKeyStroke("UP");
		KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
		KeyStroke zKey = KeyStroke.getKeyStroke("Z");
		KeyStroke xKey = KeyStroke.getKeyStroke("X");
		KeyStroke cKey = KeyStroke.getKeyStroke("C");
		KeyStroke leftKey_released = KeyStroke.getKeyStroke("released LEFT");
		KeyStroke rightKey_released = KeyStroke.getKeyStroke("released RIGHT");
		KeyStroke upKey_released = KeyStroke.getKeyStroke("released UP");
		KeyStroke downKey_released = KeyStroke.getKeyStroke("released DOWN");
		KeyStroke zKey_released = KeyStroke.getKeyStroke("released Z");
		KeyStroke xKey_released = KeyStroke.getKeyStroke("released X");
			
		inMap.put(leftKey, "left");
		inMap.put(rightKey, "right");
		inMap.put(upKey, "up");
		inMap.put(downKey, "down");
		inMap.put(zKey, "zoom");
		inMap.put(xKey, "un-zoom");
		inMap.put(leftKey_released, "left_released");
		inMap.put(rightKey_released, "right_released");
		inMap.put(upKey_released, "up_released");
		inMap.put(downKey_released, "down_released");
		inMap.put(zKey_released, "zoom_stop");
		inMap.put(xKey_released, "unzoom_stop");
		inMap.put(cKey, "clear_selected_neurons");
		
		ActionMap actMap = this.getActionMap();
		
		actMap.put("left", new ViewMoveAction(LEFT));
		actMap.put("right", new ViewMoveAction(RIGHT));
		actMap.put("up", new ViewMoveAction(UP));
		actMap.put("zoom", new ViewMoveAction(ZOOM));
		actMap.put("un-zoom", new ViewMoveAction(UNZOOM));
		actMap.put("down", new ViewMoveAction(DOWN));
		actMap.put("left_released", new ViewStopAction(LEFT));
		actMap.put("right_released", new ViewStopAction(RIGHT));
		actMap.put("up_released", new ViewStopAction(UP));
		actMap.put("down_released", new ViewStopAction(DOWN));
		actMap.put("zoom_stop", new ViewStopAction(ZOOM));
		actMap.put("unzoom_stop", new ViewStopAction(UNZOOM));
		actMap.put("clear_selected_neurons", new AbstractAction(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < selected_neurons.size(); i++){
					selected_neurons.get(i).clear();
				}
			}
			
		});
	}
	
	public void setWeights(SimpleMatrix[] weights){
		this.weights = weights;
	}
	
	public void setBiases(SimpleMatrix[] biases){
		this.biases = biases;
	}
	
	public void setActivations(SimpleMatrix[] activations){
		this.activations = activations;
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g){
		
		Graphics2D g2 = (Graphics2D) g;
		double scaleFactorX = (double)frame.getContentPane().getWidth()/WIDTH;
		double scaleFactorY = (double)frame.getContentPane().getHeight()/HEIGHT;
		//to keep the nodes circles, the size of the nodes scales with the height of the screen instead of the width
		
		for(int i = 0; i < dimensions.length; i++){
			for(int j = (int)Math.max((view_y / y_gap + dimensions[i]/2 - HEIGHT / (2 * y_gap) - 2), 0); j < (view_y / y_gap + dimensions[i]/2 + HEIGHT / (2 * y_gap) + 2) && j < dimensions[i]; j++){
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(1));
				g.drawOval((int)((x_gap * i - x_gap + WIDTH/2 - view_x) * scaleFactorX - (circle_diameter * scaleFactorY)/2 ), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - (circle_diameter)/2 - view_y)  * scaleFactorY), (int)(circle_diameter * scaleFactorY), (int)(circle_diameter * scaleFactorY));
				
				if(Math.pow(mx - ((x_gap * i - x_gap + WIDTH/2 - view_x) * scaleFactorX), 2) + Math.pow(my - (((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - view_y) * scaleFactorY), 2) < Math.pow((circle_diameter / 2) * scaleFactorY, 2)){
					g2.setColor(Color.MAGENTA);
					g.fillOval((int)((x_gap * i - x_gap + WIDTH/2 - view_x + circle_diameter * scaleFactorY * 0.05) * frame.getContentPane().getWidth()/WIDTH - (circle_diameter * scaleFactorY)/2), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - circle_diameter/2 - view_y + circle_diameter * scaleFactorY * 0.05) * scaleFactorY), (int)(circle_diameter * 0.9 * scaleFactorY), (int)(circle_diameter * 0.9 * scaleFactorY));
				}
				
				if(activations != null){
					Font font = new Font("Courier", Font.PLAIN, 10);
					FontMetrics metrics = g.getFontMetrics(font);
					String text = "" + (int)(activations[i].get(j) * 10000)/(double)10000;
				
					g.setColor(Color.RED);
					g.drawString(text, (int)((x_gap * i - x_gap + WIDTH/2 - view_x - metrics.stringWidth(text)/2) * scaleFactorX), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - circle_diameter/2 - view_y - 3 * metrics.getHeight()/2) * scaleFactorY));
				}
				
				if(biases != null && i != 0){
					Font font = new Font("Courier", Font.PLAIN, 10);
					FontMetrics metrics = g.getFontMetrics(font);
					String text = "" + (int)(biases[i - 1].get(j) * 1000)/(double)1000;
					g.setColor(Color.BLUE);
					g.drawString(text, (int)((x_gap * i - x_gap + WIDTH/2 - view_x - metrics.stringWidth(text)/2) * scaleFactorX), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - circle_diameter/2 - view_y - metrics.getHeight()/2) * scaleFactorY));
				}
				
				//if no special neuron are selected to observe; then draw all lines
				if(i < dimensions.length - 1){
					if(getNumContents(selected_neurons) == 0){
						g.setColor(Color.BLACK);
						for(int k = (int)Math.max((view_y / y_gap + dimensions[i + 1]/2 - HEIGHT / (2 * y_gap) - 2), 0); k < (view_y / y_gap + dimensions[i + 1]/2 + HEIGHT / (2 * y_gap) + 2) && k < dimensions[i + 1]; k++){
							double weight = weights[i].get(weights[i].getIndex(k, j));
							if(weight >= 0)
								g2.setColor(new Color(0, (int)Math.max(Math.min(weight * 100, 255), 100), 0));
							else
								g2.setColor(new Color((int)Math.max(Math.min(-weight * 100, 255), 100), 0, 0));
							g2.setStroke(new BasicStroke((int)Math.abs(weight)));
							g.drawLine((int)((x_gap * i - x_gap + WIDTH/2 - view_x) * scaleFactorX + (circle_diameter * scaleFactorY)/2), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - view_y) * scaleFactorY), (int)((x_gap * (i + 1) - x_gap + WIDTH/2 - view_x) * scaleFactorX - (circle_diameter * scaleFactorY)/2), (int)(((-dimensions[i + 1]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * k - view_y) * scaleFactorY));
						}
					}else{
						for(int a = 0; a < selected_neurons.get(i).size(); a++){
							for(int b = 0; b < selected_neurons.get(i + 1).size(); b++){
								double weight = weights[i].get(weights[i].getIndex(selected_neurons.get(i + 1).get(b), selected_neurons.get(i).get(a)));
								if(weight >= 0)
									g2.setColor(new Color(0, (int)Math.max(Math.min(weight * 100, 255), 100), 0));
								else
									g2.setColor(new Color((int)Math.max(Math.min(-weight * 100, 255), 100), 0, 0));
								g2.setStroke(new BasicStroke((int)Math.abs(weight)));
								g.drawLine((int)((x_gap * i - x_gap + WIDTH/2 - view_x) * scaleFactorX + (circle_diameter * scaleFactorY)/2), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * selected_neurons.get(i).get(a) - view_y) * scaleFactorY), (int)((x_gap * (i + 1) - x_gap + WIDTH/2 - view_x) * scaleFactorX - (circle_diameter * scaleFactorY)/2 ), (int)(((-dimensions[i + 1]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * selected_neurons.get(i + 1).get(b) - view_y) * scaleFactorY));
								g2.setColor(Color.BLACK);
								
								Font font = new Font("Courier", Font.PLAIN, 10);
								FontMetrics metrics = g.getFontMetrics(font);
								String text = "" + weight;
								g.drawString(text, (int)((x_gap * ((double)i + 0.5) - x_gap + WIDTH/2 - view_x) * scaleFactorX + (circle_diameter * scaleFactorY)/2 - metrics.stringWidth(text)*2/3), 
										(int)((((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * selected_neurons.get(i).get(a) - 2 * view_y + (-dimensions[i + 1]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * selected_neurons.get(i + 1).get(b))/2) * scaleFactorY + metrics.getHeight()/2));
							}
						}
					}
				}
			}
			
			//display special selected neurons
			g2.setColor(Color.MAGENTA);
			for(int a = 0; a < selected_neurons.get(i).size(); a++){
				g.fillOval((int)((x_gap * i - x_gap + WIDTH/2 - view_x + circle_diameter * scaleFactorY * 0.05) * frame.getContentPane().getWidth()/WIDTH - (circle_diameter * scaleFactorY)/2), (int)(((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * selected_neurons.get(i).get(a) - circle_diameter/2 - view_y + circle_diameter * scaleFactorY * 0.05) * scaleFactorY), (int)(circle_diameter * 0.9 * scaleFactorY), (int)(circle_diameter * 0.9 * scaleFactorY));
			}
		}
	}
	
	public int getNumContents(List<List<Integer>> list){
		//return total number of elements in the array lists
		
		int num = 0;
		
		for(int i = 0; i < list.size(); i++){
			num += list.get(i).size();
		}
		
		return num;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		double scaleFactorX = (double)frame.getContentPane().getWidth()/WIDTH;
		double scaleFactorY = (double)frame.getContentPane().getHeight()/HEIGHT;
		
		for(int i = 0; i < dimensions.length; i++){
			for(int j = (int)Math.max(view_y / y_gap + dimensions[i]/2 - HEIGHT / (2 * y_gap) - 2, 0); j < view_y / y_gap + dimensions[i]/2 + HEIGHT / (2 * y_gap) + 2 && j < dimensions[i]; j++){
				if(Math.pow(mx - ((x_gap * i - x_gap + WIDTH/2 - (circle_diameter * scaleFactorY)/2 - view_x) * frame.getContentPane().getWidth()/WIDTH), 2) + Math.pow(my - (((-dimensions[i]/2 + HEIGHT / (2 * y_gap)) * y_gap + y_gap * j - (circle_diameter * scaleFactorY)/2 - view_y) * scaleFactorY), 2) < Math.pow(circle_diameter * scaleFactorY, 2)){
					
					if(!selected_neurons.get(i).contains(j))
						selected_neurons.get(i).add(j);
					else
						selected_neurons.get(i).remove(new Integer(j));
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
class ViewMoveAction extends AbstractAction{
		
		int move;
		
		ViewMoveAction(int MOVE_TYPE){
			this.move = MOVE_TYPE;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!view_movements.contains(move))
				view_movements.add(move);
		}
	}
	
	class ViewStopAction extends AbstractAction{

		int move;
		
		ViewStopAction(int MOVE_TYPE){
			this.move = MOVE_TYPE;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			view_movements.remove(new Integer(move));
		}
	}
}