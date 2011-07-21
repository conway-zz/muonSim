
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.MCParticle;
import hep.aida.*;

/*
 * Simple object to hold some data with methods for filling itself
 * instantiate one for every processXX() run in process.
 * Basically for having permanent data object through process run.
 *@author Alex Conway
 */

public class dataObj{

    String NAME;
    double EVENTS = 0;
    double E_MC_SUM = 0;
    double E_CAL_SUM =0;
    double AVG_E_MC = 0;
    double AVG_E_CAL = 0;
    
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D tpEn;
    IDataPointSetFactory dpsf;
    IDataPointSet hitsDPS;
    
    public dataObj(String name){
        NAME = name;
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        dpsf = af.createDataPointSetFactory(TREE);
        hitsDPS = dpsf.create("hitData","t,x,y,z,E,tp",6);
        hitsDPS.setTitle(name+"hitsDPs");
    }
    
    //Add MCP energy to E_MC_SUM
    public void addMCE(EventHeader event){
        List<MCParticle> MCs = event.getMCParticles();
        for(MCParticle mc : MCs){
            if(mc.getGeneratorStatus() == mc.FINAL_STATE){
                this.E_MC_SUM+= mc.getEnergy();
            }
        }
    }
    
    //Add whole event's detectable energy.
    //do not call this and its other version in same processXX()
    public void addEn(EventHeader event){
        List<List<SimCalorimeterHit>> hitsCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> hits : hitsCol){
            for(SimCalorimeterHit hit : hits){
                this.E_CAL_SUM += hit.getRawEnergy();
            }
        }
    }
    //add energy for detector type cal,
    //eg ECalBarrelHits
    //make sure you're calling it with the right detector name
    public void addEn(EventHeader event, String cal){
        List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, cal);
        for(SimCalorimeterHit hit : hits){
            this.E_CAL_SUM =+ hit.getRawEnergy();
        }
    }
    
    //Add data from each hit to a datapoint set for analyses
    public void hitsToDPS(EventHeader event){
        int index = 0;
        List<List<SimCalorimeterHit>> hitsCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> hits : hitsCol){
            for(SimCalorimeterHit hit : hits){
                this.hitsDPS.addPoint();
                this.hitsDPS.point(index).coordinate(0).setValue(hit.getTime());
                this.hitsDPS.point(index).coordinate(1).setValue(hit.getPosition()[0]);
                this.hitsDPS.point(index).coordinate(2).setValue(hit.getPosition()[1]);
                this.hitsDPS.point(index).coordinate(3).setValue(hit.getPosition()[2]);
                this.hitsDPS.point(index).coordinate(4).setValue(hit.getRawEnergy());
                this.hitsDPS.point(index).coordinate(5).setValue(getTPrime(this.hitsDPS.point(index)));
                index++;
            }
        }
    }
    
    public void hitsToDPS(EventHeader event, String cal){
        
        int index = 0;
        List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, cal);
        for(SimCalorimeterHit hit : hits){
            this.hitsDPS.addPoint();
            this.hitsDPS.point(index).coordinate(0).setValue(hit.getTime());
            this.hitsDPS.point(index).coordinate(1).setValue(hit.getPosition()[0]);
            this.hitsDPS.point(index).coordinate(2).setValue(hit.getPosition()[1]);
            this.hitsDPS.point(index).coordinate(3).setValue(hit.getPosition()[2]);
            this.hitsDPS.point(index).coordinate(4).setValue(hit.getRawEnergy());
            this.hitsDPS.point(index).coordinate(5).setValue(getTPrime(this.hitsDPS.point(index)));
            index++;
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
}
