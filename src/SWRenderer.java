import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SWRenderer extends JFrame
{
	public static void main(String[] args)
	{
		new SWRenderer();
	}
	
	
	SWRenderer()
	{
		super();
		setSize(500,500);
		setResizable(true);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().add(new JPanel()
		{
			@Override
			public void paint(Graphics _g)
			{
				super.paint(_g);
				
				int[] xPoints = {100,(int) (System.currentTimeMillis()%500),100};
				int[] yPoints = {100,100,200};
				
				_g.setColor(Color.RED);
				_g.fillPolygon(xPoints, yPoints, xPoints.length);
				
				_g.setColor(Color.BLACK);
				_g.drawPolygon(xPoints, yPoints, xPoints.length);
				
				repaint();
			}
		});
	}
}
