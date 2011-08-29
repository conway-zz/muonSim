/*
Simple program that loops over Calorimeter Hit collections and calculates the total energy in the Calorimeter
We assume this is not a sampling calorimeter so no corrections are being done. 
 */

import org.lcsim.util.aida.*;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.util.Driver;
import hep.aida.*;
import java.util.ArrayList;
import hep.physics.vec.VecOp;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

public class AnalyzeCaloHitsNew extends Driver {

    private AIDA aida = AIDA.defaultInstance();
    String name = null;
    IHistogram1D h1 = null;
    List<IHistogram1D> histos = new ArrayList<IHistogram1D>();
    List<IHistogram1D> histoscell = new ArrayList<IHistogram1D>();
    IHistogram1D hissumEEdep = null;
    IHistogramFactory hf = null;
    double[] xval = {5.,
        10.,
        15.,
        20.,
        25.,
        30.,
        35.,
        40.,
        45.,
        50.,
        55.,
        100.,
        200.,
        300.,
        400.,
        500.,
        600.,
        700.,
        800.,
        1000.,
        2000.};
    String[] Collection_names = {
        "EcalBarrelHits",
        "EcalEndcapHits",
        "HcalBarrelHits",
        "HcalEndcapHits",
        "MuonBarrelHits",
        "MuonEndcapHits"
    };
    double[] threshold = {
        4.5,
        4.5,
        4.5,
        4.5,
        26.,
        26.
    };
    final int nslices = 21;
    double[] sumEEdep = new double[nslices];
    final int ncolls = 6;
    double sumEEdepall = 0.0;

    @Override
    protected void startOfData() {
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = af.createTreeFactory().create();
        hf = af.createHistogramFactory(tree);
        h1 = hf.createHistogram1D("Edep per cell", 100, 0, 50.);
        hissumEEdep = hf.createHistogram1D("Edep energy ", 100, 4., 10.);
        for (int i = 0; i < nslices; i++) {
            name = "Edep energy  " + xval[i] + " nsec";
            histos.add(hf.createHistogram1D(name, 100, 4., 10.));
        }
        for (int i = 0; i < 4; i++) {
            name = "Edep per cell " + Collection_names[i];
            histoscell.add(hf.createHistogram1D(name, 100, 0.0, 50.));
        }
        for (int i = 4; i < ncolls; i++) {
            name = "Edep per cell " + Collection_names[i];
            histoscell.add(hf.createHistogram1D(name, 100, 0.0, 250.));
        }

        double fraction = 0.0;
    }

    @Override
    protected void process(EventHeader event) {
        // Get the list of MCParticles from the event

        double E_in = 10.0;
        double E_kin = 0.0;
        final double c = 299.792458; // speed of light in mm/nsec

        List<List<SimCalorimeterHit>> simCalorimeterHitCollections = event.get(SimCalorimeterHit.class);

        for (int i = 0;
                i < nslices;
                i++) {
            sumEEdep[i] = 0.0;
        }
        sumEEdepall = 0.0;
        double fraction = 0.0;

        for (List<SimCalorimeterHit> simCalorimeterHits : simCalorimeterHitCollections) {
            String CollectionName = event.getMetaData(simCalorimeterHits).getName();
            //System.out.println(CollectionName+":  "+simCalorimeterHits.size());
            if (CollectionName.contains("Endcap")) {
                continue;
            }
            int coll = 0;
            double thresh = 0.0;
            /*            for (int i = 0; i < ncolls; i++) {
            if (CollectionName.equals(Collection_names[i])) {
            coll = i;
            thresh = threshold[i];
            break;
            }
            }
             */
            for (SimCalorimeterHit calorimeterHit : simCalorimeterHits) {
                double E = calorimeterHit.getRawEnergy();
                if (E * 1000. < thresh) {
                    continue;
                }
                aida.cloud1D("Edeppercell").fill(E);
                histoscell.get(coll).fill(1000. * E);
                h1.fill(E);
                sumEEdepall = sumEEdepall + E;

                //                   aida.cloud1D("mucosTheta").fill(VecOp.cosTheta(particle.getMomentum()));
                //                   aida.cloud1D("muphi").fill(VecOp.phi(particle.getMomentum()));

                double T = calorimeterHit.getTime();
                double[] x = calorimeterHit.getPosition();
                Hep3Vector vec = new BasicHep3Vector(x);
                aida.cloud1D("cosTheta").fill(VecOp.cosTheta(vec), E);
                aida.cloud1D("phi").fill(VecOp.phi(vec), E);
                double r = Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
                double tp = T - r / c;
                for (int i = 0; i < nslices; i++) {
                    if (tp < xval[i]) {
                        sumEEdep[i] = sumEEdep[i] + E;
                    }
                }

                aida.cloud1D("Edep Radius").fill(r, E);
                aida.cloud2D("Edep vs time").fill(E, T);
                //	h11.fill(T,E);	
                aida.cloud1D("Edep Time").fill(T);
                aida.cloud1D("corrected Edep Time").fill(tp);
                aida.cloud2D("rvstp").fill(r, tp);
            }
        }     // end loop over calorimeter hit collections

        hissumEEdep.fill(sumEEdepall);

        for (int i = 0;
                i < nslices;
                i++) {
            //System.out.println(i+"   "+sumEEdep[i]);
            histos.get((i)).fill(sumEEdep[i]);
        }
//        fraction = sumEEdep / E_in;
//        aida.cloud1D("fraction").fill(fraction);
    }

    @Override
    protected void endOfData() {
        for (int i = 0; i < nslices; i++) {
            System.out.println(xval[i]);
        }
        for (int i = 0; i < nslices; i++) {
            System.out.println(histos.get(i).mean() + ",");
        }
        System.out.println(hissumEEdep.mean() + "};");
                for (int i = 0; i < nslices; i++) {
            System.out.println(histos.get(i).rms()+",");
        }
        System.out.println(hissumEEdep.rms()+ "};");
//        for (int i = 0; i < nslices; i++) {
//            System.out.println(xval[i] + "  " + histos.get(i).mean() + "   " + histos.get(i).rms());
//        }
    }
}
