/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.lcsim.util.aida.AIDA;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.util.Driver;

/**
 *
 * @author agias
 */
public class AnalyzeCaloHits extends Driver {
    
    private AIDA aida = AIDA.defaultInstance();

   protected void process(EventHeader event) {

/*
       // Get the list of MCParticles from the event
       double E_in = 0.0;
       double E_kin = 0.0;
       // assume we shot only one particle
       int evtNr = event.getEventNumber();
       List<MCParticle> particles = event.get(MCParticle.class, event.MC_PARTICLES);
       for (MCParticle particle : particles) {
           if (particle.getGeneratorStatus() == 0) {
               E_in = E_in + particle.getEnergy();
               E_kin = E_kin + particle.getEnergy() - particle.getMass();
               if (particle.getProductionTime() == 0.0) {
                   String Particlename = particle.getType().getName();
               }
           }
           break;
       }
       //Integer Ein = (int) Math.floor(E_kin + 0.5d);
       List<List<SimCalorimeterHit>> simCalorimeterHitCollections = event.get(SimCalorimeterHit.class);
       double sumEEdep = 0.0;
       double sumECeren = 0.0;
       double fraction = 0.0;
       for (List<SimCalorimeterHit> simCalorimeterHits : simCalorimeterHitCollections) {
           String CollectionName = event.getMetaData(simCalorimeterHits).getName();
   //System.out.println(CollectionName);
       for (SimCalorimeterHit calorimeterHit : simCalorimeterHits) {
       double E = calorimeterHit.getRawEnergy();
       sumEEdep = sumEEdep + E;
       double T = calorimeterHit.getTime();
       double[] x = calorimeterHit.getPosition();
       double r = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
       aida.cloud1D("Edep Radius").fill(r, E);
       aida.cloud1D("Edep Time").fill(T);
       }
       }     // end loop over calorimeter hit collections
*/
       List<List<myHit>> myHitCol = event.get(myHit.class);
         for(List<myHit> myHits : myHitCol) {
System.out.println("fuck this shit, asshole");
            for(myHit myhit : myHits){
                aida.cloud1D("tPrimes").fill(getCorrectTime(myhit));

            }
         }
/*
       aida.cloud1D("Edep energy").fill(sumEEdep);
       fraction = sumEEdep / E_in;
       aida.cloud1D("fraction").fill(fraction);
*/
   }
    
    
    //averages corrected times
    public double getTprime(EventHeader event) {
        double sum = 0;
        int events = 0;
        List<List<myHit>> myHitCol = event.get(myHit.class);
        for(List<myHit> myHits : myHitCol) {
            for(myHit myhit : myHits){
                events++;
                sum += getCorrectTime(myhit);
            }
        }       
        return sum/events;
               
    }
    
    //gets correct time as described, c in mm/ns
    public double getCorrectTime(myHit hit){
        double[] pos = hit.getPosition();
        return getAbsRadius(pos)/299.792458;
    }
    
    //simply calculates radius to [x,y,z] from [0,0,0]
    //ie., absolute radius.
    public double getAbsRadius(double[] pos){
        return Math.sqrt(pos[0]*pos[0]+pos[1]*pos[1]+pos[2]*pos[2]);        
    }
}
