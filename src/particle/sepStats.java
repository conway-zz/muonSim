import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import java.util.List;
import hep.aida.*;
import org.lcsim.event.MCParticle;
/**
 * Does the same stuff as groupStats but does it for EM and hadronic 
 * calorimeters separately
 * ######                                            #######
 * MUST HAVE groupStats OPENED IN JAS3 OR THIS WILL NOT COMPILE
 * ######                                            ########
 * @author agias
 */
public abstract class sepStats extends groupStats{
    
    double E_CUTOFF_PCNT = 0.99;
    
    protected void process(EventHeader event){
        System.out.println(processMC(event));
        processEMCal(event);
        processHCal(event);
    }
    
    public void processEMCal(EventHeader event) {
        
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = null;
        //creates tree.aida file in runtime directory which stores all 
        //histograms passed to it, so they may be universally accessed.
        try{
            tree = af.createTreeFactory().create("treeEM.aida","xml",false,true);
            //tree will overwrite objects created with same path
            tree.setOverwrite();
        }
        catch (java.io.IOException e) {
            System.err.println("could not create treeEM.aida file.  Sowwy =("+
                    e.getMessage());          
        }
        IHistogramFactory hf = af.createHistogramFactory(tree);
        
        //get all hits for this barrel. 
        //sysId() can return null values, so this is better
        List<SimCalorimeterHit> myHitCol = event.get(SimCalorimeterHit.class, "EcalBarrelHits");
        
        //Cut off splashback hits by energy deposition over time.  
        //Will eventually move to clusters...   
        //separate from t_cal_max so both graphs can be plotted
        double[] window = calTimer(myHitCol, E_CUTOFF_PCNT);
        
        //count hits in event
        int calhits = 0;
        //get particle's MC energy
        double MC_en = processMC(event);
        //energy detected in specified cal
        double E_cal = 0.0;
        //get stats for histogram ranges
        double t_cal_max = 0;
        double t_cal_min = 10000000;
        double rmax = 0;
        double rmin = 10000000;
        double rcmax = 0;
        double rcmin = 10000000;
        double tpmax = 0;
        double tpmin = 10000000;
        
                
            for(SimCalorimeterHit myhit : myHitCol){
                if(myhit.getTime()<window[1]){
//                    if(myhit.getSystemId() == 5){
                        E_cal += myhit.getRawEnergy();
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
  //              }
            }
        
            System.out.println("###HCal Results###");
            System.out.println("MC Energy: "+MC_en);
            System.out.println("Cal Energy: "+E_cal);
            System.out.println("Fraction detected"+(E_cal/MC_en));
            System.out.println();
            System.out.println("T-Cal range: ["+t_cal_min+", "+t_cal_max+"]");
            System.out.println("r/c Range: ["+rcmin+", "+rcmax+"]");
            System.out.println("T-Prime range: ["+tpmin+", "+tpmax+"]");
            System.out.println("Total Hits: "+calhits); 
        
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
         IHistogram1D zoomtpEn = hf.createHistogram1D("zoomtpEn", "Closeup of T-primes",
                 (int) window[3], window[0], window[1]);
         

            for(SimCalorimeterHit myhit : myHitCol){                   
                
                    //Plot histogram of t-cals  
                    hTcals.fill(myhit.getTime());

                
                    //Plot histogram of r/c's
                    hrc.fill(getRC(myhit));
             
                
                    //Plot histogram of t-primes
                    hTprimes.fill(myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                
                    //Plot histogram of t-primes weighted by energy
                    tpEn.fill(myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458,
                            myhit.getRawEnergy()); 
                    
                    //Zoomed 
                    zoomtpEn.fill(myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458,
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
                
//                }   
//            }
//         }
            
            //now integrate selected histograms            
            IHistogram1D intTcEn = integratedHist(tcEn, hf, E_cal, .95);
            
            IHistogram1D intrcEn = integratedHist(trcEn, hf, E_cal, .95);
            
            IHistogram1D intTprimeEn = integratedHist(tpEn, hf, E_cal, .95);
            
            histToCSV(intTprimeEn);
    }
    
    public void processHCal(EventHeader event) {
        
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = null;
        //creates tree.aida file in runtime directory which stores all 
        //histograms passed to it, so they may be universally accessed.
        try{
            tree = af.createTreeFactory().create("treeH.aida","xml",false,true);
            //tree will overwrite objects created with same path
            tree.setOverwrite();
        }
        catch (java.io.IOException e) {
            System.err.println("could not create treeH.aida file.  Sowwy =(  Check your permissions"+
                    e.getMessage());          
        }
        IHistogramFactory hf = af.createHistogramFactory(tree);
        
        //only counts the hits contributing first n% of energy added
        double[] window = eventTimer(event, 1);
        
        
        //count hits in event
        int calhits = 0;
        //get particle's MC energy
        double MC_en = processMC(event);
        //energy detected in specified cal
        double E_cal = 0.0;
        //get stats for histogram ranges
        double t_cal_max = 0;
        double t_cal_min = 10000000;
        double rmax = 0;
        double rmin = 10000000;
        double rcmax = 0;
        double rcmin = 10000000;
        double tpmax = 0;
        double tpmin = 10000000;
        
        //Note, some data files have different system names.
        //Check event browser if this doesn't grab hits
        List<SimCalorimeterHit> myHitCol = event.get(SimCalorimeterHit.class, 
                "HcalBarrelHits");
        
            for(SimCalorimeterHit myhit : myHitCol){
                //if(myhit.getTime()<window[1]){
                    
                       E_cal += myhit.getRawEnergy();
                       t_cal_max = Math.max(t_cal_max, myhit.getTime());
                       t_cal_min = Math.min(t_cal_min, myhit.getTime());
                       rmax = Math.max(rmax, getAbsRadius(myhit.getPosition()));
                       rmin = Math.min(rmin, getAbsRadius(myhit.getPosition()));
                       rcmax = Math.max(rcmax, getRC(myhit));
                       rcmin = Math.min(rcmin, getRC(myhit));
                       tpmax = Math.max(tpmax, myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                       tpmin = Math.min(tpmin, myhit.getTime()-getAbsRadius(myhit.getPosition())/299.792458);
                    
                       calhits++;
                   
                //}
            }
        
            System.out.println("###HCal Results###");
            System.out.println("MC Energy: "+MC_en);
            System.out.println("Cal Energy: "+E_cal);
            System.out.println("Fraction detected"+(E_cal/MC_en));
            System.out.println();
            System.out.println("T-Cal range: ["+t_cal_min+", "+t_cal_max+"]");
            System.out.println("r/c Range: ["+rcmin+", "+rcmax+"]");
            System.out.println("T-Prime range: ["+tpmin+", "+tpmax+"]");
            System.out.println("Total Hits: "+calhits);         
        
        //Spawn all histograms to be created before for loop
        
         IHistogram1D hTcals = hf.createHistogram1D("hTcals", "Histogram of T-cals", 
                 calhits,t_cal_min,t_cal_max);
         IHistogram1D hrc = hf.createHistogram1D("hrc", "Histogram of rcs",
                 calhits,rcmin,rcmax);
         IHistogram1D hTprimes = hf.createHistogram1D("hTprimes", "Histogram of T-Primes",
                 calhits,tpmin,tpmax);
         IHistogram1D tpEn = hf.createHistogram1D("tpEn", "Histogram of T-Primes weighted by energy",
                 calhits,tpmin,tpmax);
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
         
         
            for(SimCalorimeterHit myhit : myHitCol){
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
         
            
            //now integrate selected histograms            
            IHistogram1D intTcEn = integratedHist(tcEn, hf, E_cal, .95);
            
            IHistogram1D intrcEn = integratedHist(trcEn, hf, E_cal, .95);
            
            IHistogram1D intTprimeEn = integratedHist(tpEn, hf, E_cal, .95);
            
            histToCSV(intTprimeEn);
    }
    
    public double processMC(EventHeader event) {
        
        // Get the list of MCParticles from the event
        List<MCParticle> particles = event.get(MCParticle.class, event.MC_PARTICLES);
        // Loop over the particles
        double sumE = 0.0;               
        for (MCParticle particle : particles) {
            if (particle.getGeneratorStatus() == particle.FINAL_STATE) {
                sumE = sumE + particle.getEnergy();
                System.out.println("PGID:   " + particle.getPDGID());
                List<MCParticle> daughters = particle.getDaughters();
                for (MCParticle daughter : daughters) {
                    if (daughter.getGeneratorStatus() == daughter.FINAL_STATE){
                        sumE = sumE + daughter.getEnergy();
                        System.out.println("daughter found");
                    }
                }
            }
        }
        return sumE;
        
    } // end loop over particles
}
