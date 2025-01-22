package projectworkspace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all data fetches and processing with the data from the database.
 *
 * @author Thomas England
 */
public class DataReader {
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    /**
     * Establishes a connection to the regions database on localhost server using DriverManager to
     * allow usage of statement to execute queries.
     *
     * @throws SQLException in the event of an error in connecting to the database. This method
     * must only be called from within a block with an SQLException handler.
     */
    private void establishConnection() throws SQLException {
        String username = "root";
        String password = "B@nd1t03";

        connection = DriverManager.getConnection("jdbc:mysql://localhost/regions?" +
                "user=" + username + "&password=" + password);

        statement = connection.createStatement();
    }

    /**
     * Retrieves the average population density from a custom dataset containing both regions and
     * states.
     *
     * @param set ArrayList of Strings containing regions or states
     * @return an int containing the average population density out of all regions and states in
     * the given set
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public int getDensityFromSet(ArrayList<String> set) {
        // int to store average overall density to return
        int avgDensity = 0;

        // build query to select avg population density
        StringBuilder query = new StringBuilder(
            "SELECT AVG(PopulationDensity) AS 'PopulationDensity' " +
            "FROM region_populations " +
            "INNER JOIN regions " +
            "ON region_populations.RegionID = regions.RegionID " +
            "WHERE"
        );

        // add to query to selected only the rows that are a part of the dataset
        for (String area : set) {
            if (area.length() == 2) {
                query.append(" StateName = '").append(area).append("'");
            } else {
                String region = area.substring(0, area.indexOf(',')).trim();
                String state = area.substring(area.indexOf(',') + 2).trim();
                query.append(" (RegionName = '").append(region).append("' && StateName = '").append(state).append("')");
            }
            if (set.indexOf(area) != set.size() - 1) {
                query.append(" ||");
            } else {
                query.append(";");
            }
        }

        // attempt to retrieve avg population density from database using query
        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());

            resultSet.next();
            avgDensity = (int) resultSet.getDouble("PopulationDensity");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return the avg density from all areas in dataset
        return avgDensity;
    }

    /**
     * Retrieves the population density from a specified region
     *
     * @param region String containing the name of the region to fetch the population density from
     * @param state String containing the name of the state that the region belongs to
     * @return double containing the population density per square kilometer for specified region
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public double getRegionDensity(String region, String state) {
        // double to store region population density to return
        double regionDensity = 0.0;

        // attempt to retrieve population density value for given RegionName and StateName from
        // database
        try {
            resultSet = statement.executeQuery("""
                    SELECT region_populations.PopulationDensity, regions.RegionName, regions.StateName
                    FROM region_populations
                    INNER JOIN regions
                    ON region_populations.RegionID = regions.RegionID
                    WHERE regions.StateName = '""" + state + "' && RegionName = '" + region + "';"
            );

            resultSet.next();
            regionDensity = (double) resultSet.getInt("PopulationDensity");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return population density value from given region
        return regionDensity;
    }

    /**
     * Retrieves the ZHVI data for each month from a given set of both regions and states for a
     * specified time period starting at the specified startMonth and ending at the last available
     * month. The returned map holds keys as X values (the month when the ZHVI was read as a
     * double value) and values as Y values (the ZHVI for that given month).
     *
     * @param dataset ArrayList of Strings containing the dataset with regions and/or states to
     *                retrieve ZHVI data from
     * @param startMonth String containing the month to begin pulling data from
     * @return a HashMap with keys as X values (months) and values as Y values (ZHVI values)
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromSet(ArrayList<String> dataset, String startMonth) {
        // retrieve number of remaining months including startMonth
        int numMonths = getNumOfRemainingMonths(startMonth);
        // HashMap to store XY values
        Map<Double, Double> XYSeriesData = new HashMap<>();
        // String to store AVG(month1), AVG(month2), ... for use in query
        String selectAllMonthsString = createSelectAllMonthsString(startMonth);
        // double to store last non null value in case nulls are found as y values
        double lastNonNullValue = 0.0;
        // String array to hold all remaining months including startMonth
        String[] months = getMonthsArray(startMonth);

        // begin building query to select avg() of all months
        StringBuilder query = new StringBuilder(
            "SELECT " + selectAllMonthsString + " " +
            "FROM region_zhvi_values " +
            "INNER JOIN regions " +
            "ON region_zhvi_values.RegionID = regions.RegionID " +
            "WHERE"
        );

        // add to query to select for each area (region or state) in dataset
        for (String area : dataset) {
            // if current area is a state
            if (area.length() == 2) {
                query.append(" StateName = '").append(area).append("'");
                // else (current area not a state)
            } else {
                String region = area.substring(0, area.indexOf(',')).trim();
                String state = area.substring(area.indexOf(',') + 2).trim();
                query.append(" (StateName = '").append(state).append("' && RegionName = '").append(region).append("')");
            }
            if (dataset.indexOf(area) != dataset.size() - 1) {
                query.append(" ||");
            } else {
                query.append(";");
            }
        }

        // attempt to retrieve all x and y values from table given by query
        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            // add each x and y value to HashMap, if y is null, set y as last non null value
            for (int i = 0; i < numMonths; i++) {
                double yVal = resultSet.getDouble(months[i]);
                if (yVal == 0.0) {
                    yVal = lastNonNullValue;
                } else {
                    lastNonNullValue = yVal;
                }
                XYSeriesData.put(getDoubleFromMonth(months[i]), yVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return HashMap with XY values
        return XYSeriesData;
    }

    /**
     * Retrieves the ZHVI data for each month from a given state for a specified time period
     * starting at the specified startMonth and ending at the last available month. The returned
     * HashMap holds keys as X values (the month when the ZHVI was read as a double value) and
     * values as Y values (the ZHVI for that given month).
     *
     * @param state String containing the state initials to retrieve ZHVI data from
     * @param startMonth String containing the month to begin pulling data from
     * @return a HashMap with keys as X values (months) and values as Y values (ZHVI values)
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromState(String state, String startMonth) {
        // structure similar to getZhviDataFromSet()
        int numMonths = getNumOfRemainingMonths(startMonth);
        Map<Double, Double> XYSeriesData = new HashMap<>();
        String selectAllMonthsString = createSelectAllMonthsString(startMonth);
        double lastNonNullValue = 0.0;
        String[] months = getMonthsArray(startMonth);

        // build query to select avgs from all months where StateName is the specified state
        StringBuilder query = new StringBuilder(
            "SELECT " + selectAllMonthsString + " " +
            "FROM region_zhvi_values " +
            "INNER JOIN regions " +
            "ON region_zhvi_values.RegionID = regions.RegionID " +
            "WHERE StateName = '" + state + "';"
        );

        // attempt to retrieve all x and y values from table given by query
        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            // add each x and y value to HashMap, if y is null, set y to last non null value
            for (int i = 0; i < numMonths; i++) {
                double yVal = resultSet.getDouble(months[i]);
                if (yVal == 0.0) {
                    yVal = lastNonNullValue;
                } else {
                    lastNonNullValue = yVal;
                }
                XYSeriesData.put(getDoubleFromMonth(months[i]), yVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return HashMap with XY values
        return XYSeriesData;
    }

    /**
     * Retrieves the ZHVI data for each month from a given region for a specified time period
     * starting at the specified startMonth and ending at the last available month. The returned
     * HashMap holds keys as X values (the month when the ZHVI was read as a double value) and
     * values as Y values (the ZHVI for that given month).
     *
     * @param region String containing the region to retrieve ZHVI data from
     * @param state String containing the state initials which the region belongs to
     * @param startMonth String containing the month to begin pulling data from
     * @return a HashMap with keys as X values (months) and values as Y values (ZHVI values)
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromRegion(String region, String state, String startMonth) {
        // structure similar to getZhviDataFromState()
        int numMonths = getNumOfRemainingMonths(startMonth);
        Map<Double, Double> XYSeriesData = new HashMap<>();
        String selectAllMonthsString = createSelectAllMonthsString(startMonth);
        double lastNonNullValue = 0.0;
        String[] months = getMonthsArray(startMonth);

        StringBuilder query = new StringBuilder(
            "SELECT " + selectAllMonthsString + " " +
            "FROM region_zhvi_values " +
            "INNER JOIN regions " +
            "ON region_zhvi_values.RegionID = regions.RegionID " +
            "WHERE StateName = '" + state + "' && RegionName = '" + region + "';"
        );

        // attempt to retrieve all x and y values from table given by query
        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            // add each x and y value to HashMap, if y is null, set y to last non null value
            for (int i = 0; i < numMonths; i++) {
                double yVal = resultSet.getDouble(months[i]);
                if (yVal == 0.0) {
                    yVal = lastNonNullValue;
                } else {
                    lastNonNullValue = yVal;
                }
                XYSeriesData.put(getDoubleFromMonth(months[i]), yVal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return HashMap with XY values
        return XYSeriesData;
    }

    /**
     * Retrieves the average region population density out of all regions in database as a double.
     *
     * @return double containing the average region density out of all regions
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public double getAvgRegionDensity() {
        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT AVG(PopulationDensity)
                FROM regions.region_populations;
            """);

            resultSet.next();
            return resultSet.getDouble("AVG(PopulationDensity)");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return 0.0;
    }

    /**
     * Retrieves the average state population density out of all states in database as an int.
     *
     * @return int containing the average state density out of all states
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public int getAvgStateDensity() {
        int avgStateDensity = 0;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT AVG(AvgDensity)
                FROM statedensityview;
            """);

            resultSet.next();
            avgStateDensity = (int) resultSet.getDouble("AVG(AvgDensity)");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return avgStateDensity;
    }

    /**
     * Retrieves the average state population density for one specified state.
     *
     * @param state String containing the state initials with which to retrieve average population
     *              density value from
     * @return int containing the avg population density for the specified state
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public int getStateDensity(String state) {
        int avgRegionDensity = 0;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
            SELECT AvgDensity
            FROM statedensityview
            WHERE StateName = '""" + state + "';");

            resultSet.next();
            double avgDensity = resultSet.getDouble("AvgDensity");
            avgRegionDensity = (int) avgDensity;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return avgRegionDensity;
    }

    /**
     * Creates an array of strings containing all regions for a specified state.
     *
     * @param stateName String containing the state initials with which to retrieve regions from
     * @return an array of Strings containing all regions for the specified state
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String[] getRegionsFromState(String stateName) {
        // arrayList to store regions
        ArrayList<String> regionsList = new ArrayList<>();
        // array to return
        String[] regionsArray;

        try {
            establishConnection();

            // find all RegionNames with given StateName (sorted alphabetically)
            resultSet = statement.executeQuery("""
                SELECT RegionName
                FROM regions.regions
                WHERE StateName = """ + "'" + stateName + "' " +
                "ORDER BY RegionName ASC;"
            );

            // put all regionNames in ArrayList
            while (resultSet.next()) {
                String region = resultSet.getString("RegionName");
                regionsList.add(region);
            }

            // convert ArrayList to array and return
            regionsArray = new String[regionsList.size()];
            for (int i = 0; i < regionsList.size(); i++) {
                regionsArray[i] = regionsList.get(i);
            }
            return regionsArray;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return null;
    }

    /**
     * Creates an array of Strings containing state initials of all states in the database.
     *
     * @return an array of Strings containing all states
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String[] getStates() {
        // structure similar to getRegionsFromState()
        ArrayList<String> statesList = new ArrayList<>();
        String[] statesArray;

        try {
            establishConnection();

            // retrieve all unique StateNames from database (sorted alphabetically)
            resultSet = statement.executeQuery("""
                SELECT DISTINCT StateName
                FROM regions.regions
                ORDER BY StateName ASC;
            """);


            while (resultSet.next()) {
                String state = resultSet.getString("StateName");
                statesList.add(state);
            }

            statesArray = new String[statesList.size()];
            for (int i = 0; i < statesList.size(); i++) {
                statesArray[i] = statesList.get(i);
            }
            return statesArray;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return null;
    }

    /**
     * Retrieves the incorporated status of a given region.
     *
     * @param state String containing the state initials of the state that the specified region
     *              belongs to
     * @param region String containing the region to check the incorporated status of
     * @return boolean indicating true if the specified region is incorporated or false otherwise
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public boolean getIncorporatedStatus(String state, String region) {
        // query to select Incorporated column for given state and region
        StringBuilder query = new StringBuilder(
            "SELECT Incorporated " +
            "FROM region_populations " +
            "INNER JOIN regions " +
            "ON region_populations.RegionID = regions.RegionID " +
            "WHERE RegionName = '" + region + "' && StateName = '" + state + "';"
        );

        // attempt to retrieve incorporated status for region from table given by query
        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            return resultSet.getInt("Incorporated") == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    /**
     * Retrieves the first available beginDate for a specified state. (The first month where the
     * column is not all nulls).
     *
     * @param state String containing the state to find the first available beginDate for
     * @return String containing the first available beginDate for the given state
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromState(String state) {
        // startMonthString to return result
        String startMonthString = null;

        String selectAllMonths = createSelectAllMonthsString("Jan2000");
        int numMonths = getNumOfRemainingMonths("Jan2000");
        String[] months = getMonthsArray("Jan2000");

        // build query to select all avg zhvi data for current state
        StringBuilder query = new StringBuilder(
            "SELECT " + selectAllMonths + " " +
            "FROM region_zhvi_values " +
            "INNER JOIN regions " +
            "ON region_zhvi_values.RegionID = regions.RegionID " +
            "WHERE StateName = '" + state + "';"
        );

        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            // find first column with non-null avg and assign startMonthString to name of that
            // column
            for (int i = 0; i < numMonths; i++) {
                double yVal = resultSet.getDouble(months[i]);
                if (yVal != 0.0) {
                    startMonthString = months[i];
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return startMonthString
        return startMonthString;
    }

    /**
     * Retrieves the first available beginDate for a specified region. (The first month where the
     * column is not all nulls).
     *
     * @param state String containing the state initials of the region
     * @param region String containing the region name to find the first available beginDate for
     * @return String containing the first available beginDate for the given state
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromRegion(String state, String region) {
        // string startMonthString to return result
        String startMonthString = null;

        String selectAllMonths = createSelectAllMonthsString("Jan2000");
        int numMonths = getNumOfRemainingMonths("Jan2000");
        String[] months = getMonthsArray("Jan2000");

        // build query to select all avg zhvi data for current state and region
        StringBuilder query = new StringBuilder(
                "SELECT " + selectAllMonths + " " +
                        "FROM region_zhvi_values " +
                        "INNER JOIN regions " +
                        "ON region_zhvi_values.RegionID = regions.RegionID " +
                        "WHERE StateName = '" + state + "' && RegionName = '" + region + "';"
        );

        try {
            establishConnection();

            resultSet = statement.executeQuery(query.toString());
            resultSet.next();

            // find first column with non-null avg and assign startMonthString to name of that
            // column
            for (int i = 0; i < numMonths; i++) {
                double yVal = resultSet.getDouble(months[i]);
                if (yVal != 0.0) {
                    startMonthString = months[i];
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        // return startMonthString
        return startMonthString;
    }

    /**
     * Retrieves the first available beginDate for a graph comparing two separate states. Returns
     * the highest beginDate (first column without all nulls) out of BOTH of the state datasets.
     *
     * @param state1 String containing state initials of first state in graph comparison
     * @param state2 String containing state initials of the second state in graph comparison
     * @return String containing highest begin date out of both of the state datasets.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromStates(String state1, String state2) {
        String state1StartMonth = getBeginDateFromState(state1);
        String state2StartMonth = getBeginDateFromState(state2);

        if (getDoubleFromMonth(state1StartMonth) > getDoubleFromMonth(state2StartMonth)) {
            return state1StartMonth;
        } else {
            return state2StartMonth;
        }
    }

    /**
     * Retrieves the first available beginDate for a graph comparing two separated regions. Returns
     * the highest beginDate (first column without all nulls) out of BOTH of the region datasets.
     *
     * @param state1 String containing state initials of the state of the first region
     * @param region1 String containing region name of first region in graph comparison
     * @param state2 String containing state initials of the state of the second region
     * @param region2 String containing region name of second region in graph comparison
     * @return String containing highest begin date out of both of the region datasets
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromRegions(String state1, String region1, String state2, String region2) {
        String region1StartMonth = getBeginDateFromRegion(state1, region1);
        String region2StartMonth = getBeginDateFromRegion(state2, region2);

        if (getDoubleFromMonth(region1StartMonth) > getDoubleFromMonth(region2StartMonth)) {
            return region1StartMonth;
        } else {
            return region2StartMonth;
        }
    }

    /**
     * Attempts to close the connection to the localhost database by closing the ResultSet 3 times
     * (which closes Statement, which closes Connection).
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an array of Strings containing all months after and including the specified start
     * month.
     *
     * @param startMonth String containing the starting month to begin the months array
     * @return a String array containing all months after and including given startMonth
     */
    private String[] getMonthsArray(String startMonth) {
        // retrieve number of remaining months including startMonth and create array with this size
        int numMonths = getNumOfRemainingMonths(startMonth);
        String[] months = new String[numMonths];

        // fill months array with all available months after and including given startMonth
        String currentMonth = startMonth;
        for (int i = 0; i < numMonths; i++) {
            months[i] = currentMonth;
            currentMonth = getNextMonth(currentMonth);
        }

        // return fully built string array
        return months;
    }

