package cop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphReplay;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.ui.geom.Point3;

public class GraphTools {

	/**
	 * Captures a .png image of the graph and saves it - uses Apache Commons Imaging Library.
	 */
	static void SnapShot()
    {
    	FileImages img = new FileImages();
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    	img.getSnapshot(GraphClass.graph, "screenshot"+timeStamp+".png");
    }

    /**
     * 
     * @param graph the graph that is the checked to find out number of connected components.
     * @return the number of connected components of the graph
     */
    public static int connectedComponents(SingleGraph graph)
    {
    	ConnectedComponents cc = new ConnectedComponents();
        cc.init(graph);
        int components = cc.getConnectedComponentsCount();
        if(components != 1)
        	new NotifyUser("No path exists!");
        return components;
    }
	
}

class FileImages extends FileSinkImages
{
	public static BufferedImage img;
	
	public synchronized void outputNewImage(String filename) {
		switch (layoutPolicy) {
		case COMPUTED_IN_LAYOUT_RUNNER:
			layoutPipeIn.pump();
			break;
		case COMPUTED_ONCE_AT_NEW_IMAGE:
			if (layout != null)
			layout.compute();
			break;
		case COMPUTED_FULLY_AT_NEW_IMAGE:
			stabilizeLayout(layout.getStabilizationLimit());
			break;
		default: break;
		}
		if (resolution.getWidth() != image.getWidth() || resolution.getHeight() != image.getHeight())
			initImage();
		if (clearImageBeforeOutput) {
			for (int x = 0; x < resolution.getWidth(); x++)
			for (int y = 0; y < resolution.getHeight(); y++)
			image.setRGB(x, y, 0x00000000);
		}
		if (gg.getNodeCount() > 0) {
			if (autofit) {
				gg.computeBounds();
				Point3 lo = gg.getMinPos();
				Point3 hi = gg.getMaxPos();
				renderer.getCamera().setBounds(lo.x, lo.y, lo.z, hi.x, hi.y, hi.z);
			}
			renderer.render(g2d, 0, 0, resolution.getWidth(),
			resolution.getHeight());
		}
		for (PostRenderer action : postRenderers)
			action.render(g2d);
		
		image.flush();
	
	}

	
	public synchronized void writeAll(Graph g, String filename) throws IOException
	{
		clearGG();
		GraphReplay replay = new GraphReplay(String.format("file_sink_image-write_all-replay-%x", System.nanoTime()));
		
		replay.addSink(gg);
		replay.replay(g);
		replay.removeSink(gg);
		
		outputNewImage(filename);
		img = image;
		clearGG();
	}
	
	public  void getSnapshot(Graph g, String filename)
	{
		try {
			File f = new File(filename);
			writeAll(g,filename);
			Imaging.writeImage(img,f,ImageFormats.PNG,null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ImageWriteException e) {
			e.printStackTrace();
		}
		
	}
}

