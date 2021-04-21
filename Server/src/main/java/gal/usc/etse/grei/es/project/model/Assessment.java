package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "comments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Assessment {
    @Id
    private String id;
    @NotNull(message = "The rating field can not be empty")
    @Schema(minimum = "1", maximum = "10", example = "8")
    private Integer rating;
    @NotNull(message = "The user field can not be empty")
    private User user;
    @NotNull(message = "The film field can not be empty")
    private Film film;
    @Schema(example = "This film is good.")
    private String comment;

    public Assessment() {
    }

    public Assessment(String id, Integer rating, User user, Film film, String comment) {
        this.id = id;
        this.rating = rating;
        this.user = user;
        this.film = film;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public User getUser() {
        return user;
    }

    public Film getFilm() {
        return film;
    }

    public String getComment() {
        return comment;
    }

    public Assessment setId(String id) {
        this.id = id;
        return this;
    }

    public Assessment setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public Assessment setUser(User user) {
        this.user = user;
        return this;
    }

    public Assessment setFilm(Film film) {
        this.film = film;
        return this;
    }

    public Assessment setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assessment that = (Assessment) o;
        return Objects.equals(id, that.id) && Objects.equals(rating, that.rating) && Objects.equals(user, that.user) && Objects.equals(film, that.film) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, user, film, comment);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Assessment.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("rating=" + rating)
                .add("user=" + user)
                .add("film=" + film)
                .add("comment='" + comment + "'")
                .toString();
    }
}
