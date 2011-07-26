/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class testProcessor extends Driver {
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D TPrimesEn;
    IHistogram1D TPrimesEn2;
    IHistogram1D TPrimesEn2Int;
    IHistogram1D TPrimesEnEM;
    IHistogram1D TPrimesEnH;
    IHistogram1D TPrimesEnInt;
    IHistogram1D TPrimesFixd;
    IHistogram1D windowEff;
    IHistogram1D windowEffEM;
    IHistogram1D windowEffH;
    IHistogram1D totalEnergy;
    IHistogram1D TPrimesAll;
    IHistogram1D TPrimesAllInt;
    double sum = 0;
    
     protected void startOfData(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().create();
        hf = af.createHistogramFactory(TREE);
        /*
        try{
            TREE = af.createTreeFactory().create("/testTree", "xml", false, true);
        } 
        catch (IOException e) {
                System.out.println("testTree could not be created"+ e);
        }
        */
        
        hf = af.createHistogramFactory(TREE);
        
        TPrimesAll = hf.createHistogram1D(
                "total energy",
                "totalEnergyALL",
                1000,0.0,1000.0);
        
        TPrimesEn = hf.createHistogram1D(
                "Tprimes weighted by energy ",
                "TPrimesEn ",
               2000,-.02,1);
        TPrimesEn2 = hf.createHistogram1D(
                "Tprimes weighted by energy 2",
                "TPrimesEn2 ",
               2000,-1,999);
        TPrimesEnEM = hf.createHistogram1D(
                "Tprimes EM weighted by energy ",
                "TPrimesEnEM",
               2000,-.02,1);
        TPrimesEnH = hf.createHistogram1D(
                "Tprimes H weighted by energy ",
                "TPrimesEnH",
               2000,-.02,1);
        totalEnergy = hf.createHistogram1D(
                "total energy integrated",
                "totalEnergy",
                1000,-0.2,1000);
     }
     
     protected void process(EventHeader event){
         
         List<List<SimCalorimeterHit>> allhits = event.get(SimCalorimeterHit.class);
         for(List<SimCalorimeterHit> hitcol : allhits){
             for(SimCalorimeterHit hit : hitcol){
             TPrimesAll.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
            }
         }
         List<SimCalorimeterHit> emhits = event.get(SimCalorimeterHit.class, "EcalBarrelHits");
        
         for(SimCalorimeterHit hit : emhits){
             TPrimesEn.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             TPrimesEnEM.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             sum += hit.getRawEnergy();
             TPrimesEn2.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             
         }
         List<SimCalorimeterHit> hhits = event.get(SimCalorimeterHit.class, "HcalBarrelHits");
        
         for(SimCalorimeterHit hit : hhits){
             TPrimesEn.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             TPrimesEnH.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             sum += hit.getRawEnergy();
             TPrimesEn2.fill(
                     getTPrime(hit),
                     hit.getRawEnergy());
             
         }
     }
     
     protected void endOfData(){
         TPrimesAllInt = integratedHist(TPrimesAll, .9);
         TPrimesEnInt = integratedHist(TPrimesEn, .9);
         TPrimesEn2Int = integratedHist(TPrimesEn2, .9);
         //TPrimesFixd = histFixer(TPrimesEn);
         windowEff = windowEfficiency(TPrimesEn);
     }
     
     public double getRadius(double[] point){
         return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
     }
     public double getTPrime(SimCalorimeterHit hit){
         return hit.getTime() - getRadius(hit.getPosition())/299.792458;
     }
     public IHistogram1D integratedHist(IHistogram1D hist, double threshhold){    
        
        boolean isAboveThresh = false;
        boolean hitYet = false;
        double firstTime=0;
        
        IHistogram1D intHist = hf.createHistogram1D("int"+hist.title(), 
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
            System.out.println(i+","+i*maxwin/maxwinbins+","+hist.coordToIndex(i*maxwin/maxwinbins));
            windowEff.fill(
                    i*maxwin/maxwinbins,
                    windowInt(
                            hist, 
                            meanbin - hist.coordToIndex(i*maxwin/maxwinbins - 1),
                            meanbin + hist.coordToIndex(i*maxwin/maxwinbins - 1)));
        }
        return windowEff;
    }
    
    public double windowInt(IHistogram1D hist, int min, int max){
        double sum = 0;
        for(int i=min; i<=max ; i++){
            sum += hist.binHeight(i);
        }
        return sum;
    }
    
    public IHistogram1D histFixer(IHistogram1D hist){
        int meanbin = hist.coordToIndex(hist.mean());
        int lowbin =0 ;
        double lowen =0 ;
        double sum = 0;
        boolean hityet = false;
        
        for(int i=meanbin; i>=0; i--){
            lowen +=hist.binHeight(i);
        }
        
        double highen = 0;
        for(int i=meanbin; i<hist.axis().bins(); i++){
            highen +=hist.binHeight(i);
        }
        System.out.println(meanbin+","+lowen+","+highen);
        for(int i=meanbin; i>=0; i--){
            if(!hityet){
                sum += hist.binHeight(i);
                System.out.println(i+","+sum);
                if(sum>.99*lowen){
                    lowbin = (meanbin-i);
                    hityet = true;
                }
            }
        }
        System.out.println(lowbin);
        hityet = false;
        sum = 0;
        int highbin = 0;
        for(int i=meanbin; i<hist.axis().bins(); i++){
            if(!hityet){
                sum += hist.binHeight(i);
                System.out.println(i+","+sum);
                if(sum>.99*highen){
                    highbin = i;
                    hityet = true;
                }
            }
        }
        System.out.println(highbin);
        IHistogram1D rv = hf.createHistogram1D(
                "fixed "+hist.title(),
                "fixed "+hist.title(),
                highbin-lowbin, hist.axis().binLowerEdge(lowbin),hist.axis().binUpperEdge(highbin));
        for(int i=lowbin; i<highbin; i++){
            rv.fill(hist.axis().binCenter(i), hist.binHeight(i));
        }
        return rv;
    }
}
