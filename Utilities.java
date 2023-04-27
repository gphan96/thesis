import java.math.BigDecimal;
import java.util.List;

public class Utilities {
   

    public Utilities() {
    }

    public boolean checkStop(List<Double> meanList, int n) {
        double delta = 0.0;
        // for (int i = 1; i < meanList.size(); i++) {
        //     double meanPre = Math.abs(meanList.get(i - 1));
        //     double meanCur = Math.abs(meanList.get(i));
        //     delta += Math.abs(meanPre - meanCur);
        // }
        double meanTotal = Math.abs(getMean(meanList));
        double meanCur = Math.abs(meanList.get(meanList.size() - 1));
        delta = Math.abs(meanTotal - meanCur);
        // No change in first n significant digits
        if (getLeadingZero(delta) - getLeadingZero(meanCur) >= n) {
            return true;
        }
        
        return false;
    }

    private int getLeadingZero(double value) {
        String strValue = Double.toString(value);
        int numLeadingZeros = new BigDecimal(strValue)
            .scale() - strValue.indexOf('.') - 1;
        return numLeadingZeros;
    }    

    private double getMean(List<Double> meanList) {        
        // Calculate mean of all mean value
        double sum = 0.0;
        int numMean = meanList.size();
        for (int i = 0; i < numMean; i++) {
           sum += meanList.get(i);
        }
        return (sum / numMean);
}
}
