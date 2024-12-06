package projectworkspace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataReader {
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    @SuppressWarnings("CallToPrintStackTrace")
    public int getDensityFromSet(ArrayList<String> set) {
        ArrayList<Integer> densities = new ArrayList<>();
        int avgDensity = 0;

        try {
            establishConnection();

            for (String area : set) {
                // if current area is a state
                if (area.length() == 2) {
                    densities.add(getDensityFromState(area));
                // else (current area not a state)
                } else {
                    densities.add(getDensityFromRegion(area));
                }
            }

            int sum = 0;
            for (int density : densities) {
                sum += density;
            }
            avgDensity = sum / densities.size();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return avgDensity;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private int getDensityFromRegion(String regionAndState) throws SQLException {
        String region = regionAndState.substring(0, regionAndState.indexOf(',')).trim();
        String state = regionAndState.substring(regionAndState.indexOf(',') + 2).trim();

        resultSet = statement.executeQuery("""
            SELECT region_populations.PopulationDensity, regions.RegionName, regions.StateName
            FROM region_populations
            INNER JOIN regions
            ON region_populations.RegionID = regions.RegionID
            WHERE regions.StateName = '""" + state + "' && RegionName = '" + region + "';"
        );

        resultSet.next();
        return resultSet.getInt("PopulationDensity");
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromSet(ArrayList<String> dataset, String startMonth) {
        int numMonths = getNumOfRemainingMonths(startMonth);
        Map<Double, Double> XYSeriesData = new HashMap<>();
        double[][] yValueSets = new double[dataset.size()][numMonths];
        String selectAllMonthsString;
        String currentMonth;
        double lastNonNullValue = 0.0;

        try {
            establishConnection();

            int index = 0;
            for (String area : dataset) {
                double[] values = new double[numMonths];
                String query;

                // if current area is a state
                if (area.length() == 2) {
                    currentMonth = startMonth;
                    selectAllMonthsString = "";
                    for (int i = 0; i < numMonths; i++) {
                        selectAllMonthsString += "AVG(region_zhvi_values." + currentMonth + ") AS 'Month " + i + "'";
                        if (i != numMonths - 1) {
                            selectAllMonthsString += ", ";
                        }
                        currentMonth = getNextMonth(currentMonth);
                    }

                    query =
                        "SELECT " + selectAllMonthsString +
                        "FROM region_zhvi_values " +
                        "INNER JOIN regions " +
                        "ON region_zhvi_values.RegionID = regions.RegionID " +
                        "WHERE regions.StateName = '" + area + "';";
                // else (curent area not a state)
                } else {
                    String region = area.substring(0, area.indexOf(',')).trim();
                    String state = area.substring(area.indexOf(',') + 2).trim();

                    currentMonth = startMonth;
                    selectAllMonthsString = "";
                    for (int i = 0; i < numMonths; i++) {
                        selectAllMonthsString += "region_zhvi_values." + currentMonth + " AS 'Month " + i + "'";
                        if (i != numMonths - 1) {
                            selectAllMonthsString += ", ";
                        }
                        currentMonth = getNextMonth(currentMonth);
                    }

                    query =
                        "SELECT " + selectAllMonthsString +
                        "FROM region_zhvi_values " +
                        "INNER JOIN regions " +
                        "ON region_zhvi_values.RegionID = regions.RegionID " +
                        "WHERE regions.StateName = '" + state + "' && regions.RegionName = '" + region + "'";
                }

                resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    for (int i = 0; i < numMonths; i++) {
                        double result = resultSet.getDouble("Month " + i);
                        if (result == 0.0) {
                            result = lastNonNullValue;
                        } else {
                            lastNonNullValue = result;
                        }
                        values[i] = result;
                    }
                }

                yValueSets[index] = values;
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        double[] averagedYValues = new double[numMonths];

        for (int i = 0; i < yValueSets[0].length; i++) {
            double sum = 0.0;
            for (int j = 0; j < yValueSets.length; j++) {
                sum += yValueSets[j][i];
            }
            averagedYValues[i] = sum / (double) yValueSets.length;
        }

        currentMonth = startMonth;
        for (int i = 0; i < numMonths; i++) {
            XYSeriesData.put(getDoubleFromMonth(currentMonth), averagedYValues[i]);
            currentMonth = getNextMonth(currentMonth);
        }

        return XYSeriesData;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromState(String state, String startMonth) {
        ArrayList<Integer> regionIDs = new ArrayList<>();
        String currentMonth = startMonth;
        double lastNonNullValue = 0.0;
        Map<Double, Double> resultData = new HashMap<>();

        try {
            establishConnection();

            // get all region ids from state
            resultSet = statement.executeQuery("""
                SELECT RegionID
                FROM regions.regions
                WHERE StateName = '""" + state + "';"
            );

            while (resultSet.next()) {
                regionIDs.add(resultSet.getInt("RegionID"));
            }

            // get all zhvi data from all region ids
            String zhviQuery = "SELECT AVG(";

            for (int i = 0; i < getNumOfRemainingMonths(startMonth); i++) {
                zhviQuery += currentMonth + "), AVG(";
                currentMonth = getNextMonth(currentMonth);
            }

            zhviQuery = zhviQuery.substring(0, zhviQuery.length() - ", AVG(".length());
            zhviQuery += " FROM regions.region_zhvi_values WHERE RegionID = ";

            zhviQuery += regionIDs.get(0);
            for (int i = 1; i < regionIDs.size(); i++) {
                zhviQuery += " || RegionID = " + regionIDs.get(i);
            }
            zhviQuery += ";";

            resultSet = statement.executeQuery(zhviQuery);

            currentMonth = startMonth;
            resultSet.next();
            for (int i = 0; i < getNumOfRemainingMonths(startMonth); i++) {
                double zhviValue = resultSet.getDouble("AVG(" + currentMonth + ")");
                if (zhviValue == 0.0) {
                    zhviValue = lastNonNullValue;
                } else {
                    lastNonNullValue = zhviValue;
                }

                resultData.put(getDoubleFromMonth(currentMonth), zhviValue);
                currentMonth = getNextMonth(currentMonth);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return resultData;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public Map<Double, Double> getZhviDataFromRegion(String region, String state, String startMonth) {
        int regionID;
        Map<Double, Double> resultData = new HashMap<>();
        String currentMonth = startMonth;
        double lastNonNullValue = 0.0;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionID FROM regions.regions
                WHERE StateName = '""" + state + "' && RegionName = '" + region + "';"
            );

            resultSet.next();
            regionID = resultSet.getInt("RegionID");

            resultSet = statement.executeQuery("""
                SELECT * FROM regions.region_zhvi_values
                WHERE RegionID = """ + regionID + ";"
            );

            resultSet.next();
            for (int i = 0; i < getNumOfRemainingMonths(startMonth); i++) {
                double zhviValue = resultSet.getDouble(currentMonth);
                if (zhviValue == 0.0) {
                    zhviValue = lastNonNullValue;
                } else {
                    lastNonNullValue = zhviValue;
                }

                resultData.put(getDoubleFromMonth(currentMonth), zhviValue);
                currentMonth = getNextMonth(currentMonth);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return resultData;
    }

    private int getNumOfRemainingMonths(String startMonth) {
        int startYear = Integer.parseInt(startMonth.substring(3));
        int startMonthInt = getNumFromMonth(startMonth.substring(0, 3));
        int numRemainingYears = 2024 - startYear;
        int numRemainingMonths = (numRemainingYears * 12) + (12 - startMonthInt - 1);
        return numRemainingMonths;
    }

    public static double getDoubleFromMonth(String month) {
        String yearString = month.substring(3) + ".";

        int monthNum = getNumFromMonth(month.substring(0, 3));

        return Double.parseDouble(yearString) + ((double) monthNum / 12.0);
    }

    public static String getMonthFromDouble(double month) {
        String startMonthString;

        int yearInt = (int) (month);
        double monthDouble = month - (double) yearInt;
        int monthInt = (int) Math.round(monthDouble * 12);
        switch (monthInt) {
            case 1 -> startMonthString = "Jan";
            case 2 -> startMonthString = "Feb";
            case 3 -> startMonthString = "Mar";
            case 4 -> startMonthString = "Apr";
            case 5 -> startMonthString = "May";
            case 6 -> startMonthString = "Jun";
            case 7 -> startMonthString = "Jul";
            case 8 -> startMonthString = "Aug";
            case 9 -> startMonthString = "Sep";
            case 10 -> startMonthString = "Oct";
            case 11 -> startMonthString = "Nov";
            case 12 -> startMonthString = "Dec";
            default -> throw new IllegalArgumentException("An error occurred in processing month data");
        }

        startMonthString += Integer.toString(yearInt);

        return startMonthString;
    }

    private String getNextMonth(String currentMonth) {
        int year = Integer.parseInt(currentMonth.substring(3));
        int monthInt = getNumFromMonth(currentMonth.substring(0, 3));

        if (monthInt == 12) {
            year++;
            monthInt = 1;
        } else {
            monthInt++;
        }

        return (getMonthFromNum(monthInt) + Integer.toString(year));
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public double getRegionDensity(String region, String state) {
        int regionID;
        int density = 0;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionID FROM regions.regions
                WHERE StateName = '""" + state + "' && RegionName = '" + region + "';"
            );

            resultSet.next();
            regionID = resultSet.getInt("RegionID");

            resultSet = statement.executeQuery("""
                SELECT PopulationDensity
                FROM regions.region_populations
                WHERE RegionID = """ + regionID
            );

            resultSet.next();
            density = resultSet.getInt("PopulationDensity");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return (double) density;
    }

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

    private int getDensityFromState(String state) throws SQLException {
        int avgRegionDensity;

        resultSet = statement.executeQuery("""
            SELECT AvgDensity
            FROM statedensityview
            WHERE StateName = '""" + state + "';");

        resultSet.next();
        double avgDensity = resultSet.getDouble("AvgDensity");
        avgRegionDensity = (int) avgDensity;
        return avgRegionDensity;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public int getStateDensity(String state) {
        int avgRegionDensity = 0;

        try {
            establishConnection();
            avgRegionDensity = getDensityFromState(state);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return avgRegionDensity;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String[] getRegionsFromState(String stateName) {
        ArrayList<String> regionsList = new ArrayList<>();
        String[] regionsArray;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionName
                FROM regions.regions
                WHERE StateName = """ + "'" + stateName + "';"
            );

            while (resultSet.next()) {
                String region = resultSet.getString("RegionName");
                regionsList.add(region);
            }

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

    @SuppressWarnings("CallToPrintStackTrace")
    public String[] getStates() {
        ArrayList<String> statesList = new ArrayList<>();
        String[] statesArray;

        try {
            establishConnection();

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

    @SuppressWarnings("CallToPrintStackTrace")
    public boolean getIncorporatedStatus(String state, String region) {
        int regionID;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionID
                FROM regions.regions
                WHERE StateName = '""" + state + "' && RegionName = '" + region + "';"
            );

            resultSet.next();
            regionID = resultSet.getInt("RegionID");

            resultSet = statement.executeQuery("""
                SELECT Incorporated
                FROM regions.region_populations
                WHERE RegionID = """ + Integer.toString(regionID) + ";");

            resultSet.next();
            return resultSet.getInt("Incorporated") == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromState(String state) {
        String startMonthString = null;

        try {
            establishConnection();

            ArrayList<Integer> state1regionIDs = new ArrayList<>();
            ArrayList<Double> state1beginDates = new ArrayList<>();
            Double startMonth;

            // find begin month from state1
            // retrieve all regions from state1
            resultSet = statement.executeQuery("""
                SELECT RegionID FROM regions.regions
                WHERE StateName = '""" + state + "';"
            );

            while (resultSet.next()) {
                state1regionIDs.add(resultSet.getInt("RegionID"));
            }

            for (Integer id : state1regionIDs) {
                state1beginDates.add(getDoubleFromMonth(getStartMonth(id)));
            }

            startMonth = 0.0;
            for (Double date : state1beginDates) {
                if (date > startMonth) {
                    startMonth = date;
                }
            }

            // convert startMonth to string
            int yearInt = (int) ((double) startMonth);
            double monthDouble = startMonth - (double) yearInt;
            int monthInt = (int) Math.round(monthDouble * 12);
            switch (monthInt) {
                case 1 -> startMonthString = "Jan";
                case 2 -> startMonthString = "Feb";
                case 3 -> startMonthString = "Mar";
                case 4 -> startMonthString = "Apr";
                case 5 -> startMonthString = "May";
                case 6 -> startMonthString = "Jun";
                case 7 -> startMonthString = "Jul";
                case 8 -> startMonthString = "Aug";
                case 9 -> startMonthString = "Sep";
                case 10 -> startMonthString = "Oct";
                case 11 -> startMonthString = "Nov";
                case 12 -> startMonthString = "Dec";
                default -> throw new IllegalArgumentException("An error occurred in processing month data");
            }

            startMonthString += Integer.toString(yearInt);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return startMonthString;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromStates(String state1, String state2) {
        String startMonthString = null;

        try {
            establishConnection();

            ArrayList<Integer> state1regionIDs = new ArrayList<>();
            ArrayList<Double> state1beginDates = new ArrayList<>();
            ArrayList<Integer> state2regionIDs = new ArrayList<>();
            ArrayList<Double> state2beginDates = new ArrayList<>();
            Double state1startMonth;
            Double state2startMonth;
            Double startMonth;

            // find begin month from state1
            // retrieve all regions from state1
            resultSet = statement.executeQuery("""
                SELECT RegionID FROM regions.regions
                WHERE StateName = '""" + state1 + "';"
            );

            while (resultSet.next()) {
                state1regionIDs.add(resultSet.getInt("RegionID"));
            }

            for (Integer id : state1regionIDs) {
                state1beginDates.add(getDoubleFromMonth(getStartMonth(id)));
            }

            state1startMonth = 0.0;
            for (Double date : state1beginDates) {
                if (date > state1startMonth) {
                    state1startMonth = date;
                }
            }

            // find begin month from state2
            // retrieve all regions from state2
            resultSet = statement.executeQuery("""
                SELECT RegionID FROM regions.regions
                WHERE StateName = '""" + state2 + "';"
            );

            while (resultSet.next()) {
                state2regionIDs.add(resultSet.getInt("RegionID"));
            }

            for (Integer id : state2regionIDs) {
                state2beginDates.add(getDoubleFromMonth(getStartMonth(id)));
            }

            state2startMonth = 0.0;
            for (Double date : state2beginDates) {
                if (date > state2startMonth) {
                    state2startMonth = date;
                }
            }

            startMonth = state1startMonth > state2startMonth ? state1startMonth : state2startMonth;

            // convert startMonth to string
            int yearInt = (int) ((double) startMonth);
            double monthDouble = startMonth - (double) yearInt;
            int monthInt = (int) Math.round(monthDouble * 12);
            switch (monthInt) {
                case 1 -> startMonthString = "Jan";
                case 2 -> startMonthString = "Feb";
                case 3 -> startMonthString = "Mar";
                case 4 -> startMonthString = "Apr";
                case 5 -> startMonthString = "May";
                case 6 -> startMonthString = "Jun";
                case 7 -> startMonthString = "Jul";
                case 8 -> startMonthString = "Aug";
                case 9 -> startMonthString = "Sep";
                case 10 -> startMonthString = "Oct";
                case 11 -> startMonthString = "Nov";
                case 12 -> startMonthString = "Dec";
                default -> throw new IllegalArgumentException("An error occurred in processing month data");
            }

            startMonthString += Integer.toString(yearInt);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return startMonthString;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromRegion(String state, String region) {
        int regionID;
        String regionStartMonth = null;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionID
                FROM regions.regions
                WHERE StateName = '""" + state + "' && RegionName = '" + region + "';"
            );

            resultSet.next();
            regionID = resultSet.getInt("RegionID");

            regionStartMonth = getStartMonth(regionID);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return regionStartMonth;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String getBeginDateFromRegions(String state1, String region1, String state2, String region2) {
        int region1ID;
        int region2ID;
        String region1StartMonth;
        String region2StartMonth;

        try {
            establishConnection();

            resultSet = statement.executeQuery("""
                SELECT RegionID
                FROM regions.regions
                WHERE StateName = '""" + state1 + "' && RegionName = '" + region1 + "';"
            );

            resultSet.next();
            region1ID = resultSet.getInt("RegionID");

            resultSet = statement.executeQuery("""
                SELECT RegionID
                FROM regions.regions
                WHERE StateName = '""" + state2 + "' && RegionName = '" + region2 + "';"
            );

            resultSet.next();
            region2ID = resultSet.getInt("RegionID");

            region1StartMonth = getStartMonth(region1ID);
            region2StartMonth = getStartMonth(region2ID);

            int region1StartYear = Integer.parseInt(region1StartMonth.substring(3));
            int region2StartYear = Integer.parseInt(region2StartMonth.substring(3));

            if (region1StartYear < region2StartYear) {
                return region2StartMonth;
            } else if (region2StartYear < region1StartYear) {
                return region1StartMonth;
            } else {
                int r1MonthInt = getNumFromMonth(region1StartMonth.substring(0, 3));
                int r2MonthInt = getNumFromMonth(region2StartMonth.substring(0, 3));

                if (r1MonthInt < r2MonthInt) {
                    return region2StartMonth;
                } else {
                    return region1StartMonth;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return null;
    }

    private String getStartMonth(int regionID) throws SQLException {
        double zhvi = 0.0;
        String currentMonth = null;
        String currentYear = null;

        for (int i = 2000; i <= 2024; i++) {
            currentYear = Integer.toString(i);
            for (int j = 1; j <= 12; j++) {
                currentMonth = getMonthFromNum(j);

                if (i == 2024 && j > 10) {
                    break;
                }

                resultSet = statement.executeQuery(
                    "SELECT " + currentMonth + currentYear + " " +
                    "FROM regions.region_zhvi_values" + " " +
                    "WHERE RegionID = " + Integer.toString(regionID) + ";"
                );

                resultSet.next();
                zhvi = resultSet.getDouble(currentMonth + currentYear);
                if (zhvi != 0.0) {
                    break;
                }
            }

            if (zhvi != 0.0) {
                break;
            }
        }

        return currentMonth + currentYear;
    }

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

    private void establishConnection() throws SQLException {
        String username = "root";
        String password = "B@nd1t03";

        connection = DriverManager.getConnection("jdbc:mysql://localhost/regions?" +
                "user=" + username + "&password=" + password);

        statement = connection.createStatement();
    }

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
}
