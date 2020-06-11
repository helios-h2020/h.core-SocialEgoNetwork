package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import java.util.concurrent.locks.ReentrantLock;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This class implements a wrapper for {@link ContextualEgoNetworkListener} instances
 * that performs callbacks asynchronously to the main thread. For example, this wrapper may be used
 * if a listener involves heavy computations that would create large overheads.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class AsyncRunListener implements ContextualEgoNetworkListener {
	private ContextualEgoNetworkListener wrappedListener;
	private ReentrantLock lock;
	
	public AsyncRunListener(ContextualEgoNetworkListener wrappedListener) {
		this.wrappedListener = wrappedListener;
		lock = new ReentrantLock();
	}
	
	/**
	 * Retrieves the wrapped listener to which this listener's callbacks are asynchronously forwarded to.
	 * @return The wrapped listener
	 */
	public ContextualEgoNetworkListener getWrappedListener() {
		return wrappedListener;
	}
	
	public void init(ContextualEgoNetwork contextualEgoNetwork) {
		try {
			if((wrappedListener.getClass().getMethod("init", ContextualEgoNetwork.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.init(contextualEgoNetwork);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onCreateNode(Node node) {
		try {
			if((wrappedListener.getClass().getMethod("onCreateNode", Node.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onCreateNode(node);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onCreateContext(Context context) {
		try {
			if((wrappedListener.getClass().getMethod("onCreateContext", Context.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onCreateContext(context);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onLoadContext(Context context) {
		try {
			if((wrappedListener.getClass().getMethod("onLoadContext", Context.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onLoadContext(context);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onSaveContext(Context context) {
		try {
			if((wrappedListener.getClass().getMethod("onSaveContext", Context.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onSaveContext(context);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onRemoveContext(Context context) {
		try {
			if((wrappedListener.getClass().getMethod("onRemoveContext", Context.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onRemoveContext(context);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onAddNode(Context context, Node node) {
		try {
			if((wrappedListener.getClass().getMethod("onAddNode", Context.class, Node.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onAddNode(context, node);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}

	public void onRemoveNode(Node node) {
		try {
			if((wrappedListener.getClass().getMethod("onRemoveNode", Node.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onRemoveNode(node);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onRemoveNode(Context context, Node node) {
		try {
			if((wrappedListener.getClass().getMethod("onRemoveNode", Context.class, Node.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onRemoveNode(context, node);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onCreateEdge(Edge edge) {
		try {
			if((wrappedListener.getClass().getMethod("onCreateEdge", Edge.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onCreateEdge(edge);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onRemoveEdge(Edge edge) {
		try {
			if((wrappedListener.getClass().getMethod("onRemoveEdge", Edge.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onRemoveEdge(edge);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
	
	public void onCreateInteraction(Interaction interaction) {
		try {
			if((wrappedListener.getClass().getMethod("onCreateInteraction", Interaction.class)).isDefault())
				return;
		}
		catch(Exception e) {
			Utils.error(e);
		}
		(new Thread() {
			@Override
			public void run() {
			    lock.lock();
			    try {
			    	wrappedListener.onCreateInteraction(interaction);
			    } finally {
			        lock.unlock();
			    }
			}
		}).start();
	}
}
