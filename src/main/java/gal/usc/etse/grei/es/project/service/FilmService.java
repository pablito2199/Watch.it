package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmRepository films;

    //Instancia
    @Autowired
    public FilmService(FilmRepository films) {
        this.films = films;
    }

    //devuelve la película con el id correspondiente
    public Optional<Film> get(String id) {
        return films.findById(id);
    }

    //devuelve la lista de películas paginadas
    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> keywords, List<String> genres,
                                    List<String> producers, List<String> crew, List<String> cast, Date releasedate) {
        Pageable request = PageRequest.of(page, size, sort);
        //el filtro deberá contener lo especificado debido al "CONTAINS" de los Strings
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //buscamos las películas según los filtros
        Example<Film> filter = Example.of(new Film().setReleaseDate(releasedate), matcher);
        Page<Film> result = films.findAll(filter, request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
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
