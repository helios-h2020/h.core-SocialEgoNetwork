package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import java.util.Calendar;
import java.util.Date;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;

/**
 * This class implements a {@link ContextualEgoNetworkListener} that counts the number of ego and alter interactions
 * over the course of the week and time of day.
 * 
 * @author Emmanouil Krasanakis (maniospas@hotmail.com)
 */
public class ActivityListener implements ContextualEgoNetworkListener {
	public static class WeeklyHistory {
		private long[][] timeCounter;
		public WeeklyHistory() {
			timeCounter = new long[7][24];
		}
		public void event() {
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(new Date());
		    int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
		    int hour = calendar.get(Calendar.HOUR_OF_DAY);
		    timeCounter[day][hour] += 1;
		}
	}
	
	@Override
	public void onCreateInteraction(Interaction interaction) {
		Edge edge = interaction.getEdge();
		edge.getSrc().getOrCreateInstance(WeeklyHistory.class).event();
		edge.getDst().getOrCreateInstance(WeeklyHistory.class).event();
	}
}
