
import org.lcsim.util.aida.AIDA;
import hep.physics.vec.VecOp;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.util.Driver;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
/*
 * An example showing how to access MCParticles from the EventHeader and
 * make some simple histograms from the data.
 * 
 */
public class Analysis101 extends Driver {

    private AIDA aida = AIDA.defaultInstance();

    protected void process(EventHeader event) {
        MCParticle mum = null;
        MCParticle mup = null;
        // Get the list of MCParticles from the event
        List<MCParticle> particles = event.get(MCParticle.class, event.MC_PARTICLES);
        // Histogram the number of particles per event
        // Loop over the particles
        int found = 0;
        int ntracks = 0;
        double sumE = 0.0;
        Hep3Vector sumP = new BasicHep3Vector(0.,0.,0.);
        for (MCParticle particle : particles) {
            if (particle.getGeneratorStatus() == particle.FINAL_STATE) {
                ntracks++;
                sumE = sumE + particle.getEnergy();
                sumP = VecOp.add(sumP, particle.getMomentum());
                if (Math.abs(particle.getPDGID()) == 13) {
                    aida.cloud1D("muenergy").fill(particle.getEnergy());
                    aida.cloud1D("mucosTheta").fill(VecOp.cosTheta(particle.getMomentum()));
                    aida.cloud1D("muphi").fill(VecOp.phi(particle.getMomentum()));
                    //System.out.println("PGID:   " + particle.getPDGID());
                    if (particle.getPDGID() == 13) {
                        mum = particle;
                        found++;
                    }
                    if (particle.getPDGID() == -13) {
                        mup = particle;
                        found++;
                    }
                }
            }
        } // end loop over particles
        double psquare= VecOp.dot(sumP, sumP);
        double ainvmass = Math.sqrt(sumE * sumE + psquare);
        aida.cloud1D("ainvmass").fill(ainvmass);
        aida.cloud1D("nTracks").fill(ntracks);
//        System.out.println("found:   " + found);
        if (found == 2) {
            double invmas = mum.getMass() * mum.getMass() + mup.getMass() * mup.getMass()
                    + 2. * (mum.getEnergy() * mup.getEnergy() - VecOp.dot(mum.getMomentum(), mup.getMomentum()));
            invmas = Math.sqrt(invmas);
            aida.cloud1D("muinvmas").fill(invmas);
        }
    }
}