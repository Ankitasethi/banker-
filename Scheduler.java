import java.io.*;
import java.util.*;
public class Scheduler {
    Task []tasks;
    int T;//the number of tasks
    int R; //number of resource types
    int []resourceUnits; // the number of units present of each resource type
    int []busyUnits; // the number of units busy 
    ArrayList<Integer> waitForResource; 
    public ArrayList<Integer> abortedButNotFree=new ArrayList<Integer>(); // because resources are freed next cycle
    public ArrayList<Integer> abortedButNotFree2=new ArrayList<Integer>();
    public  Scheduler()
    {
        T=0;
        R=0;        
    }
    public int getNumberOfTask()
    {
        return T;
    }
    
    int totalResources;
    
    // read information from file, return true if all correct
    public boolean read(String fname) 
    {
        int taskNumber,idResource, amount;
        
        try {
            BufferedReader in;
            in = new BufferedReader(new FileReader(fname));
            String str;                
            str = in.readLine(); //read data of the first line:
            String[] st = str.split(" ");
            T=Integer.parseInt(st[0]); //read number of tasks
            R=Integer.parseInt(st[1]); //read number of resources

            resourceUnits=new int[R];
            busyUnits=new int[R]; 
            tasks=new Task[T]; 
            
            for(int i=0;i<R;i++)
            {
                busyUnits[i]=0;
                resourceUnits[i]=Integer.parseInt(st[i+2]);
                totalResources+=resourceUnits[i];
            }
            //waiting list
            waitForResource=new ArrayList<>(); //can't be more than the number of tasks
            
            //
            for(int i=0;i<T;i++)
                tasks[i]=new Task(i+1,R);
            //read tasks:            
            int m=1;
            String old;
            while ((str = in.readLine()) != null) {
                if(str.length()==0)
                    continue;
                str=str.replaceAll("\t", " ");
                old=str;
                str=str.replaceAll("  ", " ");
                while(old.length()!=str.length())
                {
                    old=str;
                    str=str.replaceAll("  ", " ");
                }
                String[] temp = str.split(" ");
                taskNumber=Integer.parseInt(temp[1]);
                idResource=Integer.parseInt(temp[2]);
                amount=Integer.parseInt(temp[3]);
                tasks[taskNumber-1].addActivity(temp[0], idResource, amount);
                m++;
                }
            } 
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private boolean checkPosibilityOptimistic(Activity c){
        if(c.amount<=resourceUnits[c.resourceNumber-1]-busyUnits[c.resourceNumber-1])
        {                                

            busyUnits[c.resourceNumber-1]+=c.amount;
            tasks[c.taskNumber-1].doTask();                            
            return true;

        }
        else
        {
            tasks[c.taskNumber-1].increaseTimeWait();            
        }
        return false; 
    }
    
    //check if unsafe for banker
   /*private boolean isUnsafe(Activity c)
    {
    	boolean safe=true;
    	boolean finish=true;
    	int k=c.resourceNumber-1;
        //find initiate resource for task (which not is wait for resource):
        //find all requirements
       
        for(int i=0;i<T;i++)  
        {	if(!tasks[i].IsAborted) 
        			if (tasks[i].getMaxPossible(k)-tasks[i].getUsedNow(k)>resourceUnits[k]-busyUnits[k])
        					{
        						finish=false;
        						}
        			
        			if(finish==false)
        				safe=false;
        			if(tasks[c.taskNumber-1].getCurrentActivity().type==Activity.typeActivity.request)
        				if (c.amount- tasks[c.taskNumber-1].getUsedNow(c.resourceNumber-1) > tasks[c.taskNumber-1].getMaxPossible(c.resourceNumber-1))
                		{
                			System.out.printf("  Task %d's request exceeds its claim; aborted; %d units available next cycle\n",c.taskNumber,tasks[c.taskNumber-1].getUsedNow(c.resourceNumber-1));
                            tasks[c.taskNumber-1].abort();
                            abortedButNotFree.add(c.taskNumber-1);
                        	//if(c.amount > resourceUnits[c.resourceNumber]-busyUnits[c.resourceNumber])
                        		safe=true;    
                		}
        				
        }
        return safe;
    }*/
    private boolean isUnsafe(Activity c)
    {
        int k=c.resourceNumber-1;
        int maxCanBeUsed=0;
    //check whether its safe by checking the claims as well as current conditions
        for(int i=0;i<T;i++)            
            if(!tasks[i].IsAborted) 
                maxCanBeUsed+=tasks[i].getMaxPossible(k);
        if(!waitForResource.isEmpty())
        { for(Integer i:waitForResource)
                if(c.taskNumber-1!=i)
                  maxCanBeUsed-=tasks[i].getMaxPossible(k);}
               
        Task t=tasks[c.taskNumber-1];
        if(t.getCurrentActivity().type==Activity.typeActivity.request)
        { if(t.getCurrentActivity().amount+t.getUsedNow(k)>t.getMaxPossible(k))           
             {              
              System.out.printf(" Task %d's request exceeds its claim; aborted; %d units available next cycle\n",c.taskNumber,t.getUsedNow(k));
              //only one guys 
              //tasks[c.taskNumber-1].abort();
              abortedButNotFree.add(c.taskNumber-1);
             }}
      if((t.getMaxPossible(k)-t.getUsedNow(k))>(resourceUnits[k]-busyUnits[k]))
          return true;
      return false;
    }	
    private boolean checkPossibilityBank(Activity c){
        if(!isUnsafe(c))
        {   
        	busyUnits[c.resourceNumber-1]+=c.amount;
			tasks[c.taskNumber-1].doTask();
			return true;
        	
        }
        else
        	tasks[c.taskNumber-1].increaseTimeWait();            
        return false;
        }
  
    //one cycle
    public int oneCycle(boolean isOptimistic)
    {
    
        int countChanges=0;
        int r=0;
        int []wasUsedInCycle=new int[T];
        int []beFree;
        beFree=new int[R];
        for(int i=0;i<R;i++)
            beFree[i]=0;
        for(int i=0;i<T;i++)
            wasUsedInCycle[i]=0;
        //check for tasks which wait for resource and mark them as being used in current cycle
        for(Integer i:waitForResource)
            wasUsedInCycle[i]=1;
      
        //free resources

        for(int tIndex:abortedButNotFree)
        {
            int[] freeResource=tasks[tIndex].abort();                    
            for(int j=0;j<R;j++)
               busyUnits[j]-=freeResource[j];
        }
        
        
        //on start of cycle clear list of aborted process
        abortedButNotFree.clear();
       
        //check waitlist to see if anyone can come off of it
        if(!waitForResource.isEmpty())
            for(Integer i:waitForResource)
            {                
                Activity c=tasks[i].getCurrentActivity();
                /*if(c==null)
                {
                    wasUsedInCycle[i]=-1; //for removed
                    waitForResource.remove(i);
                }*/
                if(c.type==Activity.typeActivity.request)    
                   if(isOptimistic) // if it's the optimistic manager's turn
                   {
                	   if(checkPosibilityOptimistic(c)) {
	                       countChanges++;
	                       wasUsedInCycle[i]=-1;
	                   } //fulfills request if possible 
                	  
                	   if(waitForResource.indexOf(c.taskNumber-1)<0)
                        waitForResource.add(c.taskNumber-1);  //add to waitlist if task is requesting something in that cycle
                   }
                   else //in case it's banker's turn i.e. isOptimistic = false
                   { if(checkPossibilityBank(c)) //if banker can do it 
                       {
                           countChanges++;
                           wasUsedInCycle[i]=-1;
                           
                       }
                   if(waitForResource.indexOf(c.taskNumber-1)<0)
                       waitForResource.add(c.taskNumber-1); 
                   
                   }
                
            }
        //if already been used, remove from waitlist
        for(int i=T-1;i>=0;i--)
            if(wasUsedInCycle[i]==-1)            
                waitForResource.remove(waitForResource.indexOf(i));
           
      
        
        for(int i=0;i<T;i++)
            if(wasUsedInCycle[i]==0) //tasks that have neither been used and nor cant be used
                if(!tasks[i].IsAborted) //only for tasks which were not aborted
                {
                Activity c=tasks[i].getCurrentActivity();

                if(c==null)
                    continue;

                if((c.type==Activity.typeActivity.initiate)||(c.type==Activity.typeActivity.terminate)||(c.type==Activity.typeActivity.compute))
                {
                    tasks[i].doTask();
                    countChanges++;
                }
                else
                    if(c.type==Activity.typeActivity.release)
                    {
                        beFree[c.resourceNumber-1]+=c.amount;
                        tasks[i].doTask();
                        countChanges++;
                        //x=true;
                        
                        //System.out.println("task "+i+" release "+c.amount+" units resource");
                    }
                    else
                       if(c.type==Activity.typeActivity.request)   { 
                          //if(c.amount<=resourceUnits[c.resourceNumber-1]-busyUnits[c.resourceNumber-1])

                              if(isOptimistic)
                                {if(checkPosibilityOptimistic(c))
                                     countChanges++;
                                else
                                    if(waitForResource.indexOf(c.taskNumber-1)<0)
                                waitForResource.add(c.taskNumber-1);
                                }
                                else
                                {
                                    if(checkPossibilityBank(c))
                                    {	//tasks[c.taskNumber].increaseTimeWait();
                                    	countChanges++;
                                    }
                                    else
                                        if(waitForResource.indexOf(c.taskNumber-1)<0)
                                          waitForResource.add(c.taskNumber-1);  } }
                }
        
        
      
       /* for(int i=0;i<T;i++)
            if (tasks[i].currentTaskIndex==-1)
            	for(int j=0;i<T;i++)
            		if(tasks[i].IsAborted!=true)
            			tasks[i].increaseTimeWait();
            		
        /*add aborted resources to be free array so they can be freed at end of next cycle
        for(int tIndex:abortedButNotFree)
        {
            int[] freeResource=tasks[tIndex].abort();                    
            for(int j=0;j<R;j++)
            	beFree[j]+=freeResource[j];
          
            
        } */      
        // to see if deadlocked. if deadlocked, abort first task(if not resolved, the second (first after the first is aborted) will be aborted and so one
      
        for(int i=0;i<R;i++)
            busyUnits[i]-=beFree[i];
        if(countChanges==0)
        {
            for(int i=0;i<T;i++)
            {
                if(!tasks[i].IsAborted)
                {
                Activity c=tasks[i].getCurrentActivity();
                if(c==null)
                    continue;
                if(c.resourceNumber>0)                    
                    if(c.amount  >resourceUnits[c.resourceNumber-1]-busyUnits[c.resourceNumber-1])
                      {
                        //free resources
                        //how about change this to free
                        int[] freeResource=tasks[i].abort();                    
                        for(int j=0;j<R;j++)
                            busyUnits[j]-=freeResource[j];
                        countChanges++; 
                        //remove tasks from waiting list
                        int m=waitForResource.indexOf(i);
                        if(m>=0)
                          waitForResource.remove(m);
                      }
                    }
            }
        }
        return countChanges;
    
    }
    
    
    public int getTimeWait(int numTask)
    {
        return tasks[numTask].timeWait;
    }
    
    public int getTimeTotal(int numTask)
    {
        return tasks[numTask].timeWait+tasks[numTask].timeTaken;
    }
    
    public boolean isTaskAborted(int numTask)
    {
        return tasks[numTask].IsAborted;
    }
    
    public void checkBanker()
    {
          
       
        for(int i=0;i<T;i++)
            for(int j=0;j<R;j++)
               if(tasks[i].getMaxPossible(j)>resourceUnits[j])
               {
                   System.out.printf("  Banker aborts task %d before run begins:\n",(i+1));
                   System.out.printf("       claim for resourse %d (%d) exceeds number of units present (%d)\n",(j+1),tasks[i].getMaxPossible(j),resourceUnits[j]);
                   tasks[i].abort();
                   break;
               }
    }
}

