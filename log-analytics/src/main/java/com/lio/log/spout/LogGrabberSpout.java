package com.lio.log.spout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * LogGrabberSpout.java
 * Purpose: Makes a http call the server for the log file.
 * 			Read each line and emit the line, host, node, stickybit (whether it is the last line or not)
 * */
public class LogGrabberSpout extends BaseRichSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(LogGrabberSpout.class);
	
	private SpoutOutputCollector collector;
	ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(500);

	private String server;
	private String node;

	public LogGrabberSpout(String server, String node) {
		this.server = server;
		this.node = node;
	}

	
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.collector = collector;

	}
	/**
	 * Called by the Storm as long as user dont kill the process. 
	 * */
	public void nextTuple() {

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://" + server
				+ "/mwlogviewer/htmlservlet?jvmName=" + node
				+ "&filename=SystemOut.log");
		HttpResponse response;
		try {
			// Execute
			response = client.execute(get);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() == 200) {
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String in;
				boolean isLastLine = false;
				String lastLine = "";
				// Read line by line
				while ((in = reader.readLine()) != null) {
					lastLine = in;
					try {
						collector.emit(new Values(server, node, in,isLastLine));
					} catch (Exception e) {
						logger.error("Could not read from Lines server. Connection would have been lost. "+e.getMessage());
					}
				}
				isLastLine = true;
				collector.emit(new Values(server, node, lastLine,isLastLine));
			}
		} catch (IOException e) {
			logger.error("MwLoggerViewer Application seems to be down. Will retry after 10 minutes"+e.getMessage());
			// Notify User if necessary using Spring AOP
			try {
				// Wait for 10 minutes if the server is down.
				// logger.debug("Server might be down. App will resume after 10 minutes");
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
			}
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("server", "node", "line","isLastLine"));

	}

}
