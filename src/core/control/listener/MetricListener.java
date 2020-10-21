/**
 * 
 */
package core.control.listener;

import core.MessageListener;
import report.control.metric.MetricDetails;

/**
 * Interface to listen to metric events.
 *
 */
public interface MetricListener extends MessageListener {
	/**
	 * Method to be called when a new metric is created.
	 * @param metricDetails The creation and content details of the metric.
	 */
	public void newMetric(MetricDetails metricDetails);
}
