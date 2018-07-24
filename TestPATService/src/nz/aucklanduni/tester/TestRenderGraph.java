package nz.aucklanduni.tester;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;

import nz.aucklanduni.model.ComponentConfig;
import nz.aucklanduni.util.RandomUtil;

public class TestRenderGraph {
	private static final int MAX_START = 2;

	public static void main(String[] args) {
		for(int i=0; i<10; i++) {
			List<ComponentConfig> archConfig = randomCompList(30,1, 2);
			renderGraph(archConfig);
		}
	
	}

	private static void renderGraph(List<ComponentConfig> compList) {
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		int intialX = 20;
		int intialY = 20;

		graph.getModel().beginUpdate();
		try {
			HashMap<Integer, Object> graphHash = new HashMap<Integer, Object>();
			for (ComponentConfig comp : compList) {
				Object v1 = graph.insertVertex(parent, null, Integer.toString(comp.getId()), 0, 0, 80, 30,comp.isIsstartcaller()?"fillColor=red":"");
				graphHash.put(comp.getId(), v1);
			}
			
			for (ComponentConfig comp : compList) {
				if(comp.getCalls()!=null && comp.getCalls().length!=0 ) {
					for(int callId: comp.getCalls()) {
						graph.insertEdge(parent, null, "", graphHash.get(new Integer(comp.getId())), graphHash.get(new Integer(callId)));
					}
				}
			}

			mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
			layout.setOrientation(SwingConstants.WEST);
			layout.execute(parent);
			
		} finally {
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
		try {
			RandomUtil randUtil = new RandomUtil();
			ImageIO.write(image, "PNG", new File("D:\\tmp\\graph"+randUtil.randomNumber(111, 999)+".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<ComponentConfig> randomCompList(int N, int minConnect ,int maxConnect) {
		RandomUtil randUtil = new RandomUtil();
		List<ComponentConfig> compList = new ArrayList<ComponentConfig>();
		for (int i = 0; i < N; i++) {
			ComponentConfig comp = new ComponentConfig();
			comp.setId(randUtil.randomNumber(100, 999));
			compList.add(comp);
		}
		List<Integer> calledComp = new ArrayList<Integer>();
		for (ComponentConfig comp : compList) {
			// random number of connection
			int connectNo = randUtil.randomNumber(minConnect, maxConnect);
			List<Integer> calls = new ArrayList<Integer>();
			for (int i = 0; i < connectNo; i++) {
				ComponentConfig connectTo;
				int j = 0;
				do {
					connectTo = compList.get(randUtil.randomNumber(0, N - 1));
					j++;
				} while (j < N * 2 && (connectTo.getId() == comp.getId()
						|| /* calls.contains(connectTo.getId()) || */ connectTo.hasCallTo(comp.getId())));
				calls.add(connectTo.getId());
			}
			calledComp.addAll(calls);
			int[] callsArray = new int[connectNo];
			for (int i = 0; i < connectNo; i++) {
				callsArray[i] = calls.get(i);

			}
			comp.setCalls(callsArray);

		}
		// select & initial start component
		boolean hasStartComp = false;
		int startinvoke = 0;
		for (ComponentConfig comp : compList) {
			if (!calledComp.contains(new Integer(comp.getId()))) {
				comp.setIsstartcaller(true);
				hasStartComp = true;
				startinvoke++;
				if (startinvoke > MAX_START)
					break;
			}
		}
		if (!hasStartComp) {
			compList.get(randUtil.randomNumber(0, N - 1)).setIsstartcaller(true);
		}

		return compList;
	}
}
