package listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class AWSContextHandler
 *
 */
public class AWSContextHandler implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public AWSContextHandler() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
	 try {
	        com.amazonaws.http.IdleConnectionReaper.shutdown();
	    } catch (Throwable t) {
	        // log the error
	    	t.printStackTrace();
	    }
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }
	
}
