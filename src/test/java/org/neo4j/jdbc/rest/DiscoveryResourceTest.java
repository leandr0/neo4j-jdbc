package org.neo4j.jdbc.rest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.server.CommunityNeoServer;
import org.restlet.Client;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.jdbc.TestServer;
import org.neo4j.kernel.GraphDatabaseAPI;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Hunger @since 25.10.13
 */
public class DiscoveryResourceTest
{
    public static final String URI = "http://localhost:" + TestServer.PORT;
    public static final String USER_AGENT = "Neo4j JDBC Driver/<unversioned>";
    private static CommunityNeoServer webServer;
    private static GraphDatabaseAPI db;
    private static Resources.DiscoveryClientResource resource;

    @BeforeClass
    public static void startServer() throws Exception
    {
        webServer = TestServer.startWebServer( TestServer.PORT, false );
        db = webServer.getDatabase().getGraph();
        createData( db );
        resource = new Resources(URI, new Client("HTTP"), USER_AGENT ).getDiscoveryResource(  );
    }

    private static void createData( GraphDatabaseAPI db )
    {
        try ( Transaction tx = db.beginTx() )
        {
            Node node = DiscoveryResourceTest.db.createNode( DynamicLabel.label( "FooBar" ) );
            node.setProperty( "foo", "bar" );
            node.createRelationshipTo( node, DynamicRelationshipType.withName( "FOO_BAR" ) );
            tx.success();
        }
    }

    @AfterClass
    public static void stopServer() throws Exception
    {
        if ( webServer != null )
        {
            webServer.stop();
            webServer = null;
        }
        if ( db != null )
        {
            db.shutdown();
        }
    }

    @Test
    public void testGetCypherPath() throws Exception
    {
        assertEquals( URI + "/db/data/cypher", resource.getCypherPath() );
    }

    @Test
    public void testGetTransactionPath() throws Exception
    {
        assertEquals( URI + "/db/data/transaction", resource.getTransactionPath() );
    }

    @Test
    public void testGetLabels() throws Exception
    {
        assertEquals( asList( "FooBar" ), resource.getLabels() );
    }

    @Test
    public void testGetPropertyKeys() throws Exception
    {
        assertEquals( asList( "foo" ), resource.getPropertyKeys() );
    }

    @Test
    public void testGetRelationshipTypes() throws Exception
    {
        assertEquals( asList( "FOO_BAR" ), resource.getRelationshipTypes() );
    }

    @Test
    public void testUserAgent()
    {
        assertEquals( USER_AGENT, resource.getClientInfo().getAgent() );
    }
}
