package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
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
import java.util.Map;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmRepository films;
    private final MongoTemplate mongo;
    private final PatchMethod patchMethod;

    //Instancias
    @Autowired
    public FilmService(FilmRepository films, MongoTemplate mongo, PatchMethod patchMethod) {
        this.films = films;
        this.mongo = mongo;
        this.patchMethod = patchMethod;
    }

    //devuelve la película con el id correspondiente
    public Optional<Film> get(String id) {
        return films.findById(id);
    }

    //devuelve la lista de películas paginadas
    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> keywords, List<String> genres,
                                    List<String> producers, List<String> crew, List<String> cast, Date releasedate) {
        Pageable request = PageRequest.of(page, size, sort);
        //mostramos aquellas películas con id
        Criteria criteria = Criteria.where("_id").exists(true);
        //si se pasa filtro, se añade a criteria
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
        //completamos la query
        Query query = Query.query(criteria).with(request);
        //buscamos con los filtros, indicando la clase Film, que es lo que busca
        List<Film> result = mongo.find(query, Film.class);

        //solo mostraremos los campos deseados
        for (Film f : result) {
            f.setTagline(null).setCollection(null).setKeywords(null).setProducers(null).setCrew(null)
                    .setCast(null).setBudget(null).setStatus(null).setRuntime(null).setRevenue(null);
        }

        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(PageableExecutionUtils.getPage(result, request,
                    () -> mongo.count(Query.query(criteria), Film.class)));
    }

    //inserta la película
    public Optional<Film> insert(Film film) {
        return Optional.of(films.insert(film));
    }

    //modifica la película
    public Optional<Film> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        //si la película se encuentra presente en la base de datos
        if (this.get(id).isPresent()) {
            //obtenemos la película de la base de datos
            Film film = this.get(id).get();
            //actualizamos los datos con el patch
            film = patchMethod.patch(film, updates);
            //actualizamos en la base de datos
            return Optional.of(films.save(film));
        }
        //devolvemos el objeto vacío
        return Optional.empty();
    }

    //elimina la película con el id correspondiente
    public void delete(String id) {
        films.deleteById(id);
    }
}
