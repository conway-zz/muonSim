/*
 * Interface for processing any set of data
 * Add functions/init variables from new analysis classes here
 * class processor will implement process and run processXX
 * implemented by other classes
 * Also add folders if making plots
 */
import java.util.List;
import hep.aida.*;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;

public interface datasetProcessor {
     
    /*
    ###datasetStats functions###
    */
    
    //creates ROOT.aida
    public void initROOT();
    
    //makes ITree called branch in root/branch
    public void initTree(ITree root, String branch);
    
    //initialize FOLDERS at first process() call
    public void runOnce();
    
    //creates a CSV file from histogram1D
    //TODO: get histToCSV working
     public void histToCSV(IHistogram1D hist);
     
     //Produce integral 'histogram' of selected histogram
     public IHistogram1D integratedHist(IHistogram1D hist, IHistogramFactory hf,
            double maximum, double threshhold);
    
    /*
    ###groupStats
     */
    
    
    /*
     ###timingAndEnergyFns###
     */
    
    //local threshhold variable, this value seems to work for now
    public double stdthresh = .98;
    
    //get total energy detected in event
    public double totalEnergy(EventHeader event);
    
    //returns first hit time.  Used in eventTimer
     public double timeFirstHit(EventHeader event);
     
     //Get time window for calorimeter hits in thresh of energy, returns 
     //number of hits counted as well (rv[3]++)
     public double[] calTimer(List<SimCalorimeterHit> hitcol, double thresh);
     
    //takes event and timing window, integrates E over dt.
    //window[lowerbound, upperbound]
    public double windowEnergy(EventHeader event, double[] window);
    
    //simply determines when event started (raw time)
    public double[] eventTimer(EventHeader event, double threshhold);
    
    //returns the time of the last hit in the event. 
    public double timeLastHit(EventHeader event);
        
    //gets r/c time as described, c in mm/ns
    public double getRC(SimCalorimeterHit hit);
    
    //calculate radius from double [x,y,z]
    public double getAbsRadius(double[] pos);
    
    
   
    
    
    
    
    
    
    
    
    
    
}
