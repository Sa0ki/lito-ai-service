import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LitoServer {
    private final int port;
    private final int THREAD_POOL_SIZE;
    private final String endpoint;
    public LitoServer(){
        Props.loadProperties();
        port = Integer.parseInt(Props.getValueOf("port"));
        THREAD_POOL_SIZE = Integer.parseInt(Props.getValueOf("thread_pool_size"));
        endpoint = Props.getValueOf("endpoint");
    }
    public void start(){
       try{
           HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

           ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

           server.setExecutor(executor);

           server.createContext(endpoint, new ImageHandler());

           server.start();

           System.out.println("Server started on port " + port + ".");

       }catch(Exception e){
           System.out.println(e.getMessage());
       }
    }
}
