package pt.linguateca.relations;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.organic.JGraphSelfOrganizingOrganicLayout;

public class RelationsGraphM2Drawer implements GraphModelListener{

	private static final int X_INCREMENT = 200;
	private static final int Y_INCREMENT = 100;
	private static final int NODE_WIDTH = 150;
	private static final int NODE_HEIGHT = 20;
	private static final int NODES_PER_LINE = 6;

	private static final int FONT_SIZE = 10;
	private static final Font NODE_FONT = new Font("qq", Font.PLAIN, FONT_SIZE);

	private static final Color IDENT_COLOR = Color.green;
	private static final Color INCLUI_COLOR = Color.red;
	private static final Color INCLUIDO_COLOR = Color.magenta;
	private static final Color OCORRE_COLOR = Color.blue;
	private static final Color SEDE_COLOR = Color.cyan;
	private static final Color NOT_BASIC_COLOR = Color.black;
	
	public static enum nodeLabel{
		CONTENT, ID, BOTH;
	}

	//private int _labelType;

	private RelationsGraphM2 _relGraph;
	private JGraph _jGraph;

	private HashMap<String, DefaultGraphCell> _cellMap;
	private HashMap<String, Color> _colorMap;

	public RelationsGraphM2Drawer(RelationsGraphM2 graph)
	{
		_relGraph = graph;		
		_cellMap = new HashMap<String, DefaultGraphCell>();

		//_labelType = label;

		GraphModel model = new DefaultGraphModel();
		GraphLayoutCache view = new GraphLayoutCache(model, new	DefaultCellViewFactory());

		_jGraph = new JGraph(model, view);
		_jGraph.getModel().addGraphModelListener(this);

		initColorMap();
	}

	private void initColorMap()
	{
		_colorMap = new HashMap<String, Color>();

		_colorMap.put(Relation.IDENTIDADE, IDENT_COLOR);
		_colorMap.put(Relation.INCLUI, INCLUI_COLOR);
		_colorMap.put(Relation.INCLUIDO, INCLUIDO_COLOR);
		_colorMap.put(Relation.OCORRE, OCORRE_COLOR);
		_colorMap.put(Relation.SEDE, SEDE_COLOR);
		_colorMap.put(Relation.OUTRA, NOT_BASIC_COLOR);
	}

	public void drawGraph(HashMap<String, String> map, int labelType)
	{
		LinkedList<RelationsList> nodes = new LinkedList<RelationsList>(_relGraph.getAllNodes());

		int x = 0, y = 0;
		DefaultGraphCell cell;
		DefaultPort port;
		String label;

		for(RelationsList node : nodes)
		{
			label = getLabel(node, map, labelType);

			//cell = new DefaultGraphCell(entity.getId());
			//cell = new DefaultGraphCell(entity.getId()+"\n"+entity.getEntity());
			cell = new DefaultGraphCell(label);
			drawNode(cell, x, y);

			port = new DefaultPort();
			cell.add(port);

			_jGraph.getGraphLayoutCache().insert(cell);
			_cellMap.put(node.getKey(), cell);

			x += X_INCREMENT;
			if((x / X_INCREMENT) == NODES_PER_LINE)
			{
				y += Y_INCREMENT;
				x = 0;
			}

		}

		for(RelationsList node : nodes)
		{
			for(Relation r : node)
			{		
				if(!_cellMap.containsKey(r.getA()))
				{
					System.out.println("No' nao encontrado: "+r.getA());
					//System.out.println(_cellMap);
					continue;
				}

				if(!_cellMap.containsKey(r.getB()))
				{
					System.out.println("No' nao encontrado: "+r.getB());
					//System.out.println(_cellMap);
					continue;
				}

				drawEdge(_cellMap.get(r.getA()), _cellMap.get(r.getB()), r.getType());
			}
		}
	}

