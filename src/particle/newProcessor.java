/*
 * newProcssor, the latest and greatest in processing technology
 * now with everything in one hard to read java file!
 * @author Alex Conway
 */

import java.util.List;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.MCParticle;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import hep.aida.*;
import java.util.List;
import java.io.*;



public class newProcessor extends Driver{
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D eventenergy;
    IHistogram1D fractMCenergy;
    IHistogram1D fractMCenergyEM;
    IHistogram1D fractMCenergyH;
    IHistogram2D timeRad;
    IHistogram2D timeRadEM;
    IHistogram2D timeRadH;
    IHistogram2D timeRadxEn;
    IHistogram2D timeRadxEnEM;
    IHistogram2D timeRadxEnH;
    IHistogram1D showerProfile;
    IHistogram1D showerProfileEM;
    IHistogram1D showerProfileH;
    IHistogram1D showerProfileEn;
    IHistogram1D showerProfileEnEM;
    IHistogram1D showerProfileEnH;
    IHistogram1D TPrimes;
    IHistogram1D TPrimesEM;
    IHistogram1D TPrimesH;
    IHistogram1D TPrimesEn;
    IHistogram1D TPrimesEnEM;
    IHistogram1D TPrimesEnH;
    IHistogram1D TPrimesFixd;
    IHistogram1D fractRemEnergy;
    IHistogram1D HtoEM;
    IHistogram1D EMtoH;
    IHistogram2D HbyEM;
    IHistogram1D EMenergy;
    IHistogram1D Henergy;
    
    IHistogram1D TPrimesEnInt;
    IHistogram1D windowEff;
    IHistogram1D windowEffEM;
    IHistogram1D windowEffH;
    IHistogram1D fractRemEnergyEM;
    
