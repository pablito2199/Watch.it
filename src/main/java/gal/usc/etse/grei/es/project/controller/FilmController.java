package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    //recogemos todas las peículas paginando con los requestparam
    ResponseEntity<Page<Film>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
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

        //devolvemos los usuarios obtenidos
        return ResponseEntity.of(films.get(page, size, Sort.by(criteria)));
    }

    //método POST al crear una nueva película
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> insert(@RequestBody Film film) {
        //debe tener el título obligatoriamente
        if (film.getTitle() != null) {
            //insertamos la película correspondiente
            films.insert(film);
            //devolvemos código de error 200 al ir todo bien
            return ResponseEntity.ok().build();
        } else {
            //devolvemos código de error 400 al intentar añadir una película sin título
            return ResponseEntity.badRequest().build();
        }
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
            films.put(film);
            //devolvemos código de error 200 al ir todo bien
            return ResponseEntity.ok().build();
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
