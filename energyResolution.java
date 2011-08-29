/*
 * Want: Width and mean of energy distributions
 * given sliding scale of time cuts.
 */
import java.util.List;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.MCParticle;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import hep.aida.*;
import java.util.List;
import java.io.*;
/**
 *
 * @author agias
 */
public class energyResolution extends Driver {
     IAnalysisFactory af;
     ITree TREE;
     IHistogramFactory hf;
     //IHistogram1D totalEnergies;
     IHistogram1D cutEnergies;
     IHistogram2D cutEnergies2D;
     IHistogram1D meanEnergies;
     IHistogram1D widthEnergies;
     IHistogram1D tempEnergies;
     int bins;
     double min;
     double max;
     double EVENTS;
    
    protected void startOfData(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().create();
        hf = af.createHistogramFactory(TREE);
        bins = 200;
        min = 0;
        max = 1000;
        cutEnergies = hf.createHistogram1D(
                "cutEnergies",
                bins,min,max);
        cutEnergies2D = hf.createHistogram2D(
                "cutEnergies2D",
                bins,min,max,
                100,0,10);
    }
    protected void process(EventHeader event){
        EVENTS++;
        
        List<List<SimCalorimeterHit>> hitcol = event.get(SimCalorimeterHit.class);
        
        double en = 0;
        //double time = 0;
        for(double i=0; i<1000; i+=5){
            for(List<SimCalorimeterHit> hits : hitcol){
                for(SimCalorimeterHit hit : hits){
                    if(getTPrime(hit)<i){
                        en+=hit.getRawEnergy();
                    }
                }
            }
            cutEnergies.fill(
                    i,en/10000);
            cutEnergies2D.fill(
                    i,en);
            en = 0;
        }
    }
    protected void endOfData(){
        
        meanEnergies = hf.createHistogram1D(
                "meanEnergies",
                cutEnergies.axis().bins(),
                cutEnergies.axis().lowerEdge(),
                cutEnergies.axis().upperEdge());
        widthEnergies = hf.createHistogram1D(
                "widthEnergies",
                cutEnergies.axis().bins(),
                cutEnergies.axis().lowerEdge(),
                cutEnergies.axis().upperEdge());
        
        for(int i=0; i<cutEnergies2D.xAxis().bins(); i++){
            
            tempEnergies = hf.createHistogram1D(
                "tempEnergies",
                1,0,1);
            tempEnergies = hf.createHistogram1D(
                "tempEnergies",
                cutEnergies2D.yAxis().bins(),
                cutEnergies2D.yAxis().lowerEdge(),
                cutEnergies2D.yAxis().upperEdge());
             
            for(int j=0; j<cutEnergies2D.yAxis().bins(); j++){
                    tempEnergies.fill(
                            cutEnergies2D.yAxis().binCenter(j),
                            cutEnergies2D.binHeight(i,j));
            }
                
            meanEnergies.fill(
                    cutEnergies2D.xAxis().binCenter(i),
                    tempEnergies.mean());
            widthEnergies.fill(
                    cutEnergies2D.xAxis().binCenter(i),
                    tempEnergies.rms());            
        }
        
    }
    
    public double getRadius(double[] point){
         return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
     }
     public double getTPrime(SimCalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
}
