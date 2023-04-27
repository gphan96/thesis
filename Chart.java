import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart {
   private final List<Double> meanList;
   private final String fileName;
   private final int step;
   private XYSeries series;
   private XYSeriesCollection dataset;
   private JFreeChart chart;

   public Chart(List<Double> meanList, String fileName, int step) {
      this.meanList = meanList;
      this.fileName = fileName;
      this.step = step;
      series = new XYSeries("Data Series");
      draw();
   }
   
   private void draw() {
      for (int i = 0; i < meanList.size(); i += step) {
         double mean_value = meanList.get(i);
         series.add(i, mean_value);
      }
      dataset = new XYSeriesCollection(series);
      chart = ChartFactory.createXYLineChart(
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
      try {
         ChartUtils.saveChartAsPNG(new File("Chart/" + fileName + "_line-chart.png"), chart, 1000, 500);
      } catch (IOException e) {
         System.err.println("Failed to draw chart: " + fileName);
         e.printStackTrace();
      }
   }
}
