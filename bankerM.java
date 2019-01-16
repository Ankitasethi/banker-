import java.io.*;
import java.util.*;
public class bankerM {

    public static void main(String[] args) {
    	Scanner sc=new Scanner(System.in);
        String fname;
        //System.out.println("Hello! Please enter valid input file name.");
        fname=args[0]; //input file name 

        Scheduler sOp=new Scheduler(); //creates new instance of scheduler for optimistic case
       ( sOp.abortedButNotFree).clear();
        sOp.read(fname); // read input file 
        while(sOp.oneCycle(true)>0);
        
        Scheduler sBank=new Scheduler(); //new instance of scheduler for using banker's algorithm
        (sBank.abortedButNotFree).clear();
        sBank.read(fname);
        sBank.checkBanker();
        while(sBank.oneCycle(false)>0);
        
        int workOp=0, workBank=0;
        int waitOp=0, waitBank=0;
        System.out.printf("%12s%30s\n", "FIFO","BANKER'S");
        for(int i=0;i<sBank.getNumberOfTask();i++)
        {
            
            if(sOp.isTaskAborted(i))
            {
            	System.out.printf("%11s%2d%13s","Task",(i+1),"aborted");
            }
                else
                {
                //System.out.print("Task " + (i+1) + " "+ +" "+((float)100.*(sOp.getTimeWait(i))/(sOp.getTimeTotal(i))));
                System.out.printf("%11s%2d%4d%4d%4.0f%%","Task",(i+1),sOp.getTimeTotal(i),sOp.getTimeWait(i),(float)100.*(sOp.getTimeWait(i))/(sOp.getTimeTotal(i)));
                waitOp +=sOp.getTimeWait(i);
                workOp+=sOp.getTimeTotal(i);
                }
            if(sBank.isTaskAborted(i))
            	//System.out.println("Task " + (i+1)+ " aborted");
                System.out.printf("%11s%2d%9s%4s\n","Task",(i+1),"aborted"," ");
            else
                {
                waitBank+=sBank.getTimeWait(i);
                workBank+=sBank.getTimeTotal(i);
                //System.out.println("Task "+ (i+1)+" " + sBank.getTimeTotal(i) + " "+sBank.getTimeWait(i)+ " "+ 100.*sBank.getTimeWait(i)/sBank.getTimeTotal(i));
                System.out.printf("%11s%2d%4d%4d%4.0f%%\n","Task",(i+1),sBank.getTimeTotal(i),sBank.getTimeWait(i),100.*sBank.getTimeWait(i)/sBank.getTimeTotal(i));            
                }
        }
        System.out.printf("%12s %4d%4d%4.0f%%","total",workOp,waitOp,100.*waitOp/workOp);
        System.out.printf("%12s %4d%4d%4.0f%%\n","total",workBank,waitBank,100.*waitBank/workBank);
    }
    
}
