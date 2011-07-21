/*
 * Uses techniques developed in group and sepStats to process 
 * entire datasets and produce information about E-MC, E-Cal and t'
 * for extending subclasses (procEM, procH, ect)
 * @author Alex Conway
 */
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import java.util.List;
import hep.aida.*;
import java.io.*;
import org.lcsim.event.MCParticle;
import java.lang.String;


public abstract class datasetStats implements datasetProcessor{
    
    
    
    /*
    //Initialize dataset creation
    
    //ROOT subfolders to create in runOnce
    //Each folder name will be the same as the 
    String[] FOLDERS = {"GROUP","EM","H"};
    
    //safely create ROOT tree for all data collectors
    //to be run from runOnce in processor
    public void initROOT(){
        try{
            ROOT = af.createTreeFactory().create("ROOT.aida","xml",false,true);
            //tree will overwrite objects created with same path
            ROOT.setOverwrite();
        }
        catch (java.io.IOException e) {
        System.err.println("could not create ROOT.aida file.  Sowwy =("+
                    e.getMessage());          
        }
    }
    
    //Initialize ITree branch, mount as root/branch/
    //Allows modular method for easily creating any new trees
   
    public void initTree(ITree root, String branch){
        ITree temp = null;
        try{
            temp = af.createTreeFactory().create(branch+".aida","xml",false,true);
            //tree will overwrite objects created with same path
            temp.setOverwrite();
        }
        catch (java.io.IOException e) {
        System.err.println("could not create "+branch+".aida file.  Sowwy =("+
                    e.getMessage());          
        }
        root.mkdir(branch+"/");
        root.mount(branch+"/",temp,"/");
    }
    
    //Initialize any ITrees, variables, etc needed before running 
    //anything else in process().  Check EVENTS == 0 in process()
    //to run for efficiency
    public void runOnce(){
        initROOT();
        for(String folder : FOLDERS){
            initTree(ROOT, folder);
        }        
    }
    */
    

     //Produce integral 'histogram' of selected histogram
    //print when integral reaches threshhold (range [0,1]) percentage of maximum
    //maximum generally should be the value of the histogram integrated
    //over all time
    public IHistogram1D integratedHist(IHistogram1D hist, IHistogramFactory hf,
            double maximum, double threshhold){    
        
        boolean isAboveThresh = false;
        boolean hitYet = false;
        double firstTime=0;
        
        IHistogram1D intHist = hf.createHistogram1D("int"+hist.title(), 
                hist.title()+", integrated", 
                hist.axis().bins(), 
                hist.axis().lowerEdge(), 
                hist.axis().upperEdge());
        
        double sum = 0;
        for(int i=0; i<hist.axis().bins() ; i++){
            double xvalue = hist.axis().binCenter(i);
            sum += hist.binHeight(i);            
            
            intHist.fill(xvalue,sum);
            if(!hitYet){
                if(sum>0){
                    hitYet=true;
                    firstTime=xvalue;
                }
            }
            if(!isAboveThresh) {
                if(sum>maximum*threshhold){
                    isAboveThresh = true;
                    System.out.println(intHist.title());
                    System.out.println("Threshhold: "+threshhold);
                    System.out.println("Threshhold reached at "+
                            (xvalue-firstTime));
                }
            }
            
        }
        
        return intHist;
    }
    
    
    //creates a CSV file from histogram1D
    //uses weighted mean of bin for x-axis, height for y
    //TODO: get histToCSV working
    public void histToCSV(IHistogram1D hist){
        try{
            // Create file 
            FileWriter fstream = new FileWriter("csv/"+hist.title()+".csv");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(hist.title()+" in CSV format\n");
            out.write("Entries: "+hist.axis().bins()+"\n");
            out.write("Low: "+hist.axis().lowerEdge()+"\n");
            out.write("High: "+hist.axis().upperEdge()+"\n");
            out.write("Mean: "+hist.mean()+"\n");
            out.write("RMS: "+ hist.rms()+"\n\n");
            
            for(int i=0; i<hist.axis().bins(); i++){
                out.write(Double.toString(hist.binMean(i))+","
                         +Double.toString(hist.binHeight(i))+"\n");
            }
            out.close();
        } catch (Exception e){ //Catch exception if any
            System.err.println("histToCSV Error: " + e.getMessage());
        }
        
    }
     
    
    
}
