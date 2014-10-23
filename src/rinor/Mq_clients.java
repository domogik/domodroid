package rinor;

import org.zeromq.ZMQ;
import misc.tracerengine;

public class Mq_clients {
	
private tracerengine Tracer = null;
private String mytag="Mq_clients";

   public void main(String[] args){
	        ZMQ.Context context = ZMQ.context(1);
	        ZMQ.Socket socket = context.socket(ZMQ.SUB);
	        
	        socket.connect ("tcp://localhost:5555");
	        String reponse = socket.recv().toString();
	        Tracer.d(mytag, reponse);
	        
	        
   }
}
