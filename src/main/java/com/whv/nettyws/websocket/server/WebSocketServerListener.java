package com.whv.nettyws.websocket.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebSocketServerListener  implements ServletContextListener{

	public void contextDestroyed(ServletContextEvent arg0) {
		
		
	}

	public void contextInitialized(ServletContextEvent e) {
		 String portStr = e.getServletContext().getInitParameter("socketPort");
		 WebSocketServer webSocketServer ;
		 if(portStr!=null&&portStr.length()>0){
			 webSocketServer = new WebSocketServer(Integer.parseInt(portStr));
		 }else{
			 webSocketServer = new WebSocketServer();
		 }
		 webSocketServer.run();
	}

}
