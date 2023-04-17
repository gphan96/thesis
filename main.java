import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class main {
   
   public static void main(String[] args) throws Exception {
      File dir = new File("SATInstances");
      File[] files = dir.listFiles();
      if (files != null) {
         for (File file : files) {            
            if (file.isFile() && file.getName().endsWith(".cnf")) {
               System.out.println(file.getName());
               XYSeries series = new XYSeries("Data Series");
               int totalSample = 1000000;
               BigDecimal sum = BigDecimal.ZERO;
               BigDecimal mean = BigDecimal.ZERO;
               List<BigDecimal> meanList = new ArrayList<>();
               DecimalFormat df = new DecimalFormat("0.##########E0");
               //-- Execution --
               System.out.println("1");
               long startTime1 = System.currentTimeMillis();
               NBLSolver solver = new NBLSolver(file);
               long startTime2 = System.currentTimeMillis();
               for (int i = 1; i <= totalSample; i++) {
                  sum = sum.add(solver.check());
                  if (i % 50 == 0) {
                     BigDecimal sample = new BigDecimal(i);
                     mean = sum.divide(sample, RoundingMode.HALF_UP);
                     meanList.add(mean);
                  }
               }
               long endTime = System.currentTimeMillis();
               //-- Scale up for drawing chart --
               System.out.println("2");
               int numDigits = mean.precision();
               int numTrailingZeros = mean.scale();
               int numLeadingDigits = numTrailingZeros - numDigits + 1;
               for (BigDecimal mean_value : meanList) {
                  BigDecimal value_new = mean_value.multiply(BigDecimal.TEN.pow(numLeadingDigits));
                  series.add(meanList.indexOf(mean_value), value_new);                  
               }
               //-- Elapsed time --
               long lTime = startTime2 - startTime1;
               long pTime = endTime - startTime2;
               long tTime = lTime + pTime;
               System.out.println("------------Loading: " + lTime + " ms");
               System.out.println("------------Processing: " + pTime + " ms");
               System.out.println("------------Total: " + tTime + " ms");
               //-- Draw Chart --
               XYSeriesCollection dataset = new XYSeriesCollection(series);
               JFreeChart chart = ChartFactory.createXYLineChart(
                  "",
                  "Noise samples",
                  "S_N mean",
                  dataset,
                  PlotOrientation.VERTICAL,
                  true,
                  true,
                  false
               );
               chart.setBackgroundPaint(Color.white);
               XYPlot plot = (XYPlot) chart.getPlot();
               NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
               yAxis.setNumberFormatOverride(df);
               // yAxis.setRange(new Range(series.getMinY(), series.getMaxY()));
               ChartUtils.saveChartAsPNG(new File("Chart/" + file.getName() + "_line-chart.png"), chart, 1000, 500);
            }
         }
      }
   }
}
