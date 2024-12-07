package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class creates and handles all operations and components in the CustomDataVisScreen. It
 * allows the user to compare median home values from each of their custom datasets and view
 * predicted data based on a trendline for each dataset.
 *
 * @author Thomas England
 */
public class CustomDataVisScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private ArrayList<ArrayList<String>> datasets;
    private JPanel actionPanel;
    private JButton beginDateBtn;
    private String selectedBeginMonth;
    private JButton viewResultsBtn;

    /**
     * 'Default' Constructor for the CustomDataVisScreen. Constructs a placeholder object of
     * a CustomDataVisScreen to add to a cardPanelLayout for changing screens.
     *
     * @param app App object referencing the App that this screen belongs to
     */
    public CustomDataVisScreen(App app) {
        this.app = app;
    }

    /**
     * Workhorse Constructor for the CustomDataVisScreen. Constructs a CustomDataVisScreen as a
     * JPanel, adds all components and handles all operations within the screen.
     *
     * @param app App object referencing the App that this screen belongs to
     * @param datasets An ArrayList of ArrayLists of Strings containing states or regions. Each
     *                 ArrayList inside the parent ArrayList represents one dataset.
     */
    public CustomDataVisScreen(App app, ArrayList<ArrayList<String>> datasets) {
        // create as JPanel with BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        this.datasets = datasets;

        // create labels and divider label
        JLabel descLabel = componentFactory.createDescLabel("Comparing all custom datasets");
        JLabel descLabel2 =  componentFactory.createDescLabel("Begin by selecting the begin date to pull data from. The end date to stop pulling data is Oct2024.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs");
        JLabel dividerLabel = componentFactory.createDescLabel("------------------------------------");

        // create contentPanel as JPanel and add description label
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);

        // create resetBtn and beginDateBtn
        JButton resetBtn = createResetBtn();
        createBeginDateBtn();

        // create actionPanel and add all components, including all areas from user-created
        // datasets
        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        for (ArrayList<String> dataset : datasets) {
            JLabel setLabel = componentFactory.createDescLabel("Set " + (datasets.indexOf(dataset) + 1) + " ------------------------------");
            actionPanel.add(Box.createVerticalStrut(2));
            actionPanel.add(setLabel);
            actionPanel.add(Box.createVerticalStrut(2));
            for (String area : dataset) {
                JLabel areaLabel = componentFactory.createDescLabel(area);
                actionPanel.add(areaLabel);
            }
        }
        actionPanel.add(dividerLabel);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(beginDateBtn);

        // add actionPanel to contentPanel
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // create backBtn and add to parent JPanel (this)
        JButton backBtn = componentFactory.createBackButton();
        this.add(backBtn, BorderLayout.NORTH);

        // wrap contentPanel in scrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // add scrollPane to parent JPanel (this)
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Called from the workhorse constructor to create the beginDateBtn which allows the user to
     * select a starting date for the data.
     */
    private void createBeginDateBtn() {
        beginDateBtn = new JButton("Select begin date");
        beginDateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        beginDateBtn.addActionListener(e -> createBeginDateDialog());
    }

    /**
     * Called when the user clicks on the beginDateBtn. Displays a dialog prompting the user to
     * select a starting date for the data to be calculated.
     */
    private void createBeginDateDialog() {
        String beginMonth = getBeginMonth();
        int beginMonthNum = DataReader.getNumFromMonth(beginMonth.substring(0, 3));
        int beginYearNum = Integer.parseInt(beginMonth.substring(3));
        ArrayList<String> monthsToChoose = new ArrayList<>();

        for (int i = beginYearNum; i <= 2020; i++) {
            for (int j = (i == beginYearNum ? beginMonthNum : 1); j <= 12; j++) {
                monthsToChoose.add(DataReader.getMonthFromNum(j) + Integer.toString(i));
            }
        }

        JDialog dialog = new JDialog(app.getFrame(), "Select a start month to begin pulling data");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store month options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (String month : monthsToChoose) {
            JButton monthBtn = new JButton(month);
            monthBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            monthBtn.addActionListener(e -> beginMonthSelected(month, dialog));
            panel.add(monthBtn);
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

    /**
     * Called when a user selects a begin month from the dialog prompt. Changes the beginDateBtn
     * text, creates a 'view results' button and adds it to the actionPanel.
     *
     * @param month String containing the user-selected begin month to start pulling data from
     * @param dialog JDialog containing the monthBtn that was clicked
     */
    private void beginMonthSelected(String month, JDialog dialog) {
        // store selected begin month, change text and disable beginDateBtn
        selectedBeginMonth = month;
        dialog.dispose();
        beginDateBtn.setText("Selected begin month: " + selectedBeginMonth);
        beginDateBtn.setEnabled(false);

        // create viewResultsBtn and add to actionPanel
        viewResultsBtn = new JButton("View Results");
        viewResultsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewResultsBtn.addActionListener(e -> viewResults());
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(viewResultsBtn);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Called when the user has selected their desired start month with which to begin pulling data
     * from. Creates a population bar chart containing population densities for all datasets
     * compared to the average density of the US. Creates a line chart containing median home
     * values for each dataset since the user-selected begin month, adds both charts to
     * actionPanel. Uses ComponentFactory to create a 'calculate predicted results' btn which
     * calculates a trendline for each dataset.
     */
    private void viewResults() {
        // disable viewResultsBtn to stop user from accidentally creating more of the same graphs
        viewResultsBtn.setEnabled(false);

        // create populationBarChart and add to actionPanel
        JFreeChart populationBarChart = createPopulationBarChart();
        ChartPanel barChartPanel = new ChartPanel(populationBarChart);
        barChartPanel.setPreferredSize(new Dimension(300, 400));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(barChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        // createzhviLineChart containing median home values and add to actionPanel
        JFreeChart zhviLineChart = createZhviLineChart();
        ChartPanel lineChartPanel = new ChartPanel(zhviLineChart);
        lineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(lineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        // use componentFactory to create calcPredictedBtn to create line chart with all measured
        // and predicted data based on trendline
        componentFactory.createCalcPredictedBtn(actionPanel, lineChartPanel);
    }

    /**
     * Called by viewResults() when the user clicks the viewResultsBtn. Creates a line chart
     * containing all median home value data from every dataset.
     *
     * @return JFreeChart containing an XYLineChart with median home value data
     */
    private JFreeChart createZhviLineChart() {
        // create XYSeriesCollection to store all XYSeries
        XYSeriesCollection finalXYSeriesCollection = new XYSeriesCollection();

        // add an XYSeries to XYSeriesCollection for each dataset in user-created datasets
        for (ArrayList<String> dataset : datasets) {
            // map containing xy coordinates to add to XYSeries
            Map<Double, Double> datasetZhviData = app.getDataReader().getZhviDataFromSet(dataset, selectedBeginMonth);
            XYSeries datasetSeries = new XYSeries("Set " + (datasets.indexOf(dataset) + 1));

            // add all coordinates from map to XYSeries and add XYSeries to XYSeriesCollection
            for (Map.Entry<Double, Double> entry : datasetZhviData.entrySet()) {
                datasetSeries.add(entry.getKey(), entry.getValue());
            }
            finalXYSeriesCollection.addSeries(datasetSeries);
        }

        // create lineChart with XYSeriesCollection
        JFreeChart lineChart = ChartFactory.createXYLineChart(
            "Median Home Values For " + datasets.size() + " Custom Datasets",
            "Date (year)",
            "Median Home Value (USD)",
            finalXYSeriesCollection
        );

        return lineChart;
    }

    /**
     * Called by viewResults() method when user clicks viewResultsBtn. Creates a population bar
     * chart containing average population densities from each dataset compared to the average
     * US state density.
     *
     * @return JFreeChart as a BarChart containing population density data
     */
    private JFreeChart createPopulationBarChart() {
        // create dataset to hold density values
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // add average state density value
        dataset.addValue(
            app.getDataReader().getAvgStateDensity(),
            "Average US State",
            "Average Density"
        );

        // add average density value for each custom dataset
        for (ArrayList<String> set : datasets) {
            dataset.addValue(
                app.getDataReader().getDensityFromSet(set),
                "Custom Dataset",
                "Set " + (datasets.indexOf(set) + 1)
            );
        }

        // create JFreeChart with dataset
        JFreeChart barChart = ChartFactory.createBarChart(
            "Population Densities",
            "Area",
            "Population Density Per Square Kilometer",
            dataset
        );

        return barChart;
    }

    /**
     * Called by createBeginDateDialog() method when the user clicks the beginDateBtn. Retrieves the
     * first available month (meaning the first column without null values) for the user-selected datasets.
     *
     * @return String containing the first available month for the user-created datasets
     */
    private String getBeginMonth() {
        // ArrayList to store begin months from each dataset
        ArrayList<Double> beginMonths = new ArrayList<>();
        // take begin month from each area in each set and add to arraylist
        for (ArrayList<String> dataset : datasets) {
            for (String area : dataset) {
                // if area is a state
                if (area.length() <= 2) {
                    beginMonths.add(DataReader.getDoubleFromMonth(app.getDataReader().getBeginDateFromState(area)));
                // else (area is a region)
                } else {
                    String region = area.substring(0, area.indexOf(',')).trim();
                    String state = area.substring(area.indexOf(',') + 2).trim();
                    beginMonths.add(DataReader.getDoubleFromMonth(app.getDataReader().getBeginDateFromRegion(state, region)));
                }
            }
        }

        // begin month is max begin month from arraylist of begin months
        Double beginMonth = Collections.max(beginMonths);
        return DataReader.getMonthFromDouble(beginMonth);
    }

    /**
     * Called by the workhorse constructor to create a 'Reset all inputs' button which refreshes
     * the screen so all user input is reset
     *
     * @return a JButton 'reset all inputs' which refreshes this screen
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshCustomDataVisScreen(datasets));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }
}
