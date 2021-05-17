package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FilmService;
import gal.usc.etse.grei.es.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@SecurityRequirement(name = "JWT")
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
    @Operation(
            operationId = "getOneFilm",
            summary = "Gets a single film details",
            description = "Get the details for a given film. To see the film details " +
                    "you must be logged in."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            )
    })
    //cogemos la variable id del path y la identificamos con el id
    //si está logueado
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Film> get(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String id
    ) {
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
    @Operation(
            operationId = "getAllFilms",
            summary = "Gets all registered films",
            description = "Get the details for the films that are registered. To see the films " +
                    "you must be logged in."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The films registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Films not found",
                    content = @Content
            )
    })
    //recogemos todas las películas paginando con los requestparam
    //si está logueado
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<Film>> get(
            //parámetros a continuación de la interrogación para el filtrado
            @Parameter(name = "Page of the search")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "Size of the search")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "Sort of the search")
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name = "Keywords of the search")
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @Parameter(name = "Genres of the search")
            @RequestParam(name = "genres", required = false) List<String> genres,
            @Parameter(name = "Producers of the search")
            @RequestParam(name = "producers", required = false) List<String> producers,
            @Parameter(name = "Crew of the search")
            @RequestParam(name = "crew", required = false) List<String> crew,
            @Parameter(name = "Casts of the search")
            @RequestParam(name = "cast", required = false) List<String> cast,
            @Parameter(name = "Day of publish of the search")
            @RequestParam(name = "day", required = false) Integer day,
            @Parameter(name = "Month of publish of the search")
            @RequestParam(name = "month", required = false) Integer month,
            @Parameter(name = "Year of publish of the search")
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

    //método GET al recuperar una valoración
    //link al servicio en films/assessments/{id}, produces lo que devuelve
    @GetMapping(
            path = "assessments/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getOneAssessment",
            summary = "Gets an assessment details",
            description = "Get the details for a given assessment. To see the assessment " +
                    "you must be logged in."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content
            )
    })
    //si está logueado
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Assessment> getAssessment(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String assessment
    ) {
        //recuperamos la valoración obtenida
        Optional<Assessment> result = assessments.get(assessment);

        //si la valoración no existe
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(FilmController.class).getAssessment(assessment)
        ).withSelfRel();

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .body(result.get());
    }

    //método GET al recuperar valoraciones de una película
    //link al servicio en films/assessments, produces lo que devuelve
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getAllFilmAssessments",
            summary = "Gets all registered assessments from a film",
            description = "Get the details for the assessments that are registered to a film. To see the " +
                    "assessments you must be the logged in."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessments registered to a film",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessments OR Film not found",
                    content = @Content
            )
    })
    //si está logueado
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<Assessment>> getAssessmentsFilm(
            //parámetro a continuación de la interrogación para el filtrado
            @Parameter(name = "Page of the search")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "Size of the search")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "id", required = true)
            @PathVariable("id") String film
    ) {
        //si la película no existe
        if (!films.get(film).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film " + film + " not found");
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
    @Operation(
            operationId = "insertFilm",
            summary = "Inserts a new film to the database",
            description = "Insert a new film. To insert a new film " +
                    "you must have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film was inserted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            )
    })
    //solo se permite a los administradores
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> insert(
            @Parameter(name = "Film to insert", required = true)
            @RequestBody @Valid Film film
    ) {
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
    @Operation(
            operationId = "insertAssessment",
            summary = "Inserts a new assessment to the database",
            description = "Insert a new assessment. To insert a new assessment " +
                    "you must be logged in and be the requested user of the assessment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment was inserted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User OR film field can not be empty",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User OR film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already wrote an assessment for that film",
                    content = @Content
            )
    })
    //si está logueado
    @PreAuthorize("isAuthenticated() and #assessment.user.email == principal")
    ResponseEntity<Assessment> insertAssessment(
            @Parameter(name = "Assessment to insert", required = true)
            @RequestBody @Valid Assessment assessment
    ) {
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
        Link film = linkTo(
                methodOn(FilmController.class).get(result.getFilm().getId())
        ).withSelfRel();
        Link allFromFilm = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(0, 0, result.getFilm().getId())
        ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, film.toString())
                .header(HttpHeaders.LINK, allFromFilm.toString())
                .body(result);
    }

    //método PATCH para modificar una película
    //link al servicio en films/{id}, consumes, pues necesita los datos del body
    @PatchMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "modifyFilm",
            summary = "Modifies a film from the database",
            description = "Modifies an existing film. To modify a film " +
                    "you must have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film was modified",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Can not remove the title OR id field OR operation can not be applied to the object",
                    content = @Content
            )
    })
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //solo se permite a los administradores
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> patch(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String id,
            @Parameter(name = "Updates to be applied to the film", required = true)
            @RequestBody List<Map<String, Object>> updates
    ) {
        //si la película no existe en la base de datos
        if (!films.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film + " + id + " not found");
        }
        //para cada operación del patch
        for (Map<String, Object> update : updates) {
            //si se intenta eliminar el título o el id
            if (update.containsValue("remove") &&
                    (update.containsValue("/title") || update.containsValue("/id"))) {
                //devolvemos código de error 422 al intentar el eliminar el campo del título o id
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not remove the field");
            }
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
        } catch (Exception e) {
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
    @Operation(
            operationId = "modifyAssessment",
            summary = "Modifies an assessment from the database",
            description = "Modifies an existing assessment. To modify an assessment " +
                    "you must be the request user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment was modified",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Can not modify OR remove films specified OR operation can not be applied to the object",
                    content = @Content
            )
    })
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //solo el propio usuario
    @PreAuthorize("@assessmentService.get(#id).get().user.email == principal")
    ResponseEntity<Assessment> patchAssessment(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String id,
            @Parameter(name = "Updates to be applied to the assessment", required = true)
            @RequestBody List<Map<String, Object>> updates
    ) {
        //si la valoración no está presente en la base de datos
        if (!assessments.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        //para cada operación del patch
        for (Map<String, Object> update : updates) {
            //si se intenta modificar el usuario, la película o el id
            if (update.containsValue("replace") &&
                    (update.containsValue("/film") || update.containsValue("/user") ||
                            update.containsValue("/_id"))) {
                //devolvemos código de error 422 al intentar modificar la película o usuario
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not modify the field");
            }
            //si se intenta eliminar el usuario, la película, la valoración o el id
            if (update.containsValue("remove") &&
                    (update.containsValue("/film") || update.containsValue("/user") ||
                            update.containsValue("/rating") || update.containsValue("/_id"))) {
                //devolvemos código de error 422 al intentar el eliminar el campo de película, usuario, valoración o id
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not remove the field");
            }
        }
        try {
            //modificamos la valoración
            Assessment result = assessments.patch(id, updates);

            //creamos los enlaces correspondientes
            Link self = linkTo(
                    methodOn(FilmController.class).getAssessment(result.getId())
            ).withSelfRel();
            Link allFromFilm = linkTo(
                    methodOn(FilmController.class).getAssessmentsFilm(0, 0, result.getFilm().getId())
            ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
            Link allFromUser = linkTo(
                    methodOn(UserController.class).getAssessmentsUser(0, 0, result.getUser().getEmail())
            ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

            //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, allFromFilm.toString())
                    .header(HttpHeaders.LINK, allFromUser.toString())
                    .body(result);
        } catch (Exception e) {
            //devolvemos un error del tipo 422, pues la operación no se puede aplicar al objeto a modificar
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Operation can not be applied to the object");
        }
    }

    //método DELETE para eliminar una película
    //link al servicio en films/{id}
    @DeleteMapping(
            path = "{id}"
    )
    @Operation(
            operationId = "deleteFilm",
            summary = "Deletes a film",
            description = "Deletes a film from the database. To delete a film you must have " +
                    "admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "The film was deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            )
    })
    //recoge la variable del id, pues necesita buscar el id para eliminar la película
    //solo se permite a los administradores
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> delete(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String id
    ) {
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

        //creamos los enlaces correspondientes
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(FilmController.class).get(0, 0, sort, null, null, null, null,
                        null, null, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(Film.class));

        //devolvemos código de error 204 al ir todo bien
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    //método DELETE para eliminar una valoración
    //link al servicio en films/assessments/{id}
    @DeleteMapping(
            path = "assessments/{id}"
    )
    @Operation(
            operationId = "deleteAssessment",
            summary = "Deletes an assessment",
            description = "Deletes an assessment from the database. To delete the assessment you " +
                    "must have admin permissions or be the requested user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "The assessment was deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Do not have sufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content
            )
    })
    //recoge la variable del id, pues necesita buscar el id para eliminar la valoración
    //solo pueden admin y el propio usuario
    @PreAuthorize("hasRole('ADMIN') or @assessmentService.get(#id).get().user.email == principal")
    ResponseEntity<Assessment> deleteAssessment(
            @Parameter(name = "id", required = true)
            @PathVariable("id") String id
    ) {
        //si la valoración no existe en la base de datos
        if (!assessments.get(id).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        String film = assessments.get(id).get().getFilm().getId();
        String user = assessments.get(id).get().getUser().getEmail();
        //eliminamos la valoración
        assessments.delete(id);

        //creamos los enlaces correspondientes
        Link allFilms = linkTo(
                methodOn(FilmController.class).getAssessmentsFilm(0, 0, film)
        ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
        Link allUsers = linkTo(
                methodOn(UserController.class).getAssessmentsUser(0, 0, user)
        ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, allFilms.toString())
                .header(HttpHeaders.LINK, allUsers.toString())
                .build();
    }
}
