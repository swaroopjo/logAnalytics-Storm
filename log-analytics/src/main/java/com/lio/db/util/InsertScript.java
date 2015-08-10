package com.lio.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import backtype.storm.tuple.Values;

public class InsertScript {

	 private Connection getDBConnection() throws Exception {
		 String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		    String dbName = "dtloadb1";
		    String connectionURL = "jdbc:derby:" + dbName + ";create=true";
		    Class.forName(driver);

		    Connection conn = DriverManager.getConnection(connectionURL);

		return conn;
	}
	 
	 public void dropAndCreateDB() throws Exception {
			
			Connection connection = getDBConnection();
			Statement dropStatement = connection.createStatement();
			dropStatement.executeUpdate("DROP TABLE  LASTLOGTABLE");
			Statement stmt = connection.createStatement();
			String createLastLogTable = "CREATE TABLE LASTLOGTABLE (SERVER VARCHAR(32) NOT NULL" +
					", NODE VARCHAR(50) NOT NULL" +
					", LASTLOGTIME VARCHAR(50) " +
					", LOGLINE VARCHAR(5000))";
			stmt.executeUpdate(createLastLogTable);
			dropStatement.executeUpdate("DROP TABLE  LOGEXCEPTIONTABLE");
			String createLogExceptionTable = "CREATE TABLE LOGEXCEPTIONTABLE ( SERVER VARCHAR(50) NOT NULL" +
					", NODE VARCHAR(50) " +
					", LOGTIME VARCHAR(50)"
					+ ", EXCEPTIONLINE VARCHAR(5000)"
					+ ", TOBENOTIFIED VARCHAR(10))";
			stmt.executeUpdate(createLogExceptionTable);
			stmt.close();
			connection.commit();
			connection.close();
		}
	 
	 public static void main(String[] args) throws Exception{
		 InsertScript generator = new InsertScript(); 
		 generator.dropAndCreateDB(); 
		String server = "",node = "",lastLogTime = "",logLine = "";
			
		server = "pmr01.mayo.edu:9082";
		node = "Web_pmr01";
		lastLogTime = getTimeinLongValue("[7/30/15 10:21:28:544 CDT]");
		 generator.insertLastLog(server,node,lastLogTime,logLine);
		server = "pmr01.mayo.edu:9082";
		node = "Web2_pmr01";
		lastLogTime = getTimeinLongValue("[7/30/15 10:21:28:544 CDT]");
		 generator.insertLastLog(server,node,lastLogTime,logLine);
		server = "pmr04.mayo.edu:9083";
		node = "Web_pmr04";
		lastLogTime = getTimeinLongValue("[7/30/15 10:21:28:544 CDT]");
		 generator.insertLastLog(server,node,lastLogTime,logLine);
		server = "pmr04.mayo.edu:9083";
		node = "Web_pmr04";
		lastLogTime = getTimeinLongValue("[7/30/15 10:21:28:544 CDT]");
		 generator.insertLastLog(server,node,lastLogTime,logLine);
		 
		 generator.listAllRecords(); 
	 }
	 
	public static String getTimeinLongValue(String time){
		String lastLogTime = "";
		  Pattern pattern = Pattern.compile("^\\[[0-9,\\/+]+\\s+[0-9,:]+\\s[A-Z]+\\]");
		  Matcher matcher= pattern.matcher(time);
		  if(matcher.find()){
			  
			 
				SimpleDateFormat outFormat = new SimpleDateFormat(
						"MM/dd/yy HH:mm:ss:SSS");
				Date d = null;
				try {
					d = outFormat.parse(time.substring(1,time.length()-4));
					lastLogTime = d.getTime()+"";
				} catch (ParseException e) {
					e.printStackTrace();
					//logger.error("Could not parse the Date Pattern Matching error");
				}
		  }
		 
	  return lastLogTime;
	}
private void listAllRecords() throws Exception {
	Connection connection = getDBConnection();
	Statement dropStatement = connection.createStatement();
	Statement stmt = connection.createStatement();
	String select = "SELECT * FROM LASTLOGTABLE";
	ResultSet rs = stmt.executeQuery(select);
	
	while(rs.next()){
		System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4));
	}
	stmt.close();
	connection.commit();
	connection.close();
		
	}

//  server= ,node= ,lastLogTime=[7/27/15 10:53:58:094 CDT]
	 
	public void insertLastLog(String server, String node, String lastLogTime,
			String logLine) throws Exception {
		Connection connection = getDBConnection();
		
		PreparedStatement pstmt = connection.prepareStatement("insert into LASTLOGTABLE values (?,?,?,?)");
		pstmt.setString(1, server);
		pstmt.setString(2, node);
		pstmt.setString(3, lastLogTime);
		pstmt.setString(4, logLine);
		
		pstmt.execute();
		pstmt.close();
		connection.commit();
		connection.close();
		
	}

	
}
