/*
 * eventTools extension;
 * fill histograms and count events
 * Instantiate one dataset for each calorimeter
 * you wish to look at with the string of the
 * calorimeter name, eg emCal = new datset("EcalBarrelHits");
 */
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import hep.aida.*;
import java.util.List;

public class dataset extends eventTools {
    
    double EVENTS;
    IAnalysisFactory af;
    ITree TREE;
    IHistogramFactory hf;
    IHistogram1D eventenergy;
    IHistogram1D fractMCenergy;
    IHistogram1D fractMCenergyEM;
    IHistogram1D fractMCenergyH;
    IHistogram2D timeRad;
    IHistogram2D timeRadxEn;
    IHistogram1D showerProfile;
    IHistogram1D showerProfileEn;
    IHistogram1D sumEnCals;
    IHistogram1D TPrimes;
    IHistogram1D TPrimesEn;
    IHistogram1D fractRemEnergy;
    IHistogram1D HtoEM;
    
    //datset constructor.
    //Initializes AIDA objects
    //ex calorimeter: EcalBarrelHits
    public dataset(String[] calorimeter){
        EVENTS = 0;
        cal= calorimeter;
        af = IAnalysisFactory.create();
        TREE = af.createTreeFactory().createTree();
        hf = af.createHistogramFactory(TREE);
        E_CAL = new double[calorimeter.length];
        
        //Plot energy detected during the event from cals 0,1
        eventenergy = hf.createHistogram1D(
                "Detected Energies of particles "+calorimeter,
                "eventenergy "+calorimeter,
                200,0,10);
        //Plot energy detected during event as a fraction of 
        //the incoming MC energy
        fractMCenergy = hf.createHistogram1D(
                "Energy detected as fraction of MC particle energy "+calorimeter,
                "percentMCenergy "+calorimeter,
                200,0,1);
        //Plot energy detected in EM during event as a fraction of 
        //the incoming MC energy
        fractMCenergyEM = hf.createHistogram1D(
                "EM Energy detected as fraction of MC particle energy "+calorimeter,
                "percentMCenergyEM "+calorimeter,
                200,0,1);
        //Plot energy detected in H during event as a fraction of 
        //the incoming MC energy
        fractMCenergyH = hf.createHistogram1D(
                "H Energy detected as fraction of MC particle energy "+calorimeter,
                "percentMCenergyH "+calorimeter,
                200,0,1);
        //Plot energy detected by cal[1] as a fraction of
        //E_MC - cal[0]
        //this is mainly for E_H / (E_MC - E_EM), 
        //to give a better idea of how much was detected
        fractRemEnergy = hf.createHistogram1D(
                "E_H / (E_MC - E_EM) "+calorimeter,
                "fractRemEnergy "+calorimeter,
                200,0,1);
        
        HtoEM = hf.createHistogram1D(
                "Ratio of E_H to E_EM ",
                "HtoEM",
                2000,0,1000);
        
        //Plot hit time as a function of radius.
        timeRad = hf.createHistogram2D(
                "Hit time as a function of radius "+calorimeter,
                "timeRad "+calorimeter,
                200,0,10,
                1000,1200,3000);
        //Plot hit time as a function of radius, weight
        //point size by energy
        timeRadxEn = hf.createHistogram2D(
                "Hit time as a function of radius, weighted by energy "+calorimeter,
                "timeRadxEn "+calorimeter,
                100,0,10,
                1000,1200,3000);
        //Plot shower profiles
        //Theta is defined as the angle between the calorimeter's
        //radial vector and the xy plane where z is the beam path
        //(Theta as in spherical polar coordinates)
        //This will be relatively valid until we can get the 
        //tracker vector as a reference
        showerProfile = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane "+calorimeter,
                "showerprofile "+calorimeter,
                100,-10,10);
        //same thing but weighted by energy
        showerProfileEn = hf.createHistogram1D(
                "Shower profiles: angular deviation from xy plane weighted by energy "+calorimeter,
                "showerprofileEn "+calorimeter,
                100,-10,10);
        //Plot raw TPrimes
        TPrimes = hf.createHistogram1D(
                "Raw TPrimes "+calorimeter,
                "TPrimes "+calorimeter,
                200,-.1,.9);
        //Plot TPrimes weighted by energy
        TPrimesEn = hf.createHistogram1D(
                "Tprimes weighted by energy "+calorimeter,
                "TPrimesEn "+calorimeter,
                200,-.1,.9);
    }
    
    
    //count events, make histograms
    public void subProcess(EventHeader event){    
        //Count events processed
        EVENTS++;
        //Get this event's MC energy
        this.addMCE(event);
        //Get detectable energy in each cal
        this.calEn(event, 0);
        this.calEn(event, 1);
        
        this.eventenergy.fill(this.E_CAL[0]+this.E_CAL[1]);
        
        this.fractMCenergy.fill((this.E_CAL[0]+this.E_CAL[1])/this.E_MC);
        this.fractMCenergyEM.fill(this.E_CAL[0]);
        this.fractMCenergyEM.fill(this.E_CAL[1]);
        
        this.fractRemEnergy.fill(this.E_CAL[1]/(this.E_MC-this.E_CAL[0]));
        
        this.HtoEM.fill(this.E_CAL[1]/this.E_CAL[0]);
        
        List<SimCalorimeterHit> calhits = event.get(SimCalorimeterHit.class, this.cal[0]);
        
        for(SimCalorimeterHit hit : calhits){
            
            this.timeRad.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()));
            
            this.timeRadxEn.fill(
                    hit.getTime(),
                    getRadius(hit.getPosition()),
                    hit.getRawEnergy());
            
            this.showerProfile.fill(
                    getTheta(hit.getPosition()));
            
            this.showerProfileEn.fill(
                    getTheta(hit.getPosition()),
                    hit.getRawEnergy());    
            this.TPrimes.fill(getTPrime(hit));
            this.TPrimesEn.fill(
                    getTPrime(hit),
                    hit.getRawEnergy());
        }        
    }
}
