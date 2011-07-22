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
        
    }
    
    String NAME = "Shit";
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D tpEn;
    IDataPointSetFactory dpsf;
    IDataPointSet hitsDPS;
    IPlotterFactory pf;
    IPlotter plot;
    IPlotterRegion reg;
    
    double EVENTS;
    double E_MC_SUM ;
    double E_CAL_SUM;
    double AVG_E_MC ;
    double AVG_E_CAL;
    
    @Override
    protected void startOfData(){
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        dpsf = af.createDataPointSetFactory(TREE);
        pf = af.createPlotterFactory();
        plot = pf.create();
        reg = plot.createRegion();
        hitsDPS =  dpsf.create("hitData","t,x,y,z,E,tp",6);
        EVENTS = 0;
        E_MC_SUM = 0;
        E_CAL_SUM =0;
        AVG_E_MC = 0;
        AVG_E_CAL = 0;
        System.out.println("data started");
    }
    
    protected void processor(EventHeader event){
        
        System.out.println(E_MC_SUM);
        System.out.println(E_CAL_SUM);
        
        List<MCParticle> MCs = event.getMCParticles();
        for(MCParticle mc : MCs){
            if(mc.getGeneratorStatus() == mc.FINAL_STATE){
                E_MC_SUM+= mc.getEnergy();
            }
        }
        int index = 0;
        List<List<SimCalorimeterHit>> hitsCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> hits : hitsCol){
            for(SimCalorimeterHit hit : hits){
                E_CAL_SUM += hit.getRawEnergy();
                hitsDPS.addPoint();
                hitsDPS.point(index).coordinate(0).setValue(hit.getTime());
                hitsDPS.point(index).coordinate(1).setValue(hit.getPosition()[0]);
                hitsDPS.point(index).coordinate(2).setValue(hit.getPosition()[1]);
                hitsDPS.point(index).coordinate(3).setValue(hit.getPosition()[2]);
                hitsDPS.point(index).coordinate(4).setValue(hit.getRawEnergy());
                hitsDPS.point(index).coordinate(5).setValue(getTPrime(this.hitsDPS.point(index)));
                index++;
                System.out.println(index);
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
    @Override
    protected void endOfData(){
        IHistogram1D rv = hf.createHistogram1D(
                NAME, 
                NAME+"tpEn",
                hitsDPS.size(),
                hitsDPS.lowerExtent(5),
                hitsDPS.upperExtent(5));
        
        for(int i=0; i<hitsDPS.size(); i++){
            
            rv.fill(hitsDPS.point(i).coordinate(5).value(),
                    hitsDPS.point(i).coordinate(4).value());
        }
        reg.plot(rv);
    }
}
