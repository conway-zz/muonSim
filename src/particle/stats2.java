import org.lcsim.util.aida.AIDA;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import java.util.List;
import hep.aida.*;
/**
 * Another graphing program because stats 
 * is getting messy and a little misdirected.
 * @author agias
 */
public class stats2 extends timingAndEnergyFns {
    
    private AIDA aida = AIDA.defaultInstance();
    
    protected void process(EventHeader event) {
        
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
        
        
        
        //count hits in event
        //sum energy detected
        double E_cal = totalEnergy(event);
        //int calhits = 0;
        //System.out.println(calhits);
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        int calhits = 0;
        for(int i=0; i<myHitCol.size(); i++) {
            List<SimCalorimeterHit> myCols = myHitCol.get(i);
            for(int j=0; j<myCols.size(); j++){
                calhits++;
            }
        }
        histograms(myHitCol, calhits, hf, E_cal);
        
        
    }
    
    
    private void histograms(List<List<SimCalorimeterHit>> myHitCol, int calhits, 
            IHistogramFactory hf, double E_cal) {
         
        
        //Spawn all histograms to be created before for loop
        
         IHistogram1D hTcals = hf.createHistogram1D("hTcals", "Histogram of T-cals", 
                 calhits/10,4.2,4.5);
         IHistogram1D hrc = hf.createHistogram1D("hRC", "Histogram of r/cs",
                 calhits/10,4.2,4.5);
         IHistogram1D hTprimes = hf.createHistogram1D("hTprimes", "Histogram of T-Primes",
                 calhits,-0.0125,0.04);
         IHistogram1D tpEn = hf.createHistogram1D("tpEn", "Histogram of T-Primes weighted by energy",
                 calhits,-0.0125,0.04);
         IHistogram1D hRadii = hf.createHistogram1D("hRadii", "Histogram of Radii",
                 calhits,1200,1900);
         IHistogram1D tcRad = hf.createHistogram1D("tcRad","Histogram of T-cals weighted by radius",
                 calhits,4,4.8);
         IHistogram1D trcRad = hf.createHistogram1D("trcRad", "Histogram of r/c's weighted by radius",
                 calhits,4,4.8);
         IHistogram1D tcEn = hf.createHistogram1D("tcEn", "Histogram of T-cals weighted by Energy",
                 calhits,4.2,4.5);
         IHistogram1D trcEn = hf.createHistogram1D("tprcEn", "Histogram of r/c's weighted by Energy",
                 calhits,4.2,4.5);
         IHistogram1D rEn = hf.createHistogram1D("rEn", "rad by E",
                 calhits,1200,1900);
         
         for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){

                
                //Plot histogram of t-cals  
                hTcals.fill(myhit.getTime());

                
                //Plot histogram of r/c's
                hrc.fill(getCorrectTime(myhit));
             
                
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
                trcRad.fill(getCorrectTime(myhit), 
                                             getAbsRadius(myhit.getPosition()));
                
                //Plot histogram of T-cals weighted by Energy
                tcEn.fill(myhit.getTime(), myhit.getRawEnergy());
                
                //Plot histogram of T-primes weighted by Energy
                tpEn.fill(getCorrectTime(myhit), myhit.getRawEnergy());
                
                //rEn
                rEn.fill(getAbsRadius(myhit.getPosition()), myhit.getRawEnergy());
                
            }
         }
            
            //now integrate selected histograms
            
            //IHistogram1D intTcals = integratedHist(hTcals, hf);
            
            //IHistogram1D intTprimes = integratedHist(hTprimes, hf);
            
            IHistogram1D intTcEn = integratedHist(tcEn, hf, E_cal, .95);
            
            IHistogram1D intTpEn = integratedHist(tpEn, hf, E_cal, .95);
            
            IHistogram1D intTprimeEn = integratedHist(tpEn, hf, E_cal, .95);
            
            /*
            System.out.println("Threshhold: 95% of detectable energy");
            System.out.println("T-cal thresh time: "
                    +integratedHistThresh(tcEn, hf, E_cal, .95));
            
            System.out.println("T-prime thresh time: "
                    +integratedHistThresh(tpEn, hf, E_cal, .95));
             * 
             */
            
             
         
         
         
    }
    


    
    private IHistogram1D integratedHist(IHistogram1D hist, IHistogramFactory hf,
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
    
    //Find when threshold percentage of maximum has been reached.
    private double integratedHistThresh(IHistogram1D hist, IHistogramFactory hf,
            double maximum,
            double thresh){
        
        IHistogram1D intHist = hf.createHistogram1D("int"+hist.title(), 
                hist.title()+", integrated", 
                hist.allEntries(), hist.axis().lowerEdge(), hist.axis().upperEdge());
        double sum = 0;
        for(int i=0; i<hist.entries() ; i++){
            sum += hist.binHeight(i); 
            double xvalue = i*(hist.axis().upperEdge()-hist.axis().lowerEdge())/hist.entries() + hist.axis().lowerEdge();
            if(sum>(thresh*maximum)){
                return xvalue;
            }            
            intHist.fill(xvalue,sum);
        }
        System.out.println("integratedHistThresh received value "
                + "for maximum that was too high");
        return thresh;
    }
    
    private void histToCSV(IHistogram1D hist){
        
        
    }
}
    
    
        
    
