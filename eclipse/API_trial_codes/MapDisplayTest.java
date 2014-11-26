import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;


import static org.graphstream.algorithm.Toolkit.*;

public class MapDisplayTest {
    public static void main(String args[]) {
        new MapDisplayTest();
    }
    
	public MapDisplayTest() {
    	System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new MultiGraph("Le Havre");
        try {
            graph.read("LeHavre.dgs");
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        graph.addAttribute("ui.stylesheet", "url('file:///C:/Users/Anshuli-PC/workspace/COP/styleSheet')");
        
        /*Adding style classes*/
        for(Edge edge: graph.getEachEdge()) {
        	if(edge.hasAttribute("isTollway")) {
                edge.addAttribute("ui.class", "tollway");
           } else if(edge.hasAttribute("isTunnel")) {
                edge.addAttribute("ui.class", "tunnel");
           } else if(edge.hasAttribute("isBridge")) {
                edge.addAttribute("ui.class", "bridge");
           }
           
            
            /*Dynamically changing colors and size*/
            
            /*fill-mode: dyn-plain in the stylesheet 
       		 *if you define three colors for fill-color the values for ui.color are still between 0 and 1, 
             *and the final color will be an interpolation of the colors*/
        
            double speedMax = edge.getNumber("speedMax") / 130.0;
            edge.setAttribute("ui.color", speedMax);
        }
        
        /*Adding Sprites*/
        SpriteManager sman = new SpriteManager(graph);
        Sprite s1 = sman.addSprite("S1");
        Sprite s2 = sman.addSprite("S2");
        Node n1 = randomNode(graph); 
        Node n2 = randomNode(graph); 
        double p1[] = nodePosition(n1); 
        double p2[] = nodePosition(n2); 
        s1.setPosition(p1[0], p1[1], p1[2]); 
        s2.setPosition(p2[0], p2[1], p2[2]);
        
        /*Zooming and panning*/
        Viewer viewer = graph.display(false);
        View view = viewer.getDefaultView();
        view.resizeFrame(800, 600);
        view.getCamera().setViewCenter(440000, 2503000, 0);
        view.getCamera().setViewPercent(0.25);    
        //zoom and pan the view using the page-up and page-down keys to zoom and the arrow keys to pan.
     

    	FileImages img = new FileImages();
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    	img.getSnapshot(graph, "screenshot"+timeStamp+".png");
    }
}