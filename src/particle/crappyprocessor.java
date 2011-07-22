/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.lcsim.util.Driver;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.MCParticle;
import hep.aida.*;
import java.util.List;
/**
 *
 * @author agias
 */
public class crappyprocessor extends Driver {
    
    public crappyprocessor(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        dpsf = af.createDataPointSetFactory(TREE);
        pf = af.createPlotterFactory();
        plot = pf.create();
        reg = plot.createRegion();
        hitsDPS =  dpsf.create("hitData","t,x,y,z,E,tp",6);
        hist = hf.createHistogram1D("tPrimes","All TPrimes", 200, -.1,.3);;
        EVENTS = 0;
        E_MC_SUM = 0;
        E_CAL_SUM =0;
        AVG_E_MC = 0;
        AVG_E_CAL = 0;
    }
    
    String NAME = "crappy";
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D tpEn;
    IDataPointSetFactory dpsf;
    IDataPointSet hitsDPS;
    IPlotterFactory pf;
    IPlotter plot;
    IPlotterRegion reg;
    IHistogram1D hist;
    
    double EVENTS;
    double E_MC_SUM ;
    double E_CAL_SUM;
    double AVG_E_MC ;
    double AVG_E_CAL;
    
    @Override
    protected void startOfData(){
        
        System.out.println("data started");
    }
    
    public void processcrap(EventHeader event){
        
        System.out.println(this.E_CAL_SUM);
        /*
        List<MCParticle> MCs = event.getMCParticles();
        for(MCParticle mc : MCs){
            if(mc.getGeneratorStatus() == mc.FINAL_STATE){
                this.E_MC_SUM+= mc.getEnergy();
            }
        }
        int index = 0;
         * 
         */
        List<List<SimCalorimeterHit>> hitsCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> hits : hitsCol){
            for(SimCalorimeterHit hit : hits){
                this.E_CAL_SUM += hit.getRawEnergy();
                hist.fill(
                        hit.getTime()-
                        Math.sqrt(
                        hit.getPosition()[0]*hit.getPosition()[0]+
                        hit.getPosition()[1]*hit.getPosition()[1]+
                        hit.getPosition()[2]*hit.getPosition()[2])/299.792458,
                        hit.getRawEnergy());
                /*
                this.E_CAL_SUM += hit.getRawEnergy();
                this.hitsDPS.addPoint();
                this.hitsDPS.point(index).coordinate(0).setValue(hit.getTime());
                this.hitsDPS.point(index).coordinate(1).setValue(hit.getPosition()[0]);
                this.hitsDPS.point(index).coordinate(2).setValue(hit.getPosition()[1]);
                this.hitsDPS.point(index).coordinate(3).setValue(hit.getPosition()[2]);
                this.hitsDPS.point(index).coordinate(4).setValue(hit.getRawEnergy());
                this.hitsDPS.point(index).coordinate(5).setValue(getTPrime(this.hitsDPS.point(index)));
                index++;
                 * 
                 */
            }
        }
        
        
        
    }
    public double getRadius(IDataPoint hit){
        double[] point = { 
            hit.coordinate(1).value(),
            hit.coordinate(2).value(),
            hit.coordinate(3).value() };
        
        return Math.sqrt(point[0]*point[0]+point[1]*point[1]+point[2]*point[2]);
    }
    public double getTPrime(IDataPoint hit){
        return hit.coordinate(0).value() - getRadius(hit)/299.792458;
    }
    
    protected void endOfCrappyData(){
        IHistogram1D inth = integratedHist(this.hist, this.hf, this.E_CAL_SUM, .90);
        
    }
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
}
