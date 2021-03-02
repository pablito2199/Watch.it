package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Cast;
import gal.usc.etse.grei.es.project.model.Crew;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Producer;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//link al servicio que se encuentra en /films
@RestController
@RequestMapping("films")
public class FilmController {
    private final FilmService films;

    //Instancia
    @Autowired
    public FilmController(FilmService films) {
        this.films = films;
    }

    //método GET al recuperar una película
    //link al servicio en films/{id}, produces lo que devuelve
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //cogemos la variable id del path y la identificamos con el id
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        //recuperamos la película obtenida
        return ResponseEntity.of(films.get(id));
    }

    //método GET al recuperar películas
    //produces lo que devuelve
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recogemos todas las películas paginando con los requestparam
    ResponseEntity<Page<Film>> get(
            //parámetros a continuación de la interrogación para el filtrado
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @RequestParam(name = "genres", required = false) List<String> genres,
            @RequestParam(name = "producers", required = false) List<Producer> producers,
            @RequestParam(name = "crew", required = false) List<Crew> crew,
            @RequestParam(name = "cast", required = false) List<Cast> cast,
            @RequestParam(name = "day", required = false) Integer day,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        //ordenamos la lista obtenida
        List<Sort.Order> criteria = sort.stream().map(string -> {
            //ordenamos la lista acendentemente
            if (string.startsWith("+")) {
                return Sort.Order.asc(string.substring(1));
                //ordenamos la lista descendentemente
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Date releaseDate = new Date(day, month, year);
        //devolvemos los usuarios obtenidos
        return ResponseEntity.of(films.get(page, size, Sort.by(criteria), releaseDate));
    }

    //método POST al crear una nueva película
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> insert(@RequestBody @Valid Film film) {
        //devolvemos la película insertada
        return ResponseEntity.of(films.insert(film));
    }

    //método PUT para modificar una película
    //link al servicio en films/{id}, consumes, pues necesita los datos del body
    @PutMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    ResponseEntity<Film> put(@PathVariable("id") String id, @RequestBody Film film) {
        //si el id existe, modificamos
        if (films.get(id).isPresent()) {
            //devolvemos la película modificada
            return ResponseEntity.of(films.put(film));
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
    }

    //método DELETE para eliminar una película
    //link al servicio en films/{id}
    @DeleteMapping(
            path = "{id}"
    )
    //recoge la variable del id, pues necesita buscar el id para eliminar la película
    ResponseEntity<Film> delete(@PathVariable("id") String id) {
        //si la película existe, podremos eliminar la película
        if (films.get(id).isPresent()) {
            //eliminamos la película
            films.delete(id);
            //devolvemos código de error 200 al ir todo bien
            return ResponseEntity.ok().build();
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
    }
}