    /**
     * Retrieves the number of remaining available months including the specified startMonth.
     *
     * @param startMonth String containing starting month to begin counting number of remaining
     *                   months
     * @return int indicating number of remaining months including given startMonth
     */
    private int getNumOfRemainingMonths(String startMonth) {
        // retrieve year and month integer from startMonth
        int startYear = Integer.parseInt(startMonth.substring(3));
        int startMonthInt = getNumFromMonth(startMonth.substring(0, 3));
        // calculate num of remaining years
        int numRemainingYears = 2024 - startYear;
        // calculate num of remaining months as num of remaining years times 12 plus num remaining
        // months in this current year
        return (numRemainingYears * 12) + (12 - startMonthInt - 1);
    }

    /**
     * Retrieves the double value for an XY series from a specified month string. Ex: converts
     * 'Jan2020' to 2020 + (1 / 12) or 2020.083333...
     *
     * @param month String containing the month to convert to a double value
     * @return double containing the specified month converted to a double
     */
    public static double getDoubleFromMonth(String month) {
        // retrieve year string and month int from month
        String yearString = month.substring(3) + ".";
        int monthNum = getNumFromMonth(month.substring(0, 3));
        // return year + (month / 12)
        return Double.parseDouble(yearString) + ((double) monthNum / 12.0);
    }

