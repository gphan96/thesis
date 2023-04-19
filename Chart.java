import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart {
   private final List<BigDecimal> meanList;
   private final String fileName;
   private XYSeries series;
   private XYSeriesCollection dataset;
   private JFreeChart chart;

   public Chart(List<BigDecimal> meanList, String fileName) {
      this.meanList = meanList;
      this.fileName = fileName;
      series = new XYSeries("Data Series");
      draw();
   }
   
   private void draw() {
      for (BigDecimal mean_value : meanList) {
         series.add(meanList.indexOf(mean_value), mean_value);
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
