/*
 * Class of functions that exist specifically to work with 
 * arbitrary AIDA functions.
 * If it deals with energy, timing, etc, put it somewhere else.
 * @author Alex Conway
 */
import hep.aida.*;
import java.io.*;



public abstract class aidaFunctions extends dpsToHistFunctions{
    
    //
    
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