	private String getLabel(RelationsList node, HashMap<String, String> map, int labelType)
	{	
		String[] splittedKey = node.getKey().split(" ");
		String key = (splittedKey.length > 1 ? splittedKey[0] : node.getKey());
		
		if(labelType == nodeLabel.CONTENT.ordinal())
		{
			if(node.getEntity() != null)
				return node.getEntity();
			
			else if(map != null && map.containsKey(key))
				return map.get(key);
			
		}
		else if(labelType == nodeLabel.BOTH.ordinal())
		{
			if(node.getEntity() != null)
				return node.getKey() + " - " + node.getEntity();
				
			if(map != null && map.containsKey(key))
				return node.getKey() + " - " + map.get(key);
			
		}
		
		return node.getKey();
	}

	private void drawNode(DefaultGraphCell cell, int x, int y)
	{
		GraphConstants.setFont(cell.getAttributes(), NODE_FONT);
		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x,y,NODE_WIDTH,NODE_HEIGHT));
		GraphConstants.setOpaque(cell.getAttributes(), true);
		GraphConstants.setEditable(cell.getAttributes(), false);
		GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createLineBorder(Color.black));
		GraphConstants.setAutoSize(cell.getAttributes(), true);
		//GraphConstants.setBackground(cell.getAttributes(), Color.lightGray);
		//GraphConstants.setForeground(cell.getAttributes(), Color.black);
	}

	private void drawEdge(DefaultGraphCell a, DefaultGraphCell b, String type)
	{	
		//System.out.println("A:"+a);
		//System.out.println("B:"+b);
		DefaultEdge edge = new DefaultEdge();

		int arrow = GraphConstants.ARROW_CLASSIC;
		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		GraphConstants.setEndFill(edge.getAttributes(), true);

		_jGraph.getGraphLayoutCache().insertEdge(
				edge, a.getFirstChild(), b.getFirstChild());

		GraphConstants.setLineColor(edge.getAttributes(), getColorForType(type));
		GraphConstants.setEndFill(edge.getAttributes(), true);
	}

	private Color getColorForType(String type)
	{
		if(_colorMap.containsKey(type))
			return _colorMap.get(type);
		else
			return _colorMap.get(Relation.OUTRA);
	}

	/*private void setOrganicLayout(){
		JGraphFacade facade = new JGraphFacade(_jGraph); // Pass the facade the JGraph instance
		JGraphOrganicLayout layout = new JGraphOrganicLayout();
		layout.setOptimizeEdgeCrossing( true );
		layout.setOptimizeNodeDistribution( true );
		layout.setNodeDistributionCostFactor( 50000011 );
		layout.setOptimizeBorderLine( true );
		layout.setAverageNodeArea( 1000000 );
		layout.setInitialMoveRadius( 500 );
		layout.setRadiusScaleFactor( 0.99 );
		layout.setOptimizeEdgeDistance( true );
		layout.setFineTuning( true );
		layout.setBorderLineCostFactor( 50000 );
		layout.setMaxIterations(20);
		
		facade.setIgnoresUnconnectedCells( true );
		layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion

		Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
		_jGraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph 
	}
	
	private void setFastOrganicLayout(){
		JGraphFacade facade = new JGraphFacade(_jGraph); // Pass the facade the JGraph instance
		JGraphFastOrganicLayout layout = new JGraphFastOrganicLayout();
		
		facade.setIgnoresUnconnectedCells( true );
		layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion

		Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
		_jGraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph 
	}
	
	private void setCircleLayout(){
		JGraphFacade facade = new JGraphFacade(_jGraph); // Pass the facade the JGraph instance
		JGraphLayout layout = new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE);
		
		facade.setIgnoresUnconnectedCells( true );
		layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion

		Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
		_jGraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph 
	}
	
	private void setSelfOrganizingLayout(){
		JGraphFacade facade = new JGraphFacade(_jGraph); // Pass the facade the JGraph instance
		JGraphSelfOrganizingOrganicLayout layout = new JGraphSelfOrganizingOrganicLayout();
		
		layout.setDensityFactor(50000);
		
		facade.setIgnoresUnconnectedCells( true );
		layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion

		Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
		_jGraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph 
	}*/
	
	public void show(String title)
	{
		//para utilizacao com o JGraphLayout Pro 
		//setOrganicLayout();
		
		JFrame frame = new JFrame(title);
		
		frame.getContentPane().add(new JScrollPane(_jGraph));
		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void graphChanged(GraphModelEvent arg0) {

	}
}
