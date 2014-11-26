import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;

     
    @SuppressWarnings("serial")
	public class ClicksTest extends JFrame implements ViewerListener {
        boolean clickEnd, clickStart = false;
        Point start, end;
    	static JSlider zoomSlider;
        protected boolean loop = true;
        private Graph graph;
        static String my_graph =
                "DGS004\n"
                + "my 0 0\n"
                + "an A \n"
                + "an B \n"
                + "an C \n"
                + "an D \n"
                + "an E \n"
                + "an F \n"
                + "ae AB A B weight:1 \n"
                + "ae AD A D weight:1 \n"
                + "ae BC B C weight:1 \n"
                + "ae CF C F weight:10 \n"
                + "ae DE D E weight:1 \n"
                + "ae EF E F weight:1 \n"
                ;
        public static void main(String args[]) 
        {
        	System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
            new ClicksTest();
        }
       
        public ClicksTest()
        {
            setSize(new Dimension(600, 600));
            graph = new SingleGraph("Clicks");

            //graph.addAttribute("ui.stylesheet", "url('file:///C:/Users/Anshuli-PC/workspace/COP/styleSheetClicks')");
           
            graph.addAttribute("ui.stylesheet",   "url('file:///C:/Users/Anshuli-PC/workspace/COP/ssheet')");
            
            
            //graph.addAttribute("ui.stylesheet", "node {shape: diamond; } ");
            	/*css properties are cumulative.*/
            System.out.println();
            graph.setStrict(false);
            graph.addAttribute("ui.quality");
            graph.addAttribute("ui.antialias");
            
            
            try {
            ByteArrayInputStream bs = new ByteArrayInputStream(my_graph.getBytes());
            FileSourceDGS source = new FileSourceDGS();
            source.addSink(graph);
            
			source.readAll(bs);
			} catch (IOException e) {
				e.printStackTrace();
			}
            for(Node node : graph)
            {
            	node.addAttribute("ui.label", "Node "+node);
            }
            Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            final View view = viewer.addDefaultView(false);
            //view.getCamera().setViewPercent(1);
            viewer.enableAutoLayout();
           
            view.setMinimumSize(new Dimension(400, 400));
            view.setPreferredSize(new Dimension(400, 400));
           
            this.getContentPane().setLayout(new BorderLayout());
            JScrollPane jsp = new JScrollPane(view);
            
            
            zoomSlider = new JSlider(100,200,100);
    	    zoomSlider.setMajorTickSpacing(20);
    	    zoomSlider.setPaintTicks(true);
    	    zoomSlider.setPaintLabels(true);
    	    zoomSlider.addChangeListener(new ChangeListener() 
    	    {
    	        public void stateChanged(ChangeEvent ce) 
    	        {
    	        	JSlider source = (JSlider)ce.getSource();
    	            if (!source.getValueIsAdjusting()) 
    	            {
    	            	int percent = source.getValue();
    	            	double viewPercent= (200.0-percent)/(100);
    	            	/*Viewer viewer = graph.display(false);
    	                View view = viewer.getDefaultView();*/
    	                view.resizeFrame(800, 600);
    	                //view.getCamera().setViewCenter(440000, 2503000, 0);
    	                view.getCamera().setViewPercent(viewPercent); 
    	            }
    	        }
    	    });
    	    zoomSlider.setVisible(true);
    	    
    	    
            this.getContentPane().add(jsp, BorderLayout.CENTER);
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
            
            
            ((Component) view).addMouseMotionListener(new MouseMotionListener()
		    {
				@Override
				public void mouseDragged(MouseEvent arg0) 
				{
					if(clickStart==false)
					{
						clickStart = true;
						start = arg0.getLocationOnScreen();
					}
					//TODO use this click thingie to make the selection zoom work.
					
					//System.out.println("location -x :"+arg0.getLocationOnScreen());
					//System.out.println("mouse dragged "+arg0.getLocationOnScreen());
					//view.getCamera().setViewCenter(440000, 2503000, 0);
			        //view.getCamera().setViewPercent(0.25);  
				}

				@Override
				public void mouseMoved(MouseEvent arg0) {
					//System.out.println("mouse moved.");
				}
		        	
		    });
            
            //ViewerPipe fromViewer = new BlockingViewerPipe(viewer);
            ViewerPipe fromViewer = viewer.newViewerPipe();
            //ThreadProxyPipe fromViewer = new ThreadProxyPipe();
            //fromViewer.init((Source) viewer);
            fromViewer.addViewerListener(this);
            fromViewer.addSink(graph);
     
            this.setVisible(true);
            
            view.add(zoomSlider);
            while(loop) 
            {
            	fromViewer.pump();//TODO this slows down things. blocking pump?
				//fromViewer.blockingPump();
			}
           
        }
     
        public void viewClosed(String id) 
        {
            loop = false;
            System.out.println("View closed");
        }
     
        public void buttonPushed(String id) {
            
        	if(graph.getNode(id)!=null)
        	{
        		Node node = graph.getNode(id);
        		System.out.println("button clicked on node "+graph.getNode(id));
        		if(graph.getAttribute("ui.stylesheet").toString().equalsIgnoreCase("url('file:///C:/Users/Anshuli-PC/workspace/COP/styleSheetClicks')"))
        			node.addAttribute("ui.style", "fill-color: rgb(240,0,0);");
        		else
        		{
	        		if(node.hasAttribute("ui.class") && node.getAttribute("ui.class")=="clicked_odd"  )
	        		{
	        			node.addAttribute("ui.class", "clicked_even");
	        			System.out.println("clicked_even class for node "+node);
	        		}
	        		else
	        		{
	        			node.addAttribute("ui.class", "clicked_odd");
	        			System.out.println("clicked_odd class for node "+node);
	        		}
	        		
        		}
        	}
        }
     
        public void buttonReleased(String id) {
            System.out.println("Button released on node "+id);
        }
    }

