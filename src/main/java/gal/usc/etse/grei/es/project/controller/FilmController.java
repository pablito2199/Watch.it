package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import gal.usc.etse.grei.es.project.service.UserService;
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
    private final AssessmentService assessments;
    private final FilmService films;
    private final UserService users;

    //Instancias
    @Autowired
    public FilmController(AssessmentService assessments, FilmService films, UserService users) {
        this.assessments = assessments;
        this.films = films;
        this.users = users;
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
            @RequestParam(name = "producers", required = false) List<String> producers,
            @RequestParam(name = "crew", required = false) List<String> crew,
            @RequestParam(name = "cast", required = false) List<String> cast,
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
        return ResponseEntity.of(films.get(page, size, Sort.by(criteria),
                keywords, genres, producers, crew, cast, releaseDate));
    }

    //método GET al recuperar valoraciones de una película
    //link al servicio en films/assessments, produces lo que devuelve
    @GetMapping(
            path = "assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<List<Assessment>> getAssessmentsFilm(
            //parámetro a continuación de la interrogación para el filtrado
            @RequestParam(name = "film") String film
    ) {
        //si la película existe, permitimos obtener sus valoraciones
        if (films.get(film).isPresent()) {
            //devolvemos las valoraciones obtenidos
            return ResponseEntity.of(assessments.getAssessmentsFilm(film));
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
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

    //método POST al crear una nueva valoración sobre una película
    //consumes, pues necesita los datos del body
    @PostMapping(
            path = "assessments",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> insertAssessment(@RequestBody @Valid Assessment assessment) {
        //Si introducimos el id de la película y el email del usuario, permitimos introducir la valoración
        if (assessment.getFilm().getId() != null && assessment.getUser().getEmail() != null) {
            //si la película y el usuario existen en la base de datos, permitimos introducir la valoración
            if (films.get(assessment.getFilm().getId()).isPresent() &&
                    users.get(assessment.getUser().getEmail()).isPresent()) {
                //devolvemos la valoración insertada
                return ResponseEntity.of(assessments.insert(assessment));
            } else {
                //devolvemos código de error 404 al producirse un error de búsqueda
                return ResponseEntity.notFound().build();
            }
        } else {
            //devolvemos código de error 400 al intentar añadir una valoración sin película o usuario sin email
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
            //devolvemos la película modificada
            return ResponseEntity.of(films.put(film));
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
    }

    //método PUT para modificar una valoración
    //link al servicio en films/assessments/{id}, consumes, pues necesita los datos del body
    @PutMapping(
            path = "assessments/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    ResponseEntity<Assessment> putAssessment(@PathVariable("id") String id, @RequestBody Assessment assessment) {
        //si el id existe, modificamos
        if (assessments.get(id).isPresent()) {
            //Si introducimos el id de la película y el email del usuario, permitimos introducir la valoración
            if (assessment.getFilm().getId() != null && assessment.getUser().getEmail() != null) {
                //si la película y el usuario existen en la base de datos, permitimos introducir la valoración
                if (films.get(assessment.getFilm().getId()).isPresent() &&
                        users.get(assessment.getUser().getEmail()).isPresent()) {
                    //devolvemos la valoración insertada
                    return ResponseEntity.of(assessments.put(id, assessment));
                } else {
                    //devolvemos código de error 404 al producirse un error de búsqueda
                    return ResponseEntity.notFound().build();
                }
            } else {
                //devolvemos código de error 400 al intentar añadir una valoración sin película o usuario sin email
                return ResponseEntity.badRequest().build();
            }
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
            return ResponseEntity.noContent().build();
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
    }

    //método DELETE para eliminar una valoración
    //link al servicio en films/assessments/{id}
    @DeleteMapping(
            path = "assessments/{id}"
    )
    //recoge la variable del id, pues necesita buscar el id para eliminar la valoración
    ResponseEntity<Assessment> deleteAssessment(@PathVariable("id") String id) {
        //si la valoración existe, podremos eliminar la valoración
        if (assessments.get(id).isPresent()) {
            //eliminamos la valoración
            assessments.delete(id);
            //devolvemos código de error 200 al ir todo bien
            return ResponseEntity.noContent().build();
        } else {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
    }
}
