import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.MCParticle;

/*
 * Base class for analysis
 * provides common variables and simple functions
 * to be inherited by general data class, 
 * datasetTools extension provides constructor
 *@author Alex Conway
 */

public class eventTools {

    String[] cal;
    double HITS;
    double E_MC;
    double[] E_CAL;
    
    
    //Add MCP energy to E_MC_SUM
    public void addMCE(EventHeader event){
        this.E_MC = 0;
        List<MCParticle> MCs = event.getMCParticles();
        for(MCParticle mc : MCs){
            if(mc.getGeneratorStatus() == mc.FINAL_STATE){
                this.E_MC+= mc.getEnergy();
            }
        }
    }
    
    //add energy for detector type cal,
    //eg ECalBarrelHits
    //make sure you're calling it with the right detector name
    public void calEn(EventHeader event, int calo){
        this.E_CAL[calo] = 0;
        List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, this.cal[calo]);
        for(SimCalorimeterHit hit : hits){
            this.E_CAL[calo] += hit.getRawEnergy();
        }
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
}