    /**
     * Retrieves the month name from a double value (does the opposite of getDoubleFromMonth()).
     * Ex: Returns 'Jan2020' if the double is 2020 + (1 / 12) or 2020.083333...
     *
     * @param month double containing the month value to convert to a string
     * @return String containing the month as YYYYMMM format
     */
    public static String getMonthFromDouble(double month) {
        // string to store month as a string
        String monthString;

        // retrieve year from month double as int
        int yearInt = (int) (month);
        // retrieve the decimal representing the month
        double monthDouble = month - (double) yearInt;
        // retrieve month int by multiplying it by 12 and rounding to nearest int
        int monthInt = (int) Math.round(monthDouble * 12);
        // add to month string the string equivalent of monthInt
        monthString = getMonthFromNum(monthInt);

        // add year to monthString and return
        monthString += Integer.toString(yearInt);
        return monthString;
    }

    /**
     * Retrieves the month number (1 for Jan, 2 for Feb, etc) from the specified month String.
     *
     * @param month String containing the month to convert to int
     * @return int containing the month number of the given month String
     */
    public static int getNumFromMonth(String month) {
        return switch (month) {
            case "Jan" -> 1;
            case "Feb" -> 2;
            case "Mar" -> 3;
            case "Apr" -> 4;
            case "May" -> 5;
            case "Jun" -> 6;
            case "Jul" -> 7;
            case "Aug" -> 8;
            case "Sep" -> 9;
            case "Oct" -> 10;
            case "Nov" -> 11;
            case "Dec" -> 12;
            default -> throw new IllegalArgumentException(
                    "An error occurred when trying to convert " + month + " to a month value"
            );
        };
    }

