package pkg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart {
   private final List<BigDecimal> meanList;
   private final String fileName;
   private final int step;
   private final BigDecimal totalMean;
   private final BigDecimal tolerance;
   private XYSeries series;
   private XYSeriesCollection dataset;
   private JFreeChart chart;

   public Chart(List<BigDecimal> meanList,BigDecimal totalMean,BigDecimal tolerance, String fileName, int step) {
      this.meanList = meanList;
      this.fileName = fileName;
      this.step = step;
      this.totalMean = totalMean;
      this.tolerance = tolerance;
      series = new XYSeries("Data Series");
      draw();
   }

   private void scale() {

   }
   
   private void draw() {
      for (int i = 0; i < meanList.size(); i += step) {
         BigDecimal mean_value = meanList.get(i);
         // System.out.println(mean_value);
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

      BasicStroke markerStroke = new BasicStroke(1.1f);

      ValueMarker marker1 = new ValueMarker(totalMean.doubleValue());
      marker1.setPaint(Color.blue);
      marker1.setStroke(markerStroke);
      ValueMarker marker2 = new ValueMarker(tolerance.doubleValue());
      marker2.setPaint(Color.black);
      marker2.setStroke(markerStroke);
      ValueMarker marker3 = new ValueMarker(- tolerance.doubleValue());
      marker3.setPaint(Color.black);
      marker3.setStroke(markerStroke);

      XYPlot plot = (XYPlot) chart.getPlot();

      plot.addRangeMarker(marker1);
      plot.addRangeMarker(marker2);
      plot.addRangeMarker(marker3);
      
      try {
         ChartUtils.saveChartAsPNG(new File("Chart/" + fileName + "_line-chart.png"), chart, 1000, 500);
      } catch (IOException e) {
         System.err.println("Failed to draw chart: " + fileName);
         e.printStackTrace();
      }
   }
}
