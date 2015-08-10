package com.lio.log.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDBUtil {

	 private Connection getDBConnection() throws Exception {
		 String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		    String dbName = "dtloadb1";
		    String connectionURL = "jdbc:derby:" + dbName + ";create=true";
		    Class.forName(driver);

		    Connection conn = DriverManager.getConnection(connectionURL);

		return conn;
	}
	 
	public long getLastLogTime(String server, String node) throws Exception{
		
		Connection connection = getDBConnection(); 
		long lastLogTime = 0l;
		String query = "SELECT SERVER as server, "
				+ "NODE as node, "
				+ "LASTLOGTIME as lastlogtime, "
				+ "LOGLINE as logline "
				+ "FROM LASTLOGTABLE WHERE server = ? AND node = ?";
		
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, server);
		stmt.setString(2, node);
		
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			if(rs.getString(3) != null && rs.getString(3) != ""){
				lastLogTime = Long.parseLong(rs.getString(3));
			}
		}
		
		return lastLogTime;
	}
	
	public static void main(String[] args) throws Exception{
		DerbyDBUtil util = new DerbyDBUtil();  
		long lastLogTime = util.getLastLogTime("termspmr04.mayo.edu:9083", "WebServiceREF1_pmr04");
		System.out.println(lastLogTime);
	}

	public void updateLastLogTime(String server, String node, long time,String line) throws Exception {
	Connection connection = getDBConnection();
		//System.out.println("Time calculated for last line"+time+" Line:"+line);
			PreparedStatement pstmt = connection.prepareStatement("UPDATE LASTLOGTABLE SET LASTLOGTIME=?,logLine=? WHERE "
					+ "SERVER=? AND NODE=?");
			pstmt.setString(1, time+"");
			pstmt.setString(2, line);
			pstmt.setString(3, server);
			pstmt.setString(4, node);
			
			
			pstmt.execute();
			connection.commit();
			pstmt.close();
			
			connection.close();
		
	}

	public StringBuffer getExceptionMessages(String server, String node) throws Exception {

		Connection connection = getDBConnection(); 
		String query = "SELECT SERVER as server, "
				+ "NODE as node, "
				+ "LOGTIME as logTime, "
				+ "EXCEPTIONLINE as exceptionLine "
				+ "FROM LOGEXCEPTIONTABLE WHERE server = ? AND node = ? AND TOBENOTIFIED = true";
		
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, server);
		stmt.setString(2, node);
		
		ResultSet rs = stmt.executeQuery();
		StringBuffer buffer = new StringBuffer(); 
	
		while(rs.next()){
			buffer.append(rs.getString(3));
			buffer.append(rs.getString(4));
			buffer.append("\n");
		}
		
		String deleteRecQuery = "DELETE FROM LOGEXCEPTIONTABLE WHERE server = ? AND node = ? AND TOBENOTIFIED = true";
		stmt = connection.prepareStatement(deleteRecQuery);
		stmt.setString(1, server);
		stmt.setString(2, node);
		stmt.executeUpdate();
		
		connection.commit();
		stmt.close();
		connection.close();
		return buffer;
		
	}

	public void addExceptionMessage(String server, String node,
			String date, String line, boolean b) throws Exception {
		
		Connection connection = getDBConnection(); 
		String query = "INSERT INTO LOGEXCEPTIONTABLE VALUES(?,?,?,?,?)";
		
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, server);
		stmt.setString(2, node);
		stmt.setString(3, date);
		stmt.setString(4, line);
		stmt.setString(5, true+"");
	
		stmt.executeUpdate();
		
		stmt.close();
		connection.close();
	}

	public void deleteMessagesAfterNotified(String server, String node) throws Exception {
		
		Connection connection = getDBConnection(); 
		String query = "DELETE FROM LOGEXCEPTIONTABLE WHERE server=?"+ " "+"and node=?";
		
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, server);
		stmt.setString(2, node);
		
		stmt.executeUpdate();
		connection.commit();
		stmt.close();
		connection.close();
		
	}
}
