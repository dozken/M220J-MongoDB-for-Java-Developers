package mflix.finalQuestions;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;
import mflix.api.daos.TicketTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class Final extends TicketTest {


    SslSettings sslSettings;
    ReadPreference readPreference;
    ReadConcern readConcern;
    WriteConcern writeConcern;

    @Before
    public void setUp() throws IOException {
        ConnectionString connectionString = new ConnectionString(getProperty("spring.mongodb.uri"));
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        MongoClient mongoClient = MongoClients.create(settings);

        sslSettings = settings.getSslSettings();
        readPreference = settings.getReadPreference();
        readConcern = settings.getReadConcern();
        writeConcern = settings.getWriteConcern();
    }

    @Test
    public void test() {
//        Assert.assertEquals(sslSettings.isEnabled(), false);
        Assert.assertEquals(readConcern.asDocument().toString(), "{ }");
        Assert.assertEquals(readPreference.toString(), "primary");
//        Assert.assertEquals(sslSettings.isInvalidHostNameAllowed(), true);
//        Assert.assertEquals(writeConcern.asDocument().toString(), "{ w : 1 }");

    }

}