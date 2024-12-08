This README file contains all submission documentation for the final project including references, installation instructions, usage instructions, and all additional comments/instructions.

# RegionalLivingInsights

An interactive application for comparing regional home values and population densities, visualizing trends, and forecasting future home values to help users make informed decisions about where to live.

Formatted version of this file available from the [github repository link](https://github.com/tom2433/RegionalLivingInsights).

# File Locations Summarized

- Documentation: '/RegionalLivingInsights/README.md'
- ER diagram: '/RegionalLivingInsights/RegionalLivingInsights.dia'
- Initial datasets (before normalization): '/RegionalLivingInsights/initial_data/'
- Video Demonstration: '/RegionalLivingInsights/Project_Demonstration.mp4'
- MySQL tables: '/RegionalLivingInsights/regions_database/tables/'
- MySQL views: '/RegionalLivingInsights/regions_database/views/'
- Source code files: 'RegionalLivingInsights/ProjectWorkspace/app/src/main/java/projectworkspace/'

# References

JFreeChart - open source chart libray for creating bar charts and line charts

Version used: 1.5.5

License: GNU Lesser General Public License

Official Website: [https://www.jfree.org/jfreechart/](https://www.jfree.org/jfreechart/)

# Installation Instructions

### Back End

- **Step 1**: Create Schema called 'regions' on localhost.
- **Step 2**: Import tables 'regions', 'region_populations', and 'region_zhvi_values' using the create_tables sql file located at '/RegionalLivingInsights/regions_database/tables/' along with each table's corresponding csv file.
- **Step 3**: Import views using the views sql file located at '/RegionalLivingInsights/regions_database/views/views.sql'
- **Step 4**: Change the string values 'username' and 'password' on lines 30 and 31 of '/RegionalLivingInsights/ProjectWorkspace/app/src/main/java/projectworkspace/DataReader.java' to your username and password used to connect to your local databases.

### Front End

##### Command Line
- In the terminal (was tested in WSL), navigate to the '/RegionalLivingInsights/ProjectWorkspace/' directory and ensure that you have JDK 21 Installed.
- Use the gradle wrapper to build and run the project:
  - For Unix/macOS --> './gradlew build' to build and './gradlew run' to run
  - For Windows --> '.\gradlew.bat build' to build and '.\gradlew.bat run' to run
##### IDE
- Open the '/RegionalLivingInsights/ProjectWorkspace/' directory with an IDE like IntelliJ.
- Use the IDE's built in run options or use the IDE's built in terminal following the command line instructions above to use the gradle wrapper.

# Usage Instructions

- The 3 main features of the application are:
  - Compare population densities and median home value histories of two separate regions
    - User cannot select two of the same regions to compare in the RegionScreen.
  - Compare population densities and median home value histories of two separate states
    - User cannot select two of the same states to compare in the StateScreen.
  - Compare population densities and median home vlaue histories of two or more custom datasets, which can each contain an unlimited number of regions and/or states.
    - User cannot have duplicate regions or states across or within custom datasets in the CustomDataScreen.
- There are three buttons on the Main Menu screen corresponding to each of these three features.
- Each of the three features has a corresponding dataVisScreen, which displays a population density bar chart.
  - Contains 3 values (one for avg region/state density, one for one user-selected region/state, and one for the other), or 1 for each user-created custom dataset plus 1 for the average US state.
- Each dataVisScreen has a line graph containing with X as the month (converted to a year) and Y as the median home value for that specific month.
  - Contains 2 XYSeries or 'lines' (one for one region/state, one for the other), or one for each user-created custom dataset.
- Each of the three features also includes a "Calculate Predicted Data" button which allows the user to create a line graph including past and future data (calculated using a trendline) for a user-specified number of years.
  - This graph is a line graph which will contain two lines (one for one region/state, one for the other), or one line for each custom dataset created by the user.
- Notes about data processing within the application:
  - The region_zhvi_values table in the database has some null values for the earlier years for some regions. To combat this, the application finds the first available column with a non-null value for each region or state. When the user chooses the start date to begin pulling data, the first date they can choose will be the first available date with non-null values.
  - Some regions may have a null value in the middle of the data being pulled. If the program encounters a null value in the middle of pulling data, it replaces the null value with the last non-null value pulled from the table.

# Notes about Data Normalization

- The initial data came from 2 separate datasets (both located in '/RegionalLivingInsights/initial_data/'):
  - 1: 'Metro_zhvi_uc_sfrcondo_tier_0.33_0.67_sm_sa_month.csv': taken from [zillow](https://www.zillow.com/research/data).
  - 2: 'uscities.csv': taken from [kaggle](https://www.kaggle.com/datasets/sergejnuss/united-states-cities-database).
- Used excel and java to combine these two datasets on RegionID found in the zillow dataset.
  - Found all common regions (city & state) between the uscities csv and the csv containing zhvi values.
  - Removed regions from zhvi csv that did not have a corresponding region in the uscities csv.
  - Removed all regions from uscities csv that did not show up in the zhvi table.
  - Used RegionID from the zhvi csv as a primary key for regions table, replaced region and state columns from zhvi csv and uscities csv with their corresponding RegionID value as a foreign key to the regions table.
___
