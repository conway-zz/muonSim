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
    IHistogram1D totalEnergies;
    
    protected void startOfData(){
        TREE = af.createTreeFactory().create();
        totalEnergies = hf.createHistogram1D(
                "totalEnergies",
                200,0,10);
    }
    protected void process(EventHeader event){
        
        double detectableEnergy = 0;
        
        //event energy as function of time cut
        IDataPointSet enOverTime = af.createDataPointSetFactory(TREE).create(
                "enOverTime",
                "enOverTime",
                3);
        int index=0;
        List<List<SimCalorimeterHit>> hitcol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> hits : hitcol){
            for(SimCalorimeterHit hit : hits){
                
                index++;
                detectableEnergy += hit.getRawEnergy();
                
                enOverTime.addPoint();
                enOverTime.point(index).coordinate(0).setValue(hit.getTime());
                enOverTime.point(index).coordinate(1).setValue(hit.getRawEnergy()); 
                enOverTime.point(index).coordinate(2).setValue(getTPrime(hit));
            }
        }
        
        totalEnergies.fill(detectableEnergy);
        //fill histograms: Mean as function of cutoff
        //                 Width (RMS for now) " " " "
        
    }
    protected void endOfData(){
        
    }
    
    public double getRadius(double[] point){
         return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
     }
     public double getTPrime(SimCalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
}
