package org.processmining.causalityinference.help;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class GraphTest extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;
	
	mxGraph graph;
	
	public GraphTest()
	{
		super("Hello, World!");

		graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		Object e1;
		Object e2;
		Object e3;
		Object e4;
		Object e5;
		
		String StyleCofounder = "";

		graph.getModel().beginUpdate();
		applyEdgeDefaults();
		try
		{
			Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
					30);
			Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
					80, 30);
			Object v3 = graph.insertVertex(parent, null, "Mahnaz", 100, 150,
					80, 30);
			Object v4 = graph.insertVertex(parent, null, "Mehran", 20, 150,
					80, 30);
			Object v5 = graph.insertVertex(parent, null, "Minen", 240, 20,
					80, 30);
			e5 = graph.insertEdge(parent, null, "Edge5", v1, v5);
			e1 = graph.insertEdge(parent, null, "Edge1", v1, v2, "dashed=1;fontColor=#FF0000;");
	//		Object e3 = graph.insertEdge(parent, null, "Edge", v2, v1);
			e2 = graph.insertEdge(parent, null, "Edge2", v1, v3, "dashed=1;fontColor=#FF0000;");
			e4 = graph.insertEdge(parent, null, "Edge4", v1, v4);
			((mxCell) e4).setStyle(mxConstants.STYLE_ENDARROW+mxConstants.ARROW_OVAL);

		}
		finally
		{
			graph.getModel().endUpdate();
		}
		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		mxGraphModel graphModel  =       (mxGraphModel)graphComponent.getGraph().getModel();
		Map<String, Object> o = graphModel.getCells();
	    Collection<Object> cells =  graphModel.getCells().values(); 
	    Object[] cs = new Object[1];
	    for (Object cell : cells) {
	    	mxCell m = (mxCell) cell;
	    	if (m.isEdge()) {
	    		cs[0]=cell;
	    		break;
	    	}		    		
	    }
    

//	    mxUtils.setCellStyles(graphComponent.getGraph().getModel(), 
//	    cs, mxConstants.STYLE_ENDARROW+";"+mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL+";"+mxConstants.ARROW_OVAL);
	    cs[0] = e4;
	    mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    cs, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
	    mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    cs, mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
	    
	    cs[0] = e2;
	    mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    cs, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
	    
	    getContentPane().add(graphComponent);

//	    mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
//	    layout.execute(graph.getDefaultParent());

		getContentPane().add(graphComponent);
	}

	public static void main(String[] args)
	{
		GraphTest frame = new GraphTest();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}
	
	private void applyEdgeDefaults() {
	    // Settings for edges
	    Map<String, Object> edge = new HashMap<String, Object>();
	    edge.put(mxConstants.STYLE_ROUNDED, true);
	    edge.put(mxConstants.STYLE_ORTHOGONAL, false);
	    edge.put(mxConstants.STYLE_EDGE, "elbowEdgeStyle");
	    edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
	    edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
	    edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
	    edge.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
	    edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
	    edge.put(mxConstants.STYLE_STROKECOLOR, "#000000"); // default is #6482B9
	    edge.put(mxConstants.STYLE_FONTCOLOR, "#446299");

	    mxStylesheet edgeStyle = new mxStylesheet();
	    edgeStyle.setDefaultEdgeStyle(edge);
	//    graph.setStylesheet(edgeStyle);
	}
	
}