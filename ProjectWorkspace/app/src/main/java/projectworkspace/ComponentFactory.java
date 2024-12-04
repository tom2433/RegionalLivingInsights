package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ComponentFactory {
    private static App app;
    // these static variables will be reset to null after they are used
    private static JButton calcPredictedBtn;
    private static JDialog dialog;
    private static JPanel actionPanel;
    private static ChartPanel lineChartPanel;
    private static int numYears;

    public static void reset() {
        calcPredictedBtn = null;
        dialog = null;
        actionPanel = null;
        lineChartPanel = null;
        numYears = 0;
    }

    public static JLabel createDescLabel(String text) {
        JLabel descLabel = new JLabel(text);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return descLabel;
    }

    public static JButton createBackButton(App app) {
        ComponentFactory.app = app;
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(app);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setVerticalAlignment(SwingConstants.CENTER);
        return backButton;
    }

    public static JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(createEmptyBorder(20, 20, 20, 20));
        return contentPanel;
    }

    public static void createCalcPredictedBtn(JPanel actionPanel, ChartPanel lineChartPanel) {
        ComponentFactory.actionPanel = actionPanel;
        ComponentFactory.lineChartPanel = lineChartPanel;
        calcPredictedBtn = new JButton("Calculate Predicted Data");
        calcPredictedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcPredictedBtn.addActionListener(e -> createPredictedDataDialog());

        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(calcPredictedBtn);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private static void createPredictedDataDialog() {
        dialog = new JDialog(app.getFrame(), "Select the number of years to predict");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (int i = 1; i <= 10; i++) {
            JButton numYearsBtn = new JButton();
            numYearsBtn.setText(i == 1 ? "1 year" : i + " years");
            numYearsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            numYearsBtn.addActionListener(e -> numYearsSelected(numYearsBtn));
            panel.add(numYearsBtn);
            panel.add(Box.createVerticalStrut(10));
        }

        // wrap panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        // add scroll pane to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // show dialog
        // center relative to parent frame
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setVisible(true);
    }

    private static void numYearsSelected(JButton numYearsBtn) {
        String series1name;
        String series2name;

        numYears = Integer.parseInt(numYearsBtn.getText().substring(0, 1));

        // check if numYears is 10 (else 10 would be recognized as 1 from substring)
        if (numYearsBtn.getText().substring(0, 2).equals("10")) {
            numYears = 10;
        }

        dialog.dispose();
        calcPredictedBtn.setText("Calculated for " + numYearsBtn.getText());

        JFreeChart lineChart = lineChartPanel.getChart();
        XYPlot plot = (XYPlot) lineChart.getPlot();
        XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
        XYSeries series1 = dataset.getSeries(0);
        XYSeries series2 = dataset.getSeries(1);
        series1name = (String) series1.getKey();
        series2name = (String) series2.getKey();

        XYSeries newSeries1 = new XYSeries(series1name);
        XYSeries newSeries2 = new XYSeries(series2name);
        for (int i = 0; i < series1.getItemCount(); i++) {
            newSeries1.add(series1.getX(i), series1.getY(i));
            newSeries2.add(series2.getX(i), series2.getY(i));
        }

        calcFutureCoords(newSeries1);
        calcFutureCoords(newSeries2);
        XYSeries divider = new XYSeries("Divider");
        double maxValSeries1 = getMaxValFromSeries(newSeries1);
        double maxValSeries2 = getMaxValFromSeries(newSeries2);
        divider.add(2025, 0);
        divider.add(2025, (maxValSeries1 > maxValSeries2 ? maxValSeries1 : maxValSeries2));

        XYSeriesCollection newDataset = new XYSeriesCollection();
        newDataset.addSeries(newSeries1);
        newDataset.addSeries(newSeries2);
        newDataset.addSeries(divider);

        JFreeChart newLineChart = ChartFactory.createXYLineChart(
            "Median Home Values for " + newSeries1.getKey() + " vs. " + newSeries2.getKey() + " +" + numYears + " years",
            "Date (Year)",
            "Median Home Value (USD)",
            newDataset
        );
        ChartPanel newLineChartPanel = new ChartPanel(newLineChart);
        newLineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(newLineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private static double getMaxValFromSeries(XYSeries series) {
        double maxY = 0.0;

        for (int i = 0; i < series.getItemCount(); i++) {
            if ((double) series.getY(i) > maxY) {
                maxY = (double) series.getY(i);
            }
        }

        return maxY;
    }

    private static void calcFutureCoords(XYSeries series) {
        // n = number of coordinates
        int n = series.getItemCount();

        // sum of all x vals, sum of all y, sum of product of all corresponding x and y, sum of
        // squares of all x values
        double sumX = 0,
            sumY = 0,
            sumXY = 0,
            sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += (double) series.getX(i);
            sumY += (double) series.getY(i);
            sumXY += (double) series.getX(i) * (double) series.getY(i);
            sumX2 += (double) series.getX(i) * (double) series.getX(i);
        }

        // calculate slope (the 'm' in y = mx + b)
        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        // calculate y-axis intercept (the 'b' in y = mx + b)
        double b = (sumY - m * sumX) / n;

        // start calculating trendline at last x value
        double lastX = series.getX(n - 1).doubleValue();
        for (int i = 1; i <= numYears * 12; i++) {
            double x = lastX + (i / 12.0);
            series.add(x, m * x + b);
        }
    }
}
