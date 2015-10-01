package it.geosolutions.appschema.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.Types;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.helpers.NamespaceSupport;

public class DenormalizedSingleValueNestedInMultiValueTest {

    public static final String GSMLNS = "urn:cgi:xmlns:CGI:GeoSciML:2.0";

    public static final String GMLNS = "http://www.opengis.net/gml";

    public static final String XLINKNS = "http://www.w3.org/1999/xlink";

    static final Name MAPPED_FEATURE_TYPE = Types.typeName(GSMLNS, "MappedFeatureType");

    static final Name MAPPED_FEATURE = Types.typeName(GSMLNS, "MappedFeature");

    static final Name GEOLOGIC_UNIT = Types.typeName(GSMLNS, "GeologicUnit");

    static FilterFactory2 ff;

    private static final String schemaBase = "/test-data/";

    private static FeatureSource<FeatureType, Feature> mfSource;

    /**
     * Generated mapped features
     */
    private static FeatureCollection<FeatureType, Feature> mfFeatures;

    /**
     * Generated geologic units
     */
    private static FeatureCollection<FeatureType, Feature> guFeatures;

    private NamespaceSupport namespaces = new NamespaceSupport();

    public DenormalizedSingleValueNestedInMultiValueTest() {
        namespaces.declarePrefix("gml", GMLNS);
        namespaces.declarePrefix("gsml", GSMLNS);
        namespaces.declarePrefix("xlink", XLINKNS);
        ff = new FilterFactoryImplNamespaceAware(namespaces);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        loadDataAccesses();
    }

    /**
     * Load all the data accesses.
     *
     * @return
     * @throws Exception
     */
    private static void loadDataAccesses() throws Exception {
        /**
         * Load mapped feature data access
         */
        Map dsParams = new HashMap();
        URL url = DenormalizedSingleValueNestedInMultiValueTest.class.getResource(schemaBase
                + "GeoSciMLPropertyfile.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> gsmlDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(gsmlDataAccess);

        FeatureType mappedFeatureType = gsmlDataAccess.getSchema(MAPPED_FEATURE);
        assertNotNull(mappedFeatureType);

        mfSource = (FeatureSource) gsmlDataAccess.getFeatureSource(MAPPED_FEATURE);
        mfFeatures = (FeatureCollection) mfSource.getFeatures();
        assertEquals(2, size(mfFeatures));

        /**
         * Load geologic unit data access
         */
        FeatureType guType = gsmlDataAccess.getSchema(GEOLOGIC_UNIT);
        assertNotNull(guType);

        FeatureSource<FeatureType, Feature> guSource = (FeatureSource<FeatureType, Feature>) gsmlDataAccess
                .getFeatureSource(GEOLOGIC_UNIT);
        guFeatures = (FeatureCollection) guSource.getFeatures();
        assertEquals(1, size(guFeatures));
    }

    private static int size(FeatureCollection<FeatureType, Feature> features) {
        int size = 0;
        FeatureIterator<Feature> iterator = features.features();
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        iterator.close();
        return size;
    }

    @Test
    public void testObservationMethod() {
        FeatureIterator<? extends Feature> featIt = mfFeatures.features();
        while (featIt.hasNext()) {
            Feature f = featIt.next();
            if ("mf1".equals(f.getIdentifier().getID())) {
                PropertyName pf = ff.property("gsml:observationMethod/gsml:CGI_TermValue/gsml:value", namespaces);
                Object obsMethValue = pf.evaluate(f);
                assertNotNull(obsMethValue);
                assertTrue(obsMethValue instanceof Collection);
                assertEquals(3, ((Collection)obsMethValue).size());
            }
        }
    }

    @Test
    public void testMetadataProperty() {
        FeatureIterator<? extends Feature> featIt = mfFeatures.features();
        while (featIt.hasNext()) {
            Feature f = featIt.next();
            if ("mf1".equals(f.getIdentifier().getID())) {
                PropertyName pf = ff.property("gml:metaDataProperty/@xlink:href", namespaces);
                Object obsMethValue = pf.evaluate(f);
                assertNotNull(obsMethValue);
                assertTrue(obsMethValue instanceof Collection);
                assertEquals(3, ((Collection)obsMethValue).size());
            }
        }
    }

}
