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
   private final int leadingZero;
   private XYSeries series;
   private XYSeriesCollection dataset;
   private JFreeChart chart;

   public Chart(List<BigDecimal> meanList, int leadingZero, String fileName, int step) {
      this.meanList = meanList;
      this.fileName = fileName;
      this.step = step;
      this.leadingZero = leadingZero;
      series = new XYSeries("Data Series");
      draw();
   }

   private void scale() {

   }
   
   private void draw() {
      for (int i = 0; i < meanList.size(); i += step) {
         BigDecimal mean_value = meanList.get(i).movePointRight(leadingZero);
         // System.out.println(mean_value);
         series.add(i, mean_value);
      }
      dataset = new XYSeriesCollection(series);
      chart = ChartFactory.createXYLineChart(
         fileName,
         "Noise samples",
         "S_N mean",
         dataset,
         PlotOrientation.VERTICAL,
         true,
         true,
         false
      );

      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setBackgroundPaint(Color.WHITE);
      plot.setRangeGridlinePaint(Color.GRAY);
      plot.setDomainGridlinePaint(Color.GRAY);
      
      try {
         ChartUtils.saveChartAsPNG(new File("Charts/" + fileName + "_line-chart.png"), chart, 1000, 500);
      } catch (IOException e) {
         System.err.println("Failed to draw chart: " + fileName);
         e.printStackTrace();
      }
   }
}
