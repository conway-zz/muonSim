/*
Simple program that loos over Calorimeter Hit collections and calculates the total energy in the Calorimeter
We assume this is not a sampling calorimeter so no corrections are being done. 
 */

import org.lcsim.util.aida.*;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.util.Driver;
import hep.aida.*;
import java.util.ArrayList;

public class AnalyzeCaloHits extends Driver {

    private AIDA aida = AIDA.defaultInstance();
    String name = null;
    IHistogram1D h1 = null;
    List<IHistogram1D> histos = new ArrayList<IHistogram1D>();
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
    double[] sumEEdep = new double[21];
    double sumEEdepall = 0.0;

    @Override
    protected void startOfData() {
        IAnalysisFactory af = IAnalysisFactory.create();
        ITree tree = af.createTreeFactory().create();
        hf = af.createHistogramFactory(tree);

        //      tree.mkdir("/Histograms");
        //      tree.cd("/Histograms");

        h1 = hf.createHistogram1D("Edep per cell", 100, 0, 0.05);
        //     IHistogram1D h11 = hf.createHistogram1D("Edep time",10000,0,5000.);
        //   IHistogram1D h11 = hf.createHistogram1D("Edep time",10000,0,5000.);


        hissumEEdep = hf.createHistogram1D("Edep energy ", 100, 9.5, 10.5);
        for (int i = 0; i < 21; i++) {
            name = "Edep energy  " + xval[i] + " nsec";
            histos.add(hf.createHistogram1D(name, 100, 0, 10));
            System.out.println(name);
        }
        double fraction = 0.0;

    }

    protected void process(EventHeader event) {
        // Get the list of MCParticles from the event

        double E_in = 10.0;
        double E_kin = 0.0;
        final double c = 299.792458; // speed of light in mm/nsec

        List<List<SimCalorimeterHit>> simCalorimeterHitCollections = event.get(SimCalorimeterHit.class);
        for (int i = 0; i < 21; i++) {
            sumEEdep[i] = 0.0;
        }
        sumEEdepall = 0.0;
        double fraction = 0.0;

        for (List<SimCalorimeterHit> simCalorimeterHits : simCalorimeterHitCollections) {
            String CollectionName = event.getMetaData(simCalorimeterHits).getName();
            //System.out.println(CollectionName);
            for (SimCalorimeterHit calorimeterHit : simCalorimeterHits) {
                double E = calorimeterHit.getRawEnergy();
                aida.cloud1D("Edeppercell").fill(E);
                h1.fill(E);
                sumEEdepall = sumEEdepall + E;

                double T = calorimeterHit.getTime();
                double[] x = calorimeterHit.getPosition();

                double r = Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
                double tp = T - r / c;
                for (int i = 0; i < 21; i++) {
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
        for (int i = 0; i < 21; i++) {
            histos.get((i)).fill(sumEEdep[i]);
        }

//        fraction = sumEEdep / E_in;
//        aida.cloud1D("fraction").fill(fraction);
    }

    @Override
    protected void endOfData() {
        System.out.println("all"+ "  " + hissumEEdep.mean() + "   " + hissumEEdep.rms());
        for (int i = 0; i < 21; i++) {
            System.out.println(xval[i] + "  " + histos.get(i).mean() + "   " + histos.get(i).rms());
        }


    }
}
