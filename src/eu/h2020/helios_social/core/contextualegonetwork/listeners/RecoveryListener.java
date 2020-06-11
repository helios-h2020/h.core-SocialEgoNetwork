package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This class implements a {@link ContextualEgoNetworkListener} that automatically 
 * safeguards the contextual ego network from failing to call save() before terminating the application.
 * For the time being, this does not safeguard against keeping unsaved changes on objects obtained of the
 * {@link eu.h2020.helios_social.core.contextualegonetwork.CrossModuleComponent#getOrCreateInstance(Class)}.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public final class RecoveryListener implements ContextualEgoNetworkListener {
	private PrintWriter writer = null;  
	private ContextualEgoNetwork contextualEgoNetwork = null;
	private static String SEPARATOR = " @@ ";
	
	/**
	 * Used by the {@link #init(ContextualEgoNetwork)} of this listener to recover context actions that have not been saved.
	 * Immediately saves the recovered contextual ego network.
	 * @param recoveryFile : The path to the recovery file
	 */
	protected synchronized void recover(String recoveryFile) {
		if(!(new File(recoveryFile)).exists())
			return;
	    HashMap<String, ArrayList<String>> contextLines = new HashMap<String, ArrayList<String>>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recoveryFile)));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String contextId = line.split(Pattern.quote(SEPARATOR))[1];
		    	if(!contextLines.containsKey(contextId))
		    		contextLines.put(contextId, new ArrayList<String>());
		    	contextLines.get(contextId).add(line);
		    }
		    br.close();
		}
		catch(Exception e) {
			Utils.error(e);
		}
	    for(String contextId : contextLines.keySet()) {
	    	ArrayList<String> activeLines = new ArrayList<String>();
	    	Context context = contextualEgoNetwork.getContextBySerializationId(contextId);
	    	for(String line : contextLines.get(contextId)) {
	    		if(line.startsWith("context.save"))
	    			activeLines = new ArrayList<String>();
	    		activeLines.add(line);
	    	}
	    	for(String line : activeLines) {
	    		String[] action = line.split(Pattern.quote(SEPARATOR), 6);
	    		if(action[0].equals("context.addNode")) 
	    			context.addNode(contextualEgoNetwork.getOrCreateNode(action[2], null));
	    		else if(action[0].equals("context.removeNode"))
	    			context.removeNode(contextualEgoNetwork.getOrCreateNode(action[2], null));
	    		else if(action[0].equals("context.createEdge"))
	    			context.addEdge(contextualEgoNetwork.getOrCreateNode(action[2], null), contextualEgoNetwork.getOrCreateNode(action[3], null));
	    		else if(action[0].equals("context.removeEdge"))
	    			context.removeEdge(contextualEgoNetwork.getOrCreateNode(action[2], null), contextualEgoNetwork.getOrCreateNode(action[3], null));
	    		else if(action[0].equals("context.removeEdge"))
	    			context.removeEdge(contextualEgoNetwork.getOrCreateNode(action[2], null), contextualEgoNetwork.getOrCreateNode(action[3], null));
	    		else if(action[0].equals("edge.createInteraction")) {
	    			Edge edge = context.getEdge(contextualEgoNetwork.getOrCreateNode(action[2], null), contextualEgoNetwork.getOrCreateNode(action[3], null));
	    			Object data = edge.getContext().getContextualEgoNetwork().getSerializer().deserializeFromString(action[6]);
	    			edge.addInteraction(Long.parseLong(action[4]), Long.parseLong(action[5]), data);
	    		}
	    	
	    	}
	    }
	}
	
	public synchronized void init(ContextualEgoNetwork contextualEgoNetwork) {
		String path = contextualEgoNetwork.getSerializer().getPath()+"recovery.log";
		if(this.contextualEgoNetwork!=null && this.contextualEgoNetwork!=contextualEgoNetwork)
			Utils.error("RecoveryListener can be assigned only to one ContextualEgoNetwork");
		this.contextualEgoNetwork = contextualEgoNetwork;
		try {
			if(writer!=null) 
				writer.close();
			else
				recover(path);
			contextualEgoNetwork.save();
			writer = new PrintWriter(new File(path));
		}
		catch (Exception e) {
			Utils.error(e);
		}
	}
	protected synchronized void write(String line) {
		if(writer==null)
			return;
		writer.write(line+"\n");
		writer.flush();
	}
	
	public void onCreateNode(Node node) {
		contextualEgoNetwork.getSerializer().save(node);
	}
	public void onRemoveNode(Node node) {
		//the contextual ego network autosaves when nodes are removed
	}
	public void onCreateContext(Context context) {
		context.save();//order is important
		contextualEgoNetwork.getSerializer().save(contextualEgoNetwork);
	}
	public void onLoadContext(Context context) {
	}
	public void onSaveContext(Context context) {
		write("context.save"+SEPARATOR+context.getSerializationId());
	}
	public void onAddNode(Context context, Node node) {
		write("context.addNode"+SEPARATOR+context.getSerializationId()+SEPARATOR+node.getId());
	}
	public void onRemoveNode(Context context, Node node) {
		write("context.removeNode"+SEPARATOR+context.getSerializationId()+SEPARATOR+node.getId());
	}
	public void onCreateEdge(Edge edge) {
		write("context.createEdge"+SEPARATOR+edge.getContext().getSerializationId()+SEPARATOR+edge.getSrc().getId()+SEPARATOR+edge.getDst().getId());
	}
	public void onRemoveEdge(Edge edge) {
		write("context.removeEdge"+SEPARATOR+edge.getContext().getSerializationId()+SEPARATOR+edge.getSrc().getId()+SEPARATOR+edge.getDst().getId());
	}
	public void onCreateInteraction(Interaction interaction) {
		Edge edge = interaction.getEdge();
		write("edge.createInteraction"
				+SEPARATOR+edge.getContext().getSerializationId()
				+SEPARATOR+edge.getSrc().getId()
				+SEPARATOR+edge.getDst().getId()
				+SEPARATOR+interaction.getStartTime()
				+SEPARATOR+interaction.getDuration()
				+SEPARATOR+edge.getContext().getContextualEgoNetwork().getSerializer().serializeToString(interaction.getData()));
	}
}
