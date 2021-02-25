package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface MovieRepository extends MongoRepository<Movie, String> {
    //recuperamos todos los campos de las películas puestos a 1
    @Query(value = "{}" /*"{$and :["
            + "?#{ [1] == null ? { '_id' : {$exists : 'true'}} : { '_id' : [1] } },"
            + "?#{ [2] == null ? { '_id' : {$exists : 'true'}} : { '_id' : [2] } },"
            + "?#{ [3] == null ? { '_id' : {$exists : 'true'}} : { '_id' : [3] } },"
            + "?#{ [4] == null ? { '_id' : {$exists : 'true'}} : { '_id' : [4] } },"
            + "]}"*/,
            fields = "{_id: 1, title: 1, overview: 1, genres: 1, releaseDate: 1, resources: 1}")
    Page<Movie> findAllMovies(Pageable request);
}
