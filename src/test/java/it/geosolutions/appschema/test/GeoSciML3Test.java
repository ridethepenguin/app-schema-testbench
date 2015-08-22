package it.geosolutions.appschema.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.geotools.data.complex.config.AppSchemaFeatureTypeRegistry;
import org.geotools.data.complex.config.EmfComplexFeatureReader;
import org.geotools.data.complex.config.Types;
import org.geotools.test.AppSchemaTestSupport;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.resolver.SchemaCache;
import org.geotools.xml.resolver.SchemaResolver;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public class GeoSciML3Test extends AppSchemaTestSupport {

    private static final String GEOSCIML3_SCHEMA_LOCATION = "/test-data/geosciml-32.xsd";
    private static final String GEOSCIML3_ORIG_SCHEMA_LOCATION = "/test-data/geosciml-32_original.xsd";

    private static EmfComplexFeatureReader reader;

    @BeforeClass
    public static void oneTimeSetUp() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        SchemaResolver resolver = new SchemaResolver(new SchemaCache(new File(tmpDir, "app-schema-cache"), true));
        reader = EmfComplexFeatureReader.newInstance();
        reader.setResolver(resolver);

        //need to register custom factory to load schema resources
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                                          .put("xsd", new XSDResourceFactoryImpl());
    }

    @Test
    public void testParseSchemaOriginal() throws Exception {
        testParseSchema(GeoSciML3Test.class.getResource(GEOSCIML3_ORIG_SCHEMA_LOCATION));
    }

    @Test
    public void testParseSchemaEdited() throws Exception {
        testParseSchema(GeoSciML3Test.class.getResource(GEOSCIML3_SCHEMA_LOCATION));
    }

    public void testParseSchema(URL schemaURL) throws Exception {
        SchemaIndex schemaIndex;
        try {
            schemaIndex = reader.parse(schemaURL);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        XSDElementDeclaration guElDecl = schemaIndex.getElementDeclaration(new QName("http://xmlns.geosciml.org/GeologicUnit/3.2", "GeologicUnit"));
        assertNotNull(guElDecl);

        AppSchemaFeatureTypeRegistry typeRegistry = new AppSchemaFeatureTypeRegistry();
        try {
            typeRegistry.addSchemas(schemaIndex);
    
            Name typeName = Types.typeName("http://xmlns.geosciml.org/GeologicUnit/3.2", "GeologicUnitType");
            ComplexType mf = (ComplexType) typeRegistry.getAttributeType(typeName);
            assertNotNull(mf);
            assertTrue(mf instanceof FeatureType);
        }
        finally {
            typeRegistry.disposeSchemaIndexes();
        }
    }

    @Test
    public void testResourceLoadOriginal() throws IOException {
        testResourceLoad(GeoSciML3Test.class.getResource(GEOSCIML3_ORIG_SCHEMA_LOCATION).toString());
    }

    @Test
    public void testResourceLoadEdited() throws IOException {
        testResourceLoad(GeoSciML3Test.class.getResource(GEOSCIML3_SCHEMA_LOCATION).toString());
    }

    public void testResourceLoad(String schemaURI) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();
        XSDResourceImpl xsdResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(schemaURI));
        assertNotNull(xsdResource);

        xsdResource.load(resourceSet.getLoadOptions());

        XSDSchema schema = xsdResource.getSchema();
        assertNotNull(schema);

        // uncommenting the following code block will make the test pass
        /*
        if (schema.getElementDeclarations().isEmpty() && schema.getTypeDefinitions().isEmpty()) {
            for (XSDSchemaContent content: schema.getContents()) {
                if (content instanceof XSDImportImpl) {
                    XSDImportImpl importDirective = (XSDImportImpl)content;
                    ((XSDSchemaImpl)schema).resolveSchema(importDirective.getNamespace());
                }
            }
        }
        */

        boolean guFound = false;
        EList<XSDElementDeclaration> elDeclList = schema.getElementDeclarations();
        for (XSDElementDeclaration elDecl: elDeclList) {
            if ("gsmlgu:GeologicUnit".equals(elDecl.getQName())) {
                guFound = true;
            }
        }

        assertTrue(guFound);
    }
}
