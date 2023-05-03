package pkg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Utilities {
   

    public Utilities() {
    }

    public boolean checkStop(BigDecimal meanCur, BigDecimal meanPre, int threshold) {
        BigDecimal delta = meanCur.subtract(meanPre).abs();
        // No change in first n significant digits
        if (getLeadingZero(delta) - getLeadingZero(meanCur) >= threshold) {
            return true;
        }
        return false;
    }

    public int getLeadingZero(BigDecimal value) {
        int numDigits = value.precision();
        int numTrailingZeros = value.scale();
        return numTrailingZeros - numDigits + 1;
    }

    private BigDecimal getNewScale(BigDecimal value) {
        int n = 20; // Number of desire significant digit
        int newScale = n - value.precision() + value.scale();
        return value.setScale(newScale, RoundingMode.HALF_UP);
    }

    public BigDecimal getMean(List<BigDecimal> meanList) {        
        // Calculate mean of all mean value
        BigDecimal sum = BigDecimal.ZERO;
        int numMean = meanList.size();
        for (int i = 0; i < numMean; i++) {
           sum = sum.add(meanList.get(i));
        }
        return sum.divide(BigDecimal.valueOf(numMean), RoundingMode.HALF_UP);
    }

    public BigDecimal getMaxAbs(List<BigDecimal> meanList) {
        BigDecimal max_abs = BigDecimal.ZERO;
        for (BigDecimal mean : meanList) {
            BigDecimal abs_value = mean.abs();
            if (abs_value.compareTo(max_abs) > 0) {
                max_abs = abs_value;
            }
        }
        return max_abs;
    }
}