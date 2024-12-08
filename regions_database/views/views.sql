-- statedensityview
CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `statedensityview` AS
    SELECT 
        `regions`.`StateName` AS `StateName`,
        AVG(`region_populations`.`PopulationDensity`) AS `AvgDensity`
    FROM
        (`regions`
        JOIN `region_populations` ON ((`regions`.`RegionID` = `region_populations`.`RegionID`)))
    GROUP BY `regions`.`StateName`