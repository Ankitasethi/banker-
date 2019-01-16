//activity class lists all possible activities and its associated values 
public class Activity {
       enum typeActivity {initiate, request,release,terminate,compute};
       int taskNumber;
       typeActivity type;
       int resourceNumber;
       int amount;
       Activity(int _task, String _type, int _resource, int _amount) throws Exception
       {
           taskNumber=_task; 
           if(_task<0)
               throw new Exception("Incorrect task number");
           switch(_type) {
	           case "initiate": 
	           {
	        	   type=typeActivity.initiate;
	        	   break;
	           }
	           case "request" :
	           {
	        	   type=typeActivity.request;    
	        	   break;
	           }
	           case "release":
	           {
	        	   type=typeActivity.release;            
	        	   break;
	           } 
	           case "terminate":
	           {
	        	   type=typeActivity.terminate;         
	        	   break;
	           }
	           case "compute":
	           {
	        	   type=typeActivity.compute;
	               _amount=_resource;
	               _resource=0;
	           }
	   
           }
           resourceNumber=_resource;
           amount=_amount;
        
          
       }       
}