import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
               int totalSample = 100;
               NBLSolver solver = new NBLSolver(file);
               BigDecimal sum = BigDecimal.ZERO;
               long startTime = System.currentTimeMillis();
               for (int i = 1; i <= totalSample; i++) {
                  sum = sum.add(solver.check());
                  if (i % 1 == 0) {
                     BigDecimal sample = new BigDecimal(i);
                     BigDecimal mean = sum.divide(sample, RoundingMode.HALF_UP);
                     // series.add(sample, mean);
                     DecimalFormat df = new DecimalFormat("0.#E0");
                     System.out.println(df.format(mean));
                  }
               }
               long endTime = System.currentTimeMillis();
               long elapsedTime = endTime - startTime;
               System.out.println("------------Total Elapsed Time: " + elapsedTime);
               //-- Draw Chart --
               // XYSeriesCollection dataset = new XYSeriesCollection(series);
               // JFreeChart chart = ChartFactory.createXYLineChart(
               //    "",
               //    "Noise samples",
               //    "S_N mean",
               //    dataset,
               //    PlotOrientation.VERTICAL,
               //    true,
               //    true,
               //    false
               // );
               // chart.setBackgroundPaint(Color.white);
               // XYPlot plot = (XYPlot) chart.getPlot();
               // NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
               // NumberFormat format = new DecimalFormat("0.#E0");
               // yAxis.setNumberFormatOverride(format);
               // yAxis.setRange(new Range(series.getMinY(), series.getMaxY()));
               // ChartUtils.saveChartAsPNG(new File("Chart/" + file.getName() + "_line-chart.png"), chart, 1000, 500);
            }
         }
      }
   }
}
