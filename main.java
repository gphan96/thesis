import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

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
               XYSeries series = new XYSeries("Data Series");
               int totalSample = 1000000;
               BigDecimal sum = BigDecimal.ZERO;
               BigDecimal mean = BigDecimal.ZERO;
               //
               long startTime1 = System.currentTimeMillis();
               NBLSolver solver = new NBLSolver(file);
               long startTime2 = System.currentTimeMillis();
               for (int i = 1; i <= totalSample; i++) {
                  System.out.println("Sample " + i);
                  sum = sum.add(solver.check());
                  if (i % 10 == 0 && i < totalSample / 2) {
                     BigDecimal sample = new BigDecimal(i);
                     mean = sum.divide(sample, RoundingMode.HALF_UP);
                     series.add(i, mean);
                  }
               }
               long endTime = System.currentTimeMillis();
               //
               long lTime = startTime2 - startTime1;
               long pTime = endTime - startTime2;
               long tTime = lTime + pTime;
               //
               DecimalFormat df = new DecimalFormat("0.#E0");
               System.out.println(df.format(mean)); 
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
               ChartUtils.saveChartAsPNG(new File("line-chart.png"), chart, 1000, 500);
            }
         }
      }
   }
}
