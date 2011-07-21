import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import java.util.List;
import hep.aida.*;

/**
 * A couple functions to get statistics from a single event,
 * grouping all calorimetric data together.
 * Not sure if histToCSV is working yet
 * 
 * @author agias
 */
public abstract class groupStats extends timingAndEnergyFns implements datasetProcessor{
    
    protected void processGroup(EventHeader event) {
        
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = null;
        //creates tree.aida file in runtime directory which stores all 
        //histograms passed to it, so they may be universally accessed.
        try{
            tree = af.createTreeFactory().create("tree.aida","xml",false,true);
            //tree will overwrite objects created with same path
            tree.setOverwrite();
        }
        catch (java.io.IOException e) {
            System.err.println("could not create tree.aida file.  Sowwy =("+
                    e.getMessage());          
        }
        IHistogramFactory hf = af.createHistogramFactory(tree);
        
        //cut off the last events that add the last 1%
        double[] window = eventTimer(event, .99);
        
        //count hits in event
        //sum energy detected
        int calhits = 0;
        double E_cal = totalEnergy(event);
        double t_cal_max = 0;
        double t_cal_min = 10000000;
        double rmax = 0;
        double rmin = 10000000;
        double rcmax = 0;
        double rcmin = 10000000;
        double tpmax = 0;
        double tpmin = 10000000;
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){
                
                if(myhit.getTime()<window[1]){
                    
                    System.out.println(myhit.getSystemId());
                    t_cal_max = Math.max(t_cal_max, myhit.getTime());
                    t_cal_min = Math.min(t_cal_min, myhit.getTime());
                    rmax = Math.max(rmax, getAbsRadius(myhit.getPosition()));
                    rmin = Math.min(rmin, getAbsRadius(myhit.getPosition()));
                    rcmax = Math.max(rcmax, getRC(myhit));
                    rcmin = Math.min(rcmin, getRC(myhit));
                    tpmax = Math.max(tpmax, myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                    tpmin = Math.min(tpmin, myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                
                    calhits++;
                }
                
            }
            
        }
        System.out.println(t_cal_max);
        System.out.println(t_cal_min);
        System.out.println(rmax);
        System.out.println(rmin);
        System.out.println(rcmax);
        System.out.println(rcmin);
        System.out.println(tpmax);
        System.out.println(tpmin);
         
        
        //Spawn all histograms to be created before for loop
        
         IHistogram1D hTcals = hf.createHistogram1D("hTcals", "Histogram of T-cals", 
                 calhits,t_cal_min,t_cal_max);
         IHistogram1D hrc = hf.createHistogram1D("hrc", "Histogram of rcs",
                 calhits,rcmin,rcmax);
         IHistogram1D hTprimes = hf.createHistogram1D("hTprimes", "Histogram of T-Primes",
                 calhits,tpmin,tpmax);
         IHistogram1D tpEn = hf.createHistogram1D("tpEn", "Histogram of T-Primes weighted by energy",
                 calhits,tpmin,tpmax);
         IHistogram1D hRadii = hf.createHistogram1D("hRadii", "Histogram of Radii",
                 calhits,rmin,rmax);
         IHistogram1D tcRad = hf.createHistogram1D("tcRad","Histogram of T-cals weighted by radius",
                 calhits,t_cal_min,t_cal_max);
         IHistogram1D trcRad = hf.createHistogram1D("trcRad", "Histogram of rc's weighted by radius",
                 calhits,rcmin,rcmax);
         IHistogram1D tcEn = hf.createHistogram1D("tcEn", "Histogram of T-cals weighted by Energy",
                 calhits,t_cal_min,t_cal_max);
         IHistogram1D trcEn = hf.createHistogram1D("tprcEn", "Histogram of rc's weighted by Energy",
                 calhits,rcmin,rcmax);
         IHistogram1D rEn = hf.createHistogram1D("rEn", "rad by E",
                 calhits,rmin,rmax);
         
         for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){

                if(myhit.getTime()<window[1]){
                
                //Plot histogram of t-cals  
                hTcals.fill(myhit.getTime());

                
                //Plot histogram of r/c's
                hrc.fill(getRC(myhit));
             
                
                //Plot histogram of t-primes
                hTprimes.fill(myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                
                //Plot histogram of t-primes weighted by energy
                tpEn.fill(myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458,
                        myhit.getRawEnergy());
                
                //Plot histogram of radii
                hRadii.fill(getAbsRadius(myhit.getPosition()));

                
                //Plot histogram of t-cals weighted by radius
                tcRad.fill(myhit.getTime(), 
                                          getAbsRadius(myhit.getPosition()));
                
                //Plot histogram of t-primes weighted by radius
                trcRad.fill(getRC(myhit), 
                                             getAbsRadius(myhit.getPosition()));
                
                //Plot histogram of T-cals weighted by Energy
                tcEn.fill(myhit.getTime(), myhit.getRawEnergy());
                
                //Plot histogram of T-primes weighted by Energy
                trcEn.fill(getRC(myhit), myhit.getRawEnergy());
                
                //rEn
                rEn.fill(getAbsRadius(myhit.getPosition()), myhit.getRawEnergy());
                }
                
            }
         }
            
            //now integrate selected histograms

            
            IHistogram1D intTcEn = integratedHist(tcEn, hf, E_cal, .95);
            
            IHistogram1D intrcEn = integratedHist(trcEn, hf, E_cal, .95);
            
            IHistogram1D intTprimeEn = integratedHist(tpEn, hf, E_cal, .95);
            
            histToCSV(intTprimeEn);
         
    }
    


   
}
    
    
        
    
