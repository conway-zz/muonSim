/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import hep.aida.*;
import java.io.*;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
/**
 *
 * @author agias
 */


public class postProcessor extends Driver{
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    
    public postProcessor(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
    }
    
    public IHistogram1D sumHist(IHistogram1D data1, IHistogram1D data2){
        return this.hf.add("Sum of event energies", data2, data2);
    }
    
    //Produce integral 'histogram' of selected histogram
    //print when integral reaches threshhold (range [0,1]) percentage of maximum
    //maximum generally should be the value of the histogram integrated
    //over all time
    public IHistogram1D integratedHist(IHistogram1D hist, double threshhold){    
        
        boolean isAboveThresh = false;
        boolean hitYet = false;
        double firstTime=0;
        
        IHistogram1D intHist = this.hf.createHistogram1D("int"+hist.title(), 
                hist.title()+", integrated", 
                hist.axis().bins(), 
                hist.axis().lowerEdge(), 
                hist.axis().upperEdge());
        
        double maximum = hist.sumAllBinHeights();
        
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
    
    //
    public IHistogram1D windowEfficiency(IHistogram1D hist){
        double mean = hist.mean();
        int bins = 200;
        double maxwin = 2*Math.min(
                mean-hist.axis().lowerEdge(), 
                hist.axis().upperEdge()-mean);
        IHistogram1D windowEff = this.hf.createHistogram1D(
                "Values of integrals based on time windows about mean "+hist.title(),
                "windowEff "+hist.title(), 
                bins,0,maxwin);
        
        
        for(int i=0; i<bins; i++){
            hist.fill(
                    i*maxwin/bins,
                    windowInt(
                            hist, 
                            mean - i*maxwin/(2*bins),
                            mean + i*maxwin/(2*bins)));
        }
        return windowEff;
    }
    
    public double windowInt(IHistogram1D hist, double min, double max){
        
        int bins = hist.coordToIndex(max)-hist.coordToIndex(min);
        double sum = 0;
        int index = hist.coordToIndex(min);
        for(int i=0; i<bins ; i++){
            index += i;
            sum += hist.binHeight(index);
        }
        return sum;
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
