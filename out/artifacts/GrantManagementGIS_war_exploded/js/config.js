config = {};

//layers I have publishing rights to an AGIO's ArcServer
config.grantPoints = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/28";
config.parkfootprints = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38";
config.stateparks = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_MapService_2017/MapServer";
config.conversions = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/24";
config.projectBoundary = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/25";
config.regions = "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/52";
config.fedWhere = "type = 'federal'";
config.stateWhere = "type = 'state'";
config.parkfootprintsWhere = "type = 'funded park'";

//basemap imagery
config.mapboxlink = "https://api.mapbox.com/styles/v1/robertkaciradpt/cjjrecba50sae2snpvqcw8ylq/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoicm9iZXJ0a2FjaXJhZHB0IiwiYSI6ImNqZ3BoODQ2NTAwM20ycXJ1OWpkZnh1emkifQ.MBfZdxZljkG8_JeivKerxw";
config.worldImagery = 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}';

//layers maintained by AGIO or ESRI. Do not have control of index links or other link info.
config.houseDistrict = "https://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/15";
config.senateDistrict = "https://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/34";
config.municipal = "https://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/41";
config.county = "https://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/8";