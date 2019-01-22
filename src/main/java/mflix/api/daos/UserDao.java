package mflix.api.daos;

import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import mflix.api.models.Session;
import mflix.api.models.User;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;
    private final MongoCollection<Session> sessionsCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
        sessionsCollection = db.getCollection("sessions", Session.class).withCodecRegistry(pojoCodecRegistry);
    }

    /**
     * Inserts the `user` object in the `users` collection.
     *
     * @param user - User object to be added
     * @return True if successful, throw IncorrectDaoOperation otherwise.
     */
    public boolean addUser(User user) {
        if(usersCollection.find(Filters.eq("email", user.getEmail())).first() != null)
            throw new IncorrectDaoOperation("User Already Exists!");

        usersCollection.withWriteConcern(WriteConcern.MAJORITY);
        usersCollection.insertOne(user);
        return true;
    }

    /**
     * Creates session using userId and jwt token.
     *
     * @param userId - user string identifier
     * @param jwt    - jwt string token
     * @return true if successful
     */
    public boolean createUserSession(String userId, String jwt) {

        if (getUserSession(userId) != null)
            return true;

        Session session = new Session();
        session.setUserId(userId);
        session.setJwt(jwt);
        sessionsCollection.insertOne(session);
        return true;
    }

    /**
     * Returns the User object matching the an email string value.
     *
     * @param email - email string to be matched.
     * @return User object or null.
     */
    public User getUser(String email) {
        return usersCollection.find(Filters.eq("email", email)).first();
    }

    /**
     * Given the userId, returns a Session object.
     *
     * @param userId - user string identifier.
     * @return Session object or null.
     */
    public Session getUserSession(String userId) {
        return sessionsCollection.find(Filters.eq("user_id", userId)).first();
    }

    public boolean deleteUserSessions(String userId) {
        return sessionsCollection.deleteOne(Filters.eq("user_id", userId)).wasAcknowledged();
    }

    /**
     * Removes the user document that match the provided email.
     *
     * @param email - of the user to be deleted.
     * @return true if user successfully removed
     */
    public boolean deleteUser(String email) {
        return deleteUserSessions(email) && usersCollection.deleteOne(Filters.eq("email", email)).wasAcknowledged();
    }

    /**
     * Updates the preferences of an user identified by `email` parameter.
     *
     * @param email           - user to be updated email
     * @param userPreferences - set of preferences that should be stored and replace the existing
     *                        ones. Cannot be set to null value
     * @return true if preferences get update, false in case of null userPreferences or unsuccessful
     * write.
     */
    public boolean updateUserPreferences(String email, Map<String, String> userPreferences) {
        if(userPreferences==null)
            throw new IncorrectDaoOperation("User Preference Cannot Be NULL");

        Bson eqEmail = Filters.eq("email", email);
        User user = usersCollection.find(eqEmail).first();
        if(user == null){
//            throw new IncorrectDaoOperation("User Does Not Exists!");
            return false;
        }

        user = usersCollection.findOneAndUpdate(eqEmail, Updates.set("preferences", userPreferences));
        return user != null;
    }
}
