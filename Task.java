/* task class which contains all possible activities for a given task that has to be scheduled
*/
import java.util.*;
public class Task {
    List<Activity> subtasks;
    int idTask;
    int timeTaken;
    int timeWait;
    int currentTaskIndex;
    boolean IsAborted;
    int countUsed[];
    int maximumPossibleResource[];
    int timeCompute;
    public Task(int _id, int _numberTypeResource)
    {
        idTask=_id;
        subtasks=new ArrayList<>();
        timeTaken=0;
        timeWait=0;
        currentTaskIndex=-1;
        IsAborted=false;
        countUsed=new int[_numberTypeResource];
        for(int i=0;i<_numberTypeResource;i++)
            countUsed[i]=0;
        maximumPossibleResource=new int[_numberTypeResource];
        for(int i=0;i<_numberTypeResource;i++)
           maximumPossibleResource[i]=0;
        timeCompute=0;
    }
     public void addActivity(String _type, int _resource, int _amount) throws Exception
    {   
        Activity newAct=new Activity(idTask, _type, _resource, _amount);
        subtasks.add(newAct);
        if(currentTaskIndex==-1)
          currentTaskIndex=0;
        if(newAct.type==Activity.typeActivity.initiate)
            maximumPossibleResource[_resource-1]=_amount;
    }
    public void increaseTimeWait()
     {
         timeWait++;        

     }
    
    
    
    public void doTask()
    {
        Activity t=subtasks.get(currentTaskIndex);
        if(currentTaskIndex>=0)
          if(t.type!=Activity.typeActivity.terminate)
          {
              timeTaken++;
              if(t.type==Activity.typeActivity.release)
                  countUsed[t.resourceNumber-1]-=t.amount;
              else
                  if(t.type==Activity.typeActivity.request)
                     countUsed[t.resourceNumber-1]+=t.amount;
                  else
                     if(t.type==Activity.typeActivity.compute)
                      {
                      timeCompute++;
                      if(timeCompute<t.amount)  //stay on same position:
                          currentTaskIndex--;
                      else
                          timeCompute=0;
                      }
                           
          }        
        currentTaskIndex++;
        if (currentTaskIndex>=subtasks.size())
            currentTaskIndex=-1;
    }
    
    public void increaseTimeTaken()
    {
        Activity t=getCurrentActivity();
        if(t!=null)
            if(t.type!=Activity.typeActivity.terminate)
                timeTaken++;        

    }
    public int getMaxPossible(int idResource)
    {
        return maximumPossibleResource[idResource];
    }
    
    public int getUsedNow(int idResource)
    {
        return countUsed[idResource];
    }
   
   public Activity getCurrentActivity()
   {   
       if(currentTaskIndex>=0)
           return subtasks.get(currentTaskIndex);
       else
           return null;
   }
    
    public int[] abort()
    {
        IsAborted=true;
        return countUsed;
    }
}
