package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "friends")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Frienship {
    @Id
    private String id;
    private String user;
    private String friend;
    private Boolean confirmed;
    private Date since;

    public Frienship() {
    }

    public Frienship(String id, String user, String friend, Boolean confirmed, Date since) {
        this.id = id;
        this.user = user;
        this.friend = friend;
        this.confirmed = confirmed;
        this.since = since;
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getFriend() {
        return friend;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public Date getSince() {
        return since;
    }

    public Frienship setId(String id) {
        this.id = id;
        return this;
    }

    public Frienship setUser(String user) {
        this.user = user;
        return this;
    }

    public Frienship setFriend(String friend) {
        this.friend = friend;
        return this;
    }

    public Frienship setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public Frienship setSince(Date since) {
        this.since = since;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frienship frienship = (Frienship) o;
        return Objects.equals(id, frienship.id) && Objects.equals(user, frienship.user) && Objects.equals(friend, frienship.friend) && Objects.equals(confirmed, frienship.confirmed) && Objects.equals(since, frienship.since);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, friend, confirmed, since);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Frienship.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("user='" + user + "'")
                .add("friend='" + friend + "'")
                .add("confirmed=" + confirmed)
                .add("since=" + since)
                .toString();
    }
}
