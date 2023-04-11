import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class main {
   
   public static void main(String[] args) throws Exception {
      File dir = new File("SATInstances");
      File[] files = dir.listFiles();
      if (files != null) {
         for (File file : files) {
            FileOutputStream fos = new FileOutputStream("Logs/log_" + file.getName() + ".txt");
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);
            XYSeries series = new XYSeries("Data Series");
            if (file.isFile() && file.getName().endsWith(".cnf")) {
               long startTime = System.currentTimeMillis();
               NBLSolver solver = new NBLSolver(file);
               int sample = 1;
               double mean = 0.0;
               for (int i = 1; i <= sample; i++) {
                  System.out.println("Sample: " + i);
                  mean += solver.check();
                  // if (i % 10 == 0) {
                  //    series.add(i, mean / i);
                  // }
               }
               long endTime = System.currentTimeMillis();
               long elapsedTime = endTime - startTime;
               System.out.println("------------Total Elapsed Time: " + elapsedTime);               
            }
            ps.close();
            fos.close();
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
            // ChartUtils.saveChartAsPNG(new File("Chart/" + file.getName() + "_line-chart.png"), chart, 1000, 600);
         }
      }
   }
}
