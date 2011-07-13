/**
 * creates graphs as currently needed
 * 
 * @author Alex Conway
 */

import hep.aida.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import java.io.FileWriter;
import java.io.Writer;
import org.lcsim.util.aida.AIDA;

public class stats extends timingAndEnergyFns {
    
    private AIDA aida = AIDA.defaultInstance();
     
     protected void process(EventHeader event) {
        
         try {
             //create log file in muonSim folder
             //mainly for checking values produced
             //using "\n" will only work on UNIX
            FileWriter log = new FileWriter("../../log.txt", true);
        
         double stdthresh = 1.0;
         
         IAnalysisFactory af = IAnalysisFactory.create();
         ITree tree = af.createTreeFactory().create();
         IHistogramFactory hf = af.createHistogramFactory(tree);
         
         ICloud1D cl1d = hf.createCloud1D("cl1d", "plotTprimes");
         //ICloud1D cl1d3 = hf.createCloud1D("cl1d3", "plotTprime");
         
         //look at tprimes from event
         List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
         double cutoff = eventTimer(event, stdthresh)[1];
         for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){
                if(myhit.getTime()<cutoff){
                    cl1d.fill(getCorrectTime(myhit));
                }
            }
         }
         
         double tprime = getTprime(event);
         log.write("avg TPrime: "+Double.toString(tprime)+"\n");
         System.out.println("avg TPrime: "+Double.toString(tprime)+"\n");
         
         //finds how much energy is lost from current getTprime(event) fn,
         //cutting off only the beginning of the event
         double detectableE = totalEnergy(event);
         double[] window = new double[2];
         window[0]=tprime;
         window[1]=timeLastHit(event);
         double detectedE = windowEnergy(event, window);
         System.out.println("Effect of Tprime cutoff only:");
         System.out.println("detectable: "+detectableE+" detected: "+detectedE);
         System.out.println("fraction: "+(detectedE/detectableE)+" difference: "
                            +(detectableE-detectedE));
         log.write("Effect of Tprime cutoff only:"+"\n");
         log.write("detectable: "+detectableE+" detected: "+detectedE+"\n");
         log.write("fraction: "+(detectedE/detectableE)+" difference: "
                            +(detectableE-detectedE)+"\n");
         
         
         //plots energy detected versus eventTimer threshhold (0-1)
         ICloud2D cl2d = hf.createCloud2D("cl2d", "energy vs threshhold");
         double threshhold = 0;
         for(double i=1; i<1000; i++){
             threshhold = (i/1000);
             detectedE = windowEnergy(event, eventTimer(event, threshhold));
             cl2d.fill(threshhold, detectedE);
             aida.cloud2D("energy vs thresh").fill(threshhold, detectedE);
         }
         
         
         //returns energy detected with given threshhold and getTprime()
         //IHistogram2D cloud2DHist = cl2d.histogram();
      
         IPlotter plotter = af.createPlotterFactory().create("Cloud.java plot");
         plotter.createRegion(0,1);
         plotter.region(0).plot(cl1d);
         //plotter.region(2).plot(cl1d3);
         plotter.region(1).plot(cl2d);
         plotter.show();
         log.close();
         
         } catch (IOException ex) {
            Logger.getLogger(stats.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     
     
}