    /**
     * Retrieves the month String (Jan for 1, Feb for 2, etc) from the specified month number.
     *
     * @param num int containing the month number to convert to String
     * @return String containing the month of the given month number
     */
    public static String getMonthFromNum(int num) {
        return switch (num) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> throw new IllegalArgumentException(
                    "An error occurred when trying to convert the number " + num + " to a month."
            );
        };
    }

    /**
     * Retrieves the next month in the sequence, given the specified currentMonth as a String.
     *
     * @param currentMonth String containing the current month
     * @return String containing the next month in the sequence, after currentMonth
     */
    private String getNextMonth(String currentMonth) {
        // retrieve year and month int from given currentMonth
        int year = Integer.parseInt(currentMonth.substring(3));
        int monthInt = getNumFromMonth(currentMonth.substring(0, 3));

        // increment year if month is dec and change month to Jan, else just increment month
        if (monthInt == 12) {
            year++;
            monthInt = 1;
        } else {
            monthInt++;
        }

        // return new month String
        return (getMonthFromNum(monthInt) + Integer.toString(year));
    }

    /**
     * Creates a string for a query that comes directly after the SELECT clause containing the
     * AVG() of all months following and including the specified start month.
     *
     * @param startMonth String containing the starting month to begin the selection
     * @return String containing the AVG() of all months following and including the given
     * startMonth for use in a query
     */
    private String createSelectAllMonthsString(String startMonth) {
        // retrieve num of remaining months including startMonth and create string to return
        int numMonths = getNumOfRemainingMonths(startMonth);
        StringBuilder selectAllMonthsString = new StringBuilder();

        // build string by adding AVG(month) for each month after and including startMonth
        String currentMonth = startMonth;
        for (int i = 0; i < numMonths; i++) {
            selectAllMonthsString.append("AVG(").append(currentMonth).append(") AS '").append(currentMonth).append("'");
            if (i != numMonths - 1) {
                selectAllMonthsString.append(", ");
            }
            currentMonth = getNextMonth(currentMonth);
        }

        // return full string
        return selectAllMonthsString.toString();
    }

}
