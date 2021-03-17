package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

//link al servicio que se encuentra en /films
@RestController
@RequestMapping("films")
public class FilmController {
    private final AssessmentService assessments;
    private final FilmService films;
    private final UserService users;
    private final LinkRelationProvider relationProvider;

    //Instancias
    @Autowired
    public FilmController(AssessmentService assessments, FilmService films, UserService users, LinkRelationProvider relationProvider) {
        this.assessments = assessments;
        this.films = films;
        this.users = users;
        this.relationProvider = relationProvider;
    }

    //método GET al recuperar una película
    //link al servicio en films/{id}, produces lo que devuelve
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //cogemos la variable id del path y la identificamos con el id
    //si está logueado
    //@PreAuthorize("isAuthenticated()")
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        //recuperamos la película indicada
        Optional<Film> result = films.get(id);

        //si no se encuentra la película
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).get(id)
        ).withSelfRel();
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(FilmController.class).get(0, 0, sort, null, null, null, null,
                        null, null, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(Film.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result.get());
    }

    //método GET al recuperar películas
    //produces lo que devuelve
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recogemos todas las películas paginando con los requestparam
    //si está logueado
    //@PreAuthorize("isAuthenticated()")
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
        //ordenamos por fecha de estreno
        if (sort.contains("+releaseDate")) {
            sort.add("+releaseDate.year");
            sort.add("+releaseDate.month");
            sort.add("+releaseDate.day");
            sort.remove("+releaseDate");
        }
        if (sort.contains("-releaseDate")) {
            sort.add("-releaseDate.year");
            sort.add("-releaseDate.month");
            sort.add("-releaseDate.day");
            sort.remove("-releaseDate");
        }

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
        Optional<Page<Film>> result = films.get(page, size, Sort.by(criteria),
                keywords, genres, producers, crew, cast, releaseDate);

        //si no hay ninguna película guardada
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Films not found");
        }
        //guardamos los resultados obtenidos
        Page<Film> data = result.get();
        //paginamos los datos obtenidos
        Pageable metadata = data.getPageable();

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).get(page, size, sort, keywords, genres, producers,
                        crew, cast, day, month, year)
        ).withSelfRel();
        Link first = linkTo(
                methodOn(FilmController.class).get(metadata.first().getPageNumber(), size, sort, keywords,
                        genres, producers, crew, cast, day, month, year)
        ).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(
                methodOn(FilmController.class).get(metadata.next().getPageNumber(), size, sort, keywords,
                        genres, producers, crew, cast, day, month, year)
        ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(
                methodOn(FilmController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, keywords,
                        genres, producers, crew, cast, day, month, year)
        ).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(
                methodOn(FilmController.class).get(data.getTotalPages() - 1, size, sort, keywords,
                        genres, producers, crew, cast, day, month, year)
        ).withRel(IanaLinkRelations.LAST);
        Link one = linkTo(
                methodOn(FilmController.class).get(null)
        ).withRel(relationProvider.getItemResourceRelFor(Film.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, first.toString())
                .header(HttpHeaders.LINK, next.toString())
                .header(HttpHeaders.LINK, previous.toString())
                .header(HttpHeaders.LINK, last.toString())
                .header(HttpHeaders.LINK, one.toString())
                .body(result.get());
    }

    //método GET al recuperar valoraciones de una película
    //link al servicio en films/assessments, produces lo que devuelve
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //si está logueado
    //@PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<Assessment>> getAssessmentsFilm(
            //parámetro a continuación de la interrogación para el filtrado
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @PathVariable("id") String film
    ) {
        //si la película no existe
        if (!films.get(film).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        //recuperamos las valoraciones obtenidas
        Optional<Page<Assessment>> result = assessments.getAssessmentsFilm(page, size, film);

        //si no hay ninguna valoración guardada
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessments not found");
        }
        //guardamos los resultados obtenidos
        Page<Assessment> data = result.get();
        //paginamos los datos obtenidos
        Pageable metadata = data.getPageable();

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(page, size, film)
        ).withSelfRel();
        Link first = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(metadata.first().getPageNumber(), size, film)
        ).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(metadata.next().getPageNumber(), size, film)
        ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(metadata.previousOrFirst().getPageNumber(), size, film)
        ).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(data.getTotalPages() - 1, size, film)
        ).withRel(IanaLinkRelations.LAST);

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, first.toString())
                .header(HttpHeaders.LINK, next.toString())
                .header(HttpHeaders.LINK, previous.toString())
                .header(HttpHeaders.LINK, last.toString())
                .body(result.get());
    }

    //método POST al crear una nueva película
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //solo se permite a los administradores
    //@PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> insert(@RequestBody @Valid Film film) {
        //guardamos la película en la base de datos
        Film result = films.insert(film);

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).get(result.getId())
        ).withSelfRel();
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(FilmController.class).get(0, 0, sort, null, null, null, null,
                        null, null, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(Film.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result);
    }

    //método POST al crear una nueva valoración sobre una película
    //consumes, pues necesita los datos del body
    @PostMapping(
            path = "assessments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //si está logueado
    //@PreAuthorize("isAuthenticated()")
    ResponseEntity<Assessment> insertAssessment(@RequestBody @Valid Assessment assessment) {
        //si no se indica correctamente el email del usuario
        if (assessment.getUser().getEmail() == null) {
            //devolvemos código de error 400 al intentar añadir una valoración con usuario sin email
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User field can not be empty");
        }
        //si no se indica correctamente el id de la película
        if (assessment.getFilm().getId() == null) {
            //devolvemos código de error 400 al intentar añadir una valoración con película sin id
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Film field can not be empty");
        }
        //si la película existe
        if (!films.get(assessment.getFilm().getId()).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        //si el usuario no existe
        if (!users.get(assessment.getUser().getEmail()).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //si el usuario ya ha realizado una valoración de la película
        if (assessments.getAssessments(assessment.getFilm().getId(), assessment.getUser().getEmail()).isPresent()) {
            //devolvemos código de error 409 al intentar añadir una valoración cuando ya se ha insertado una por ese usuario
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already wrote an assessment for that film");
        }
        //insertamos la valoración
        Assessment result = assessments.insert(assessment);

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).get(result.getFilm().getId())
        ).withSelfRel();
        Link all = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(0, 0, result.getFilm().getId())
        ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result);
    }

    //método PUT para modificar una película
    //link al servicio en films/{id}, consumes, pues necesita los datos del body
    @PatchMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //solo se permite a los administradores
    //@PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> patch(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> updates) {
        //si la película no existe en la base de datos
        if (!films.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        //si se intenta eliminar el título o el id
        if (updates.get(0).containsValue("remove") &&
                (updates.get(0).containsValue("/title") || updates.get(0).containsValue("/id"))) {
            //devolvemos código de error 422 al intentar el eliminar el campo del título o id
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not remove the field");
        }
        try {
            //modificamos la película en la base de datos
            Film result = films.patch(id, updates);

            //creamos los enlaces correspondientes
            Link self = linkTo(
                    methodOn(FilmController.class).get(id)
            ).withSelfRel();
            List<String> sort = new ArrayList<>();
            sort.add("");
            Link all = linkTo(
                    methodOn(FilmController.class).get(0, 0, sort, null, null, null, null,
                            null, null, null, null)
            ).withRel(relationProvider.getItemResourceRelFor(Film.class));

            //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result);
        } catch (JsonPatchException e) {
            //devolvemos un error del tipo 422, pues la operación no se puede aplicar al objeto a modificar
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Operation can not be applied to the object");
        }
    }

    //método PATCH para modificar una valoración
    //link al servicio en assessments/{id}, consumes, pues necesita los datos del body
    @PatchMapping(
            path = "assessments/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //solo el propio usuario
    //@PreAuthorize("@assessmentService.get(#id).get().user.email == principal")
    ResponseEntity<Assessment> patchAssessment(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> updates) {
        //si la valoración no está presente en la base de datos
        if (!assessments.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        //si se intenta modificar el usuario, la película o el id
        if (updates.get(0).containsValue("replace") &&
                (updates.get(0).containsValue("/film") || updates.get(0).containsValue("/user") ||
                        updates.get(0).containsValue("/_id"))) {
            //devolvemos código de error 422 al intentar modificar la película o usuario
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not modify the field");
        }
        //si se intenta eliminar el usuario, la película, la valoración o el id
        if (updates.get(0).containsValue("remove") &&
                (updates.get(0).containsValue("/film") || updates.get(0).containsValue("/user") ||
                        updates.get(0).containsValue("/rating") || updates.get(0).containsValue("/_id"))) {
            //devolvemos código de error 422 al intentar el eliminar el campo de película, usuario, valoración o id
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not remove the field");
        }
        try {
            //modificamos la valoración
            Assessment result = assessments.patch(id, updates);

            //creamos los enlaces correspondientes
            Link self = linkTo(
                    methodOn(FilmController.class).get(result.getFilm().getId())
            ).withSelfRel();
            Link allFilms = linkTo(
                    methodOn(FilmController.class).getAssessmentsFilm(0, 0, result.getFilm().getId())
            ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
            Link allUsers = linkTo(
                    methodOn(UserController.class).get(result.getUser().getEmail())
            ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

            //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, allFilms.toString())
                    .header(HttpHeaders.LINK, allUsers.toString())
                    .body(result);
        } catch (JsonPatchException e) {
            //devolvemos un error del tipo 422, pues la operación no se puede aplicar al objeto a modificar
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Operation can not be applied to the object");
        }
    }

    //método DELETE para eliminar una película
    //link al servicio en films/{id}
    @DeleteMapping(
            path = "{id}"
    )
    //recoge la variable del id, pues necesita buscar el id para eliminar la película
    //solo se permite a los administradores
    //@PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> delete(@PathVariable("id") String id) {
        //si la película no existe en la base de datos
        if (!films.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        //eliminamos la película
        films.delete(id);
        //eliminamos los comentarios de dicha película
        for (Assessment a : assessments.getAll()) {
            if (a.getFilm().getId().equals(id)) {
                assessments.delete(a.getId());
            }
        }
        //devolvemos código de error 200 al ir todo bien
        return ResponseEntity.noContent().build();
    }

    //método DELETE para eliminar una valoración
    //link al servicio en films/assessments/{id}
    @DeleteMapping(
            path = "assessments/{id}"
    )
    //recoge la variable del id, pues necesita buscar el id para eliminar la valoración
    //solo pueden admin y el propio usuario
    //@PreAuthorize("hasRole('ADMIN') or @assessmentService.get(#id).get().user.email == principal")
    ResponseEntity<Assessment> deleteAssessment(@PathVariable("id") String id) {
        //si la valoración no existe en la base de datos
        if (!assessments.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        //eliminamos la valoración
        assessments.delete(id);
        //devolvemos código de error 200 al ir todo bien
        return ResponseEntity.noContent().build();
    }
}
