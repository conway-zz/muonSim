/*
 * This is for functions that analyze single datapoints
 * Good example is t-r/c
 * @author agias
 */

import hep.aida.*;

public abstract class analysisFunctions{
    
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