    int EVENTS;
    
    
    protected void startOfData(){
        EVENTS = 0;
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        TREE.mkdir("/H/");
        TREE.mkdir("/EM/");
        TREE.mkdir("/EMH/"); 
        
        TREE.cd("/EMH/"); 
        //Plot energy detected during the event from cals 0,1
        eventenergy = hf.createHistogram1D(
                "Detected Energies of particles ",
                "eventenergy ",
                200,0,10);
        //Plot energy detected during event as a fraction of 
        //the incoming MC energy
        fractMCenergy = hf.createHistogram1D(
                "Energy detected as fraction of MC particle energy ",
                "percentMCenergy ",
                200,0,1);
        TREE.cd("/EM/"); 
        //Plot EM energy detected
        EMenergy = hf.createHistogram1D(
                "Energy in EM cal",
                "EMenergy",
                200,0,10);
        TREE.cd("/H/");
        //Plot H energy detected
        Henergy = hf.createHistogram1D(
                "Energy in H cal",
                "Henergy",
                200,0,10);
        //Plot energy detected in EM during event as a fraction of 
        //the incoming MC energy
        TREE.cd("/EM/");
        fractMCenergyEM = hf.createHistogram1D(
                "EM Energy detected as fraction of MC particle energy ",
                "percentMCenergyEM ",
                200,0,1);
        //Plot energy detected in H during event as a fraction of 
        //the incoming MC energy
        TREE.cd("/H/");
        fractMCenergyH = hf.createHistogram1D(
                "H Energy detected as fraction of MC particle energy ",
                "percentMCenergyH ",
                200,0,1);
        //Plot energy detected by cal[1] as a fraction of
        //E_MC - cal[0]
        //this is mainly for E_H / (E_MC - E_EM), 
        //to give a better idea of how much was detected
        TREE.cd("/EMH/");
        fractRemEnergy = hf.createHistogram1D(
                "E_H over (E_MC - E_EM) ",
                "fractRemEnergy ",
                200,0,1);
        fractRemEnergyEM = hf.createHistogram1D(
                "E_EM over (E_MC - E_EH) ",
                "fractRemEnergyEM ",
                200,0,1);
        //Plot ratios of E_H to E_EM
        HtoEM = hf.createHistogram1D(
                "Ratio of E_H to E_EM ",
                "HtoEM",
                200,0,50);
        //Plot ratios of E_EM to E_H
        EMtoH = hf.createHistogram1D(
                "Ratio of E_EM to E_H ",
                "EMtoH",
                200,0,20);
        HbyEM = hf.createHistogram2D("E_H by E_EM",
                "HbyEM",
                100,0,10,
                100,0,10);
        //Plot hit time as a function of radius.
        timeRad = hf.createHistogram2D(
                "Hit time as a function of radius ",
                "timeRad ",
                200,0,10,
                1000,1200,3000);
        TREE.cd("/EM/");
        //Plot hit time as a function of radius.
        timeRadEM = hf.createHistogram2D(
                "Hit time as a function of radius in EM",
                "timeRadEM",
                200,0,10,
                1000,1200,3000);
        TREE.cd("/H/");
        //Plot hit time as a function of radius.
        timeRadH = hf.createHistogram2D(
                "Hit time as a function of radius in H",
                "timeRadH",
                200,0,10,
                1000,1200,3000);
        //Plot hit time as a function of radius, weight
        //point size by energy
        TREE.cd("/EMH/");
        timeRadxEn = hf.createHistogram2D(
                "Hit time as a function of radius, weighted by energy ",
                "timeRadxEn ",
                100,0,10,
                1000,1200,3000);
        TREE.cd("/EM/");
        //Plot hit time as a function of radius, weight
        //point size by energy in EM
        timeRadxEnEM = hf.createHistogram2D(
                "Hit time as a function of radius, weighted by energy in EM",
                "timeRadxEn in EM",
                100,0,10,
                1000,1200,3000);
        TREE.cd("/H/");
        //Plot hit time as a function of radius, weight
        //point size by energy in H
        timeRadxEnH = hf.createHistogram2D(
                "Hit time as a function of radius, weighted by energy in H",
                "timeRadxEn in H",
                100,0,10,
                1000,1200,3000);
        //Plot shower profiles
        //Theta is defined as the angle between the calorimeter's
        //radial vector and the xy plane where z is the beam path
        //(Theta as in spherical polar coordinates)
        //This will be relatively valid until we can get the 
        //tracker vector as a reference
        TREE.cd("/EMH/");
        showerProfile = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane ",
                "showerprofile ",
                100,-10,10);
        TREE.cd("/EM/");
        showerProfileEM = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane in EM",
                "showerprofile in EM",
                100,-10,10);
        TREE.cd("/H/");
        showerProfileH = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane in H",
                "showerprofile in H",
                100,-10,10);
        TREE.cd("/EMH");
        //same thing but weighted by energy
        showerProfileEn = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane weighted by energy ",
                "showerprofileEn ",
                100,-10,10);
        TREE.cd("/EM/");
        showerProfileEnEM = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane weighted by energy in EM",
                "showerprofileEn in EM",
                100,-10,10);
        TREE.cd("/H/");
        showerProfileEnH = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane weighted by energy in H",
                "showerprofileEn in H",
                100,-10,10);
        //Plot raw TPrimes
        TREE.cd("/EMH/");
        TPrimes = hf.createHistogram1D(
                "Raw TPrimes ",
                "TPrimes ",
                100000,-1000,9000);
        TREE.cd("/EM/");
        TPrimesEM = hf.createHistogram1D(
                "Raw TPrimes EM",
                "TPrimesEM ",
                100000,-1000,9000);
        TREE.cd("/H/");
        TPrimesH = hf.createHistogram1D(
                "Raw TPrimes H",
                "TPrimesH",
                100000,-1000,9000);
        //Plot TPrimes weighted by energy
        TREE.cd("/EMH/");
        TPrimesEn = hf.createHistogram1D(
                "Tprimes weighted by energy ",
                "TPrimesEn ",
               2000,-1,9);
        TREE.cd("/EM/");
        TPrimesEnEM = hf.createHistogram1D(
                "Tprimes weighted by energy ",
                "TPrimesEn ",
                2000,-1,9);
        TREE.cd("/H/");
        TPrimesEnH = hf.createHistogram1D(
                "Tprimes weighted by energy ",
                "TPrimesEn ",
                2000,-1,9);
    }
    
    protected void process(EventHeader event){
        double HITS;
        double E_MC;
        double E_EM;
        double E_H;
        
        //Count events processed
        EVENTS++;
        //Get this event's MC energy
        E_MC = addMCE(event);
        //Get detectable energy in each cal
        E_EM = calEn(event, "EcalBarrelHits");
        E_H = calEn(event, "HcalBarrelHits");
        
        TREE.cd("/EMH/");
        eventenergy.fill(E_EM + E_H);
        
        fractMCenergy.fill((E_EM+E_H)/E_MC);
        
        fractRemEnergy.fill(E_H/(E_MC-E_EM));
        fractRemEnergyEM.fill(E_EM/(E_EM+E_H));
        
        HtoEM.fill(E_H/E_EM);
        EMtoH.fill(E_EM/E_H);
        HbyEM.fill(E_H, E_EM);
        
        TREE.cd("/EM/");
        EMenergy.fill(E_EM);
        
        fractMCenergyEM.fill(E_EM/E_MC);
        
        TREE.cd("/H/");
        Henergy.fill(E_H);
        
        fractMCenergyH.fill(E_H/E_MC);
        
        /* ### EM HITS ### */
        List<SimCalorimeterHit> emhits = event.get(SimCalorimeterHit.class, "EcalBarrelHits");
        
        for(SimCalorimeterHit hit : emhits){
            
            TREE.cd("/EMH/");
            timeRad.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()));
            timeRadxEn.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()),
                    hit.getRawEnergy());
            showerProfile.fill(
                    getTheta(hit.getPosition()));
            showerProfileEn.fill(
                    getTheta(hit.getPosition()),
                    hit.getRawEnergy()); 
            TPrimes.fill(getTPrime(hit));
            TPrimesEn.fill(
                    getTPrime(hit),
                    hit.getRawEnergy());
            TREE.cd("/EM/");
            timeRadEM.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()));
            timeRadxEnEM.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()),
                    hit.getRawEnergy());
            showerProfileEM.fill(
                    getTheta(hit.getPosition()));
            showerProfileEnEM.fill(
                    getTheta(hit.getPosition()),
                    hit.getRawEnergy());
            TPrimesEM.fill(getTPrime(hit));
            TPrimesEnEM.fill(
                    getTPrime(hit),
                    hit.getRawEnergy());
            
        }
        
        /* ### H HITS ONLY ### */
        List<SimCalorimeterHit> hhits = event.get(SimCalorimeterHit.class, "HcalBarrelHits");
        
        for(SimCalorimeterHit hit : hhits){
            TREE.cd("/EMH/");
            timeRad.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()));
            timeRadxEn.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()),
                    hit.getRawEnergy());
            showerProfile.fill(
                    getTheta(hit.getPosition()));
            showerProfileEn.fill(
                    getTheta(hit.getPosition()),
                    hit.getRawEnergy()); 
            TPrimes.fill(getTPrime(hit));
            TPrimesEn.fill(
                    getTPrime(hit),
                    hit.getRawEnergy());
            TREE.cd("/H/");
            timeRadH.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()));
            timeRadxEnH.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()),
                    hit.getRawEnergy());
            showerProfileH.fill(
                    getTheta(hit.getPosition()));
            showerProfileEnH.fill(
                    getTheta(hit.getPosition()),
                    hit.getRawEnergy());
            TPrimesH.fill(getTPrime(hit));
            TPrimesEnH.fill(
                    getTPrime(hit),
                    hit.getRawEnergy());
        }
    }
    
    protected void endOfData(){
        TREE.mkdir("/post/");
        TREE.cd("/post/");
        TPrimesEnInt = integratedHist(TPrimesEn, .9);
        TPrimesFixd = histFixer(TPrimesEn);
        windowEffEM = windowEfficiency(TPrimesEM);
        windowEffH = windowEfficiency(TPrimesH);
        windowEff = windowEfficiency(TPrimesEn);
    }
    /*
     * ###############################################
     * 
     * 
     * Helper functions Section
     * 
     * 
     * ################################################
     */
    //Add MCP energy to E_MC_SUM
    public double addMCE(EventHeader event){
        double E_MC = 0;
        List<MCParticle> MCs = event.getMCParticles();
        for(MCParticle mc : MCs){
            if(mc.getGeneratorStatus() == mc.FINAL_STATE){
                E_MC+= mc.getEnergy();
            }
        }
        return E_MC;
    }
    //add energy for detector type cal,
    //eg ECalBarrelHits
    //make sure you're calling it with the right detector name
    public double calEn(EventHeader event, String cal){
        double E_CAL = 0;
        List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, cal);
        for(SimCalorimeterHit hit : hits){
            E_CAL += hit.getRawEnergy();
        }
        return E_CAL;
    }
    
    public double getRadius(double[] point){
        return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
    }
    public double getTPrime(SimCalorimeterHit hit){
        return hit.getTime() - getRadius(hit.getPosition())/299.792458;
    }
    //point in [x,y,z]
    public double getTheta(double[] point){
        return Math.tan(point[2]/point[0]);
    }
    public double getPhi(double[] point){
        return Math.tan(point[1]/point[0]);
    }
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
        double xvalue;
        for(int i=0; i<hist.axis().bins() ; i++){
            xvalue = hist.axis().binCenter(i);
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
                            firstTime);
                }
            }
            
        }
        return intHist;
    }
    
    //
    public IHistogram1D windowEfficiency(IHistogram1D hist){
        double mean = hist.mean();
        int meanbin = hist.coordToIndex(mean);
        //Window width on one side
        double maxwin = Math.min(
                mean-hist.axis().lowerEdge(), 
                hist.axis().upperEdge()-mean);
        int maxwinbins = (int) Math.min(meanbin, hist.axis().bins()-meanbin);
        System.out.println(mean+","+meanbin+","+maxwin+","+maxwinbins);
        windowEff = hf.createHistogram1D(
                "Values of integrals based on time windows about mean "+hist.title(),
                "windowEff "+hist.title(), 
                maxwinbins,0,maxwin);
        
        for(int i=0; i<maxwinbins; i++){
            windowEff.fill(
                    i*maxwin/maxwinbins,
                    windowInt(
                            hist, 
                            meanbin - hist.coordToIndex(i*maxwin/maxwinbins),
                            meanbin + hist.coordToIndex(i*maxwin/maxwinbins)));
        }
        return windowEff;
    }
    
    public double windowInt(IHistogram1D hist, int min, int max){
        
        int bins = max-min+1;
        double sum = 0;
        int index = min;
        for(int i=0; i<bins ; i++){
            sum += hist.binHeight(index);
            index += i;            
        }
        return sum;
    }
    
    public IHistogram1D histFixer(IHistogram1D hist){
        int meanbin = hist.coordToIndex(hist.mean());
        int lowbin=0;
        double halfen = hist.sumBinHeights()/2;
        double sum = 0;
        boolean hityet = false;
        for(int i=0; i<hist.axis().bins(); i++){
            if(!hityet){
                sum += hist.binHeight(meanbin-i);
                if(sum>.99*halfen){
                    lowbin = (meanbin-i);
                    hityet = true;
                }
            }
        }
        hityet = false;
        sum = 0;
        int highbin=0;
        for(int i=1; i<hist.axis().bins(); i++){
            if(!hityet){
                sum += hist.binHeight(meanbin+i);
                if(sum>.99*halfen){
                    lowbin = (meanbin+i);
                    hityet = true;
                    break;
                }
            }
        }
        IHistogram1D rv = hf.createHistogram1D(
                "fixed "+hist.title(),
                "fixed "+hist.title(),
                highbin-lowbin, hist.axis().binCenter(lowbin),hist.axis().binCenter(highbin));
        for(int i=lowbin; i<highbin; i++){
            rv.fill(hist.axis().binCenter(i), hist.binHeight(i));
        }
        return rv;
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
