package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmRepository films;
    private final MongoTemplate mongo;

    //Instancia
    @Autowired
    public FilmService(FilmRepository films, MongoTemplate mongo) {
        this.films = films;
        this.mongo = mongo;
    }

    //devuelve la película con el id correspondiente
    public Optional<Film> get(String id) {
        return films.findById(id);
    }

    //devuelve la lista de películas paginadas
    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> keywords, List<String> genres,
                                    List<String> producers, List<String> crew, List<String> cast, Date releasedate) {
        Pageable request = PageRequest.of(page, size, sort);
        Criteria criteria = Criteria.where("_id").exists(true);
        if (keywords != null) {
            criteria.and("keywords").all(keywords);
        }
        if (genres != null) {
            criteria.and("genres").all(genres);
        }
        if (producers != null) {
            criteria.and("producers.name").all(producers);
        }
        if (crew != null) {
            criteria.and("crew.name").all(crew);
        }
        if (cast != null) {
            criteria.and("cast.name").all(cast);
        }
        if (releasedate != null) {
            if (releasedate.getDay() != null) {
                criteria.and("releaseDate.day").is(releasedate.getDay());
            }
            if (releasedate.getMonth() != null) {
                criteria.and("releaseDate.month").is(releasedate.getMonth());
            }
            if (releasedate.getYear() != null) {
                criteria.and("releaseDate.year").is(releasedate.getYear());
            }
        }
        Query query = Query.query(criteria).with(request);
        List<Film> result = mongo.find(query, Film.class);

        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(PageableExecutionUtils.getPage(result, request,
                    ()->mongo.count(Query.query(criteria), Film.class)));
    }

    //inserta la película
    public Optional<Film> insert(Film film) {
        return Optional.of(films.insert(film));
    }

    //modifica la película
    public Optional<Film> put(Film film) {
        return Optional.of(films.save(film));
    }

    //elimina la película con el id correspondiente
    public void delete(String id) {
        films.deleteById(id);
    }
}
