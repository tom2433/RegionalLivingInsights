package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
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
 * This class creates all components and handles all operations within the RegionDataVisScreen
 * which the user sees after selecting two regions to compare.
 *
 * @author Thomas England
 */
public class RegionDataVisScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private String state1;
    private String state2;
    private String region1;
    private String region2;
    private String selectedBeginMonth;
    private boolean region1Incorporated;
    private boolean region2Incorporated;
    private JButton beginDateBtn;
    private JButton viewResultsBtn;
    private JPanel actionPanel;

    /**
     * Default constructor for the RegionDataVisScreen. Creates an empty RegionDataVisScreen
     * object belonging to a specified App.
     *
     * @param app App referencing the App that this screen belongs to.
     */
    public RegionDataVisScreen(App app) {
        this.app = app;
    }

    /**
     * Workhorse constructor for the RegionDataVisScreen. Creates a RegionDataVisScreen object
     * with all components and operations. Allows the user to select a beginning month with which
     * to begin pulling data from, and displays all graphs associated with the comparison.
     *
     * @param app App referencing the App that this screen belongs to
     * @param state1 String containing the state initials for the first region in comparison
     * @param state2 String containing the region name of the first region in comparison
     * @param region1 String containing the state initials for the second region in comparison.
     * @param region2 String containing the region name of the first region comparison.
     */
    public RegionDataVisScreen(App app, String state1, String state2, String region1, String region2) {
        // create this screen as a child of JPanel with a BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        this.state1 = state1;
        this.state2 = state2;
        this.region1 = region1;
        this.region2 = region2;
        this.region1Incorporated = app.getDataReader().getIncorporatedStatus(state1, region1);
        this.region2Incorporated = app.getDataReader().getIncorporatedStatus(state2, region2);

        // create description and instruction labels
        JLabel descLabel = componentFactory.createDescLabel(
            "Comparing " + region1 + ", " + state1 + (region1Incorporated ? " (Incorporated) vs. " : " (Unincorporated) vs. ")
            + region2 + ", " + state2 + (region2Incorporated ? " (Incorporated):" : " (Unincorporated):")
        );
        JLabel descLabel2 = componentFactory.createDescLabel("Begin by selecting the begin date to pull data from. The end date to stop pulling data is Oct2024.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");

        // create contentpanel to hold all components
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);

        // create reset inputs button and beginDate button
        JButton resetBtn = createResetBtn();
        createBeginDateBtn();

        // create actionPanel to store all dynamic components (ones that update/change)
        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(beginDateBtn);

        // add actionPanel to contentPanel
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // create back button and add to parent JPanel (this)
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
     * Called from the constructor to create a JButton 'Reset all inputs' which refreshes the
     * screen and resets all user input.
     *
     * @return a JButton that refreshes the screen to reset user input
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshRegionDataVisScreen(state1, state2, region1, region2));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    /**
     * Called from the constructor to create a JButton 'Select begin date' which allows the user to
     * select from a list of available begin dates to start pulling data from.
     */
    private void createBeginDateBtn() {
        beginDateBtn = new JButton("Select begin date");
        beginDateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        beginDateBtn.addActionListener(e -> createBeginDateDialog());
    }

    /**
     * Called when the beginDateBtn is clicked. Creates a JDialog to prompt the user for a start
     * date to begin pulling data from for the line graphs.
     */
    private void createBeginDateDialog() {
        // retrieve begin date from DataReader
        String beginMonth = app.getDataReader().getBeginDateFromRegions(state1, region1, state2, region2);
        int beginMonthNum = DataReader.getNumFromMonth(beginMonth.substring(0, 3));
        int beginYearNum = Integer.parseInt(beginMonth.substring(3));
        ArrayList<String> monthsToChoose = new ArrayList<>();

        // create list of available start dates, starting at first available month and ending on Dec2020
        for (int i = beginYearNum; i <= 2020; i++) {
            for (int j = (i == beginYearNum ? beginMonthNum : 1); j <= 12; j++) {
                monthsToChoose.add(DataReader.getMonthFromNum(j) + Integer.toString(i));
            }
        }

        // create JDialog
        JDialog dialog = new JDialog(app.getFrame(), "Select a start month to begin pulling data");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store month options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add each available month as buttons to the panel
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
     * Called whenever an available month is chosen by the user from the JDialog created in the
     * createBeginDateDialog() method. Sets the selected begin month for this class to the
     * user-selected begin month from the JDialog and adds a 'view results' button to the
     * actionPanel.
     *
     * @param month String containing the user-selected begin month
     * @param dialog JDialog referencing the dialog that the user selected the begin date from
     */
    private void beginMonthSelected(String month, JDialog dialog) {
        beginDateBtn.setEnabled(false);
        selectedBeginMonth = month;
        System.out.println("Begin month selected: " + month);
        dialog.dispose();
        beginDateBtn.setText("Selected begin month: " + selectedBeginMonth);
        beginDateBtn.setEnabled(false);

        viewResultsBtn = new JButton("View Results");
        viewResultsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewResultsBtn.addActionListener(e -> viewResults());

        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(viewResultsBtn);
    }

    /**
     * Called whenever the user clicks the 'view results' button created in the
     * beginMonthSelected() method after choosing a beginDate. Creates a population density bar
     * chart, a median home value comparison line chart, and uses ComponentFactory to create a
     * 'calculate predicted results' button which creates a new linechart with a trendline to show
     * future data.
     */
    private void viewResults() {
        beginDateBtn.setEnabled(false);
        viewResultsBtn.setEnabled(false);

        JFreeChart populationBarChart = createPopulationBarChart();
        ChartPanel barChartPanel = new ChartPanel(populationBarChart);
        barChartPanel.setPreferredSize(new Dimension(300, 400));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(barChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        JFreeChart zhviLineChart = createZhviLineChart();
        ChartPanel lineChartPanel = new ChartPanel(zhviLineChart);
        lineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(lineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        componentFactory.createCalcPredictedBtn(actionPanel, lineChartPanel);
    }

    /**
     * Called by the viewResults() method when the user clicks the viewResultsBtn. Creates a line
     * chart with 2 XYSeries containing zhvi data for each region.
     *
     * @return JFreeChart containing zhvi data for each region
     */
    private JFreeChart createZhviLineChart() {
        Map<Double, Double> region1zhviData = app.getDataReader().getZhviDataFromRegion(region1, state1, selectedBeginMonth);
        Map<Double, Double> region2zhviData = app.getDataReader().getZhviDataFromRegion(region2, state2, selectedBeginMonth);
        XYSeries region1Series = new XYSeries(region1 + ", " + state1);
        XYSeries region2Series = new XYSeries(region2 + ", " + state2);
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (Map.Entry<Double, Double> entry : region1zhviData.entrySet()) {
            region1Series.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Double, Double> entry : region2zhviData.entrySet()) {
            region2Series.add(entry.getKey(), entry.getValue());
        }

        dataset.addSeries(region1Series);
        dataset.addSeries(region2Series);

        JFreeChart lineChart = ChartFactory.createXYLineChart(
            "Median home values for " + region1 + ", " + state1 + " vs. " + region2 + ", " + state2,
            "Date (Year)",
            "Median home value (USD)",
            dataset
        );

        return lineChart;
    }

    /**
     * Called by viewResults() method when the user clicks the viewResultsBtn. Creates a bar chart
     * with 3 population density values: one for the average US region, one for the first
     * user-selected region, and one for the second.
     *
     * @return JFreeChart containing population density values for avg region, region1, and region2
     */
    private JFreeChart createPopulationBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(
            app.getDataReader().getAvgRegionDensity(),
            "Average US Region",
            "Average Density"
        );
        dataset.addValue(
            app.getDataReader().getDensityFromArea(region1 + ", " + state1),
            "Selected US Region",
            region1 + ", " + state1);
        dataset.addValue(
            app.getDataReader().getDensityFromArea(region2 + ", " + state2),
            "Selected US Region",
            region2 + ", " + state2
        );

        JFreeChart barChart = ChartFactory.createBarChart(
            "Population Density",
            "Region",
            "Population Density Per Square Kilometer",
            dataset
        );

        return barChart;
    }
}