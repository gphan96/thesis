package pkg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class Chart {
   private XYSeriesCollection dataset;
   private JFreeChart chart;
   private XYPlot plot;

   public Chart(String fileName, double minRange, double maxRange, double tickUnit) {
      dataset = new XYSeriesCollection();
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
      plot = (XYPlot) chart.getPlot();
      plot.setBackgroundPaint(Color.WHITE);
      plot.setRangeGridlinePaint(Color.GRAY);
      plot.setDomainGridlinePaint(Color.GRAY);
      XYItemRenderer renderer = plot.getRenderer();
      renderer.setSeriesPaint(1, Color.BLUE);

      NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
      yAxis.setRange(minRange, maxRange);
      // yAxis.setTickUnit(new NumberTickUnit(tickUnit));
   }

   public void addSeries(List<BigDecimal> meanList, int leadingZero, int step, String fileName) {
      XYSeries series = new XYSeries(fileName);
      for (int i = 0; i < meanList.size(); i += step) {
         BigDecimal mean_value = meanList.get(i).movePointRight(leadingZero);
         series.add(i, mean_value);
      }
      dataset.addSeries(series);
   }

   public void addRegression(double slope, double intercept, int leadingZero, int start, int end) {
      XYSeries trend = new XYSeries("Trend");
      trend.add(start, (slope * start + intercept) * Math.pow(10, leadingZero));
      trend.add(end, (slope * end + intercept) * Math.pow(10, leadingZero));
      dataset.addSeries(trend);
   }

   public void modifySerieOne(int delta) {
      XYSeries serieOne = dataset.getSeries(0);
      int countSerieOne = serieOne.getItemCount();
      for (int i = 0; i < countSerieOne; i++) {
         BigDecimal value = ((BigDecimal) serieOne.getY(i)).movePointRight(delta);
         serieOne.updateByIndex(i, value);
      }
   }

   public void addMarker(String label, double value, Color color, TextAnchor anchor) {
      ValueMarker marker = new ValueMarker(value);
      marker.setLabel(label);
      marker.setLabelAnchor(RectangleAnchor.RIGHT);
      marker.setLabelTextAnchor(anchor);
      BasicStroke markerStroke = new BasicStroke(1.1f);
      marker.setPaint(color);
      marker.setStroke(markerStroke);
      plot.addRangeMarker(marker);
   }

   public void setTitle(String title) {
      chart.setTitle(title);
   }
   
   public void drawToFile(String name) {
      try {
         int width = 512;
         int height = 512;
         File file = new File("Charts/" + name + "_chart.png");
         if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
         }
         BufferedImage image = chart.createBufferedImage(width, height);
         ImageIO.write(image, "png", file);
         // ChartUtils.saveChartAsPNG(new File("Charts/" + index + "_line-chart.png"), chart, width, height);
      } catch (IOException e) {
         System.err.println("Failed to draw chart");
         e.printStackTrace();
      }
   }

   public void drawToSVG(int delta) {
      DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
      Document document = domImpl.createDocument(null, "svg", null);
      // Create an instance of SVGGraphics2D
      SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

      // Render the chart as SVG
      int width = 512;
      int height = 512;
      Rectangle2D chartArea = new Rectangle2D.Double(0, 0, width, height);
      chart.draw(svgGenerator, chartArea);

      // Export the SVG as a file
      File svgFile = new File("Charts/chart_" + delta + ".svg");
      try (OutputStream outputStream = new FileOutputStream(svgFile)) {
         Writer out = new OutputStreamWriter(outputStream, "UTF-8");
         svgGenerator.stream(out, true /* useCss */);
         outputStream.flush();
         outputStream.close();
      } catch (IOException e) {
         System.err.println("Failed to draw chart");
          e.printStackTrace();
      }
   }
}
