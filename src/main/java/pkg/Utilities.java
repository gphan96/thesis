package pkg;

import java.util.List;

public class Utilities {
   

    public Utilities() {
    }

    public boolean checkStop(double meanCur, double meanPre, double threshold) {
        double delta = 0.0;
        delta += Math.abs(meanCur - meanPre);
        // No change in first n significant digits
        if (delta / Math.abs(meanCur) <= threshold) {
            // System.out.println("delta: " + delta);
            // System.out.println("mean1: " + meanCur);
            // System.out.println("mean2: " + meanPre);
            return true;
        }
        return false;
    }

    public double getMean(List<Double> meanList) {        
        // Calculate mean of all mean value
        double sum = 0.0;
        int numMean = meanList.size();
        for (int i = 0; i < numMean; i++) {
           sum += meanList.get(i);
        }
        return (sum / numMean);
    }

    public double getMaxAbs(List<Double> meanList) {
        double max_abs = 0.0;
        for (double mean : meanList) {
            double abs_value = Math.abs(mean);
            if (abs_value > max_abs) {
                max_abs = abs_value;
            }
        }
        return max_abs;
    }
}
