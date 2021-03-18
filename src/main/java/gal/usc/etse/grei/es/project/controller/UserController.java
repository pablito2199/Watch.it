package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FriendshipService;
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

//link al servicio que se encuentra en /users
@RestController
@RequestMapping("users")
public class UserController {
    private final AssessmentService assessments;
    private final FriendshipService friendships;
    private final UserService users;
    private final LinkRelationProvider relationProvider;

    //Instancias
    @Autowired
    public UserController(AssessmentService assessments, FriendshipService friendships, UserService users, LinkRelationProvider relationProvider) {
        this.assessments = assessments;
        this.friendships = friendships;
        this.users = users;
        this.relationProvider = relationProvider;
    }

    //método GET al recuperar un usuario
    //link al servicio en users/{id}, produces lo que devuelve
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //cogemos la variable id del path y la identificamos con el email
    //solo puede admin, el propio usuario y sus amigos
    //@PreAuthorize("hasRole('ADMIN') or #email == principal or @friendshipService.areFriends(principal, #email)")
    public ResponseEntity<User> get(@PathVariable("id") String email) {
        //recuperamos el usuario obtenido
        Optional<User> result = users.get(email);

        //si no se encuentra el usuario
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(email)
        ).withSelfRel();
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(UserController.class).get(0, 0, sort, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(User.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result.get());
    }

    //método GET al recuperar usuarios
    //produces lo que devuelve
    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recogemos todos los usuarios paginando con los requestparam
    //si está logueado
    //@PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<User>> get(
            //parámetros a continuación de la interrogación para el filtrado
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name
    ) {
        //ordenamos por aniversario
        if (sort.contains("+birthday")) {
            sort.add("+birthday.year");
            sort.add("+birthday.month");
            sort.add("+birthday.day");
            sort.remove("+birthday");
        }
        if (sort.contains("-birthday")) {
            sort.add("-birthday.year");
            sort.add("-birthday.month");
            sort.add("-birthday.day");
            sort.remove("-birthday");
        }

        //ordenamos la lista obtenida
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if (string.startsWith("+")) {
                //ordenamos la lista acendentemente
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                //ordenamos la lista descendentemente
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Optional<Page<User>> result = users.get(page, size, Sort.by(criteria), email, name);

        //si no hay ningún usuario guardado
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found");
        }
        //guardamos los resultados obtenidos
        Page<User> data = result.get();
        //paginamos los datos obtenidos
        Pageable metadata = data.getPageable();

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(page, size, sort, email, name)
        ).withSelfRel();
        Link first = linkTo(
                methodOn(UserController.class).get(metadata.first().getPageNumber(), size, sort, email, name)
        ).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(
                methodOn(UserController.class).get(metadata.next().getPageNumber(), size, sort, email, name)
        ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(
                methodOn(UserController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)
        ).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(
                methodOn(UserController.class).get(data.getTotalPages() - 1, size, sort, email, name)
        ).withRel(IanaLinkRelations.LAST);
        Link one = linkTo(
                methodOn(UserController.class).get(null)
        ).withRel(relationProvider.getItemResourceRelFor(User.class));

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

    //método GET al recuperar valoraciones de un usuario
    //link al servicio en users/assessments, produces lo que devuelve
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //solo puede admin, el propio usuario y sus amigos
    //@PreAuthorize("hasRole('ADMIN') or #user == principal or @friendshipService.areFriends(principal, #user)")
    ResponseEntity<Page<Assessment>> getAssessmentsUser(
            //parámetro a continuación de la interrogación para el filtrado
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @PathVariable("id") String user
    ) {
        //si el usuario no existe
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //recuperamos las valoraciones obtenidas
        Optional<Page<Assessment>> result = assessments.getAssessmentsUser(page, size, user);

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
                methodOn(UserController.class).getAssessmentsUser(page, size, user)
        ).withSelfRel();
        Link first = linkTo(
                methodOn(UserController.class).getAssessmentsUser(metadata.first().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(
                methodOn(UserController.class).getAssessmentsUser(metadata.next().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(
                methodOn(UserController.class).getAssessmentsUser(metadata.previousOrFirst().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(
                methodOn(UserController.class).getAssessmentsUser(data.getTotalPages() - 1, size, user)
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

    //método GET al recuperar un amigo de un usuario
    //link al servicio en users/{user}/friendships/{friend}, produces lo que devuelve
    @GetMapping(
            path = "{user}/friendships/{friendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //solo pueden los usuarios implicados en la relación
    //@PreAuthorize("#user == principal and (@friendshipService.get(#friendship).get().user == principal or @friendshipService.get(#friendship).get().friend == principal)")
    ResponseEntity<Friendship> get(@PathVariable("user") String user, @PathVariable("friendship") String friendship) {
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");
        }
        //si el usuario no es el user o el friend, o no se ha aceptado la amistad todavía
        if ((!user.equals(friendships.get(friendship).get().getUser()) &&
                !user.equals(friendships.get(friendship).get().getFriend()))) {
            //devolvemos código de error 400 al intentar recuperar una amistad que no es suya
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the friendship");
        }
        //si no se ha aceptado la amistad todavía
        if (friendships.get(friendship).get().getConfirmed() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friendship not accepted yet");
        }
        //obtenemos la amistad
        Optional<Friendship> result = friendships.get(friendship);

        //si la amistad no se encuentra en la base de datos
        if (!result.isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");
        }
        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(result.get().getId())
        ).withSelfRel();
        Link all = linkTo(
                methodOn(UserController.class).getFriends(0, 0, result.get().getUser())
        ).withRel(relationProvider.getItemResourceRelFor(Friendship.class));
        Link userLink = linkTo(
                methodOn(UserController.class).get(user)
        ).withSelfRel();
        Link friendLink;
        if (user.equals(result.get().getUser())) {
            friendLink = linkTo(
                    methodOn(UserController.class).get(result.get().getFriend())
            ).withSelfRel();
        } else {
            friendLink = linkTo(
                    methodOn(UserController.class).get(result.get().getUser())
            ).withSelfRel();
        }

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .header(HttpHeaders.LINK, userLink.toString())
                .header(HttpHeaders.LINK, friendLink.toString())
                .body(result.get());
    }

    //método GET al recuperar los amigos de un usuario
    //link al servicio en users/{id}/friendships, produces lo que devuelve
    @GetMapping(
            path = "{id}/friendships",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //si es el propio usuario
    //@PreAuthorize("#user == principal")
    ResponseEntity<Page<String>> getFriends(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @PathVariable("id") String user
    ) {
        //si el usuario no existe
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Optional<Page<String>> result = friendships.getFriends(page, size, user);
        //si la lista está vacía
        if (!result.isPresent()) {
            //devolvemos código 204 al ir todo bien, pero no encontrar amigos
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "User do not have any friends");
        }
        //guardamos los resultados obtenidos
        Page<String> data = result.get();
        //paginamos los datos obtenidos
        Pageable metadata = data.getPageable();

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).getFriends(page, size, user)
        ).withSelfRel();
        Link first = linkTo(
                methodOn(UserController.class).getFriends(metadata.first().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.FIRST);
        Link next = linkTo(
                methodOn(UserController.class).getFriends(metadata.next().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo(
                methodOn(UserController.class).getFriends(metadata.previousOrFirst().getPageNumber(), size, user)
        ).withRel(IanaLinkRelations.PREVIOUS);
        Link last = linkTo(
                methodOn(UserController.class).getFriends(data.getTotalPages() - 1, size, user)
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

    //método POST al crear un nuevo usuario
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //permite crear un usuario a cualquiera
    //@PreAuthorize("permitAll()")
    ResponseEntity<User> insert(@RequestBody @Valid User user) {
        //si el usuario ya existe en la base de datos
        if (users.get(user.getEmail()).isPresent()) {
            //devolvemos código de error 409 al haber un conflicto, pues ya existe un usuario con ese correo
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        //insertamos el usuario
        User result = users.insert(user);

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(result.getEmail())
        ).withSelfRel();
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(UserController.class).get(0, 0, sort, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(User.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result);
    }

    //método POST al crear una nueva amistad
    //link al servicio en users/{id}/friendships, consumes, pues necesita los datos del body
    @PostMapping(
            path = "{id}/friendships",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //solo puede el propio usuario
    //@PreAuthorize("#user == principal")
    ResponseEntity<Friendship> insert(@PathVariable("id") String user, @RequestBody User friend) {
        //si el amigo no se encuentra en la base de datos
        if (!users.get(friend.getEmail()).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found");
        }
        //si el usuario no se encuentra en la base de datos
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //si el campo friend es nulo
        if (friend.getEmail() == null) {
            //devolvemos código de error 400 al intentar añadir un amigos con campos inválidos
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field friend can not be empty");
        }
        if (user.equals(friend.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User can not be his own friend");
        }
        //si la amistad ya existe
        if (friendships.getAllFriends(user).contains(friend.getEmail())) {
            //devolvemos código de error 409 al producirse un conflicto
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friendship already exists");
        }
        //creamos la amistad
        Friendship result = friendships.insert(user, friend.getEmail());

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(result.getId())
        ).withSelfRel();
        Link all = linkTo(
                methodOn(UserController.class).getFriends(0, 0, result.getUser())
        ).withRel(relationProvider.getItemResourceRelFor(Friendship.class));

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result);
    }

    //método PATCH para modificar un usuario
    //link al servicio en users/{id}, consumes, pues necesita los datos del body
    @PatchMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //solo puede el propio usuario
    //@PreAuthorize("#email == principal")
    ResponseEntity<User> patch(@PathVariable("id") String email, @RequestBody List<Map<String, Object>> updates) {
        //si el usuario no está presente en la base de datos
        if (!users.get(email).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //si se trata de modificar el aniversario o el email
        if (updates.get(0).containsValue("replace") &&
                (updates.get(0).containsValue("/email") || updates.get(0).containsValue("/birthday"))) {
            //devolvemos código de error 422 al intentar modificar un usuario con datos inválidos
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not modify the field");
        }
        //si se trata de eliminar el email, nombre o aniversario
        if (updates.get(0).containsValue("remove") &&
                (updates.get(0).containsValue("/email") || updates.get(0).containsValue("/birthday") ||
                        updates.get(0).containsValue("/name"))) {
            //devolvemos código de error 422 al intentar eliminar campos de un usuario inválidos
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You can not remove the field");
        }
        try {
            //modificamos el usuario
            User result = users.patch(email, updates);

            //creamos los enlaces correspondientes
            Link self = linkTo(
                    methodOn(UserController.class).get(result.getEmail())
            ).withSelfRel();
            List<String> sort = new ArrayList<>();
            sort.add("");
            Link all = linkTo(
                    methodOn(UserController.class).get(0, 0, sort, null, null)
            ).withRel(relationProvider.getItemResourceRelFor(User.class));

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

    //método PUT para modificar una amistad
    //recoge la variable del user, pues necesita buscar el usuario que desea modificar una amistad
    //recoge la variable del friendship, pues necesita buscar la amistad que desea modificar
    @PutMapping(
            path = "{user}/friendships/{friendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    //si amigo es el propio usuario
    //@PreAuthorize("#user == principal")
    ResponseEntity<Friendship> put(@PathVariable("user") String user, @PathVariable("friendship") String friendship) {
        //si el usuario no está presente en la base de datos
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");
        }
        //si el usuario no es el friend, o ya se ha aceptado la amistad
        if (!user.equals(friendships.get(friendship).get().getFriend()) ||
                friendships.get(friendship).get().getConfirmed() != null) {
            //devolvemos código de error 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not the friend");
        }
        //si ya se ha aceptado la amistad
        if (friendships.get(friendship).get().getConfirmed() != null) {
            //devolvemos código de error 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friendship already exists");
        }
        //modificamos la amistad
        Friendship result = friendships.put(friendship);

        //creamos los enlaces correspondientes
        Link self = linkTo(
                methodOn(UserController.class).get(result.getId())
        ).withSelfRel();
        Link all = linkTo(
                methodOn(UserController.class).getFriends(0, 0, result.getUser())
        ).withRel(relationProvider.getItemResourceRelFor(Friendship.class));
        Link userLink = linkTo(
                methodOn(UserController.class).get(user)
        ).withSelfRel();
        Link friendLink;
        if (user.equals(result.getUser())) {
            friendLink = linkTo(
                    methodOn(UserController.class).get(result.getFriend())
            ).withSelfRel();
        } else {
            friendLink = linkTo(
                    methodOn(UserController.class).get(result.getUser())
            ).withSelfRel();
        }

        //devolvemos la respuesta de que todo fue bien, con los enlaces en la cabecera, y el cuerpo correspondiente
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .header(HttpHeaders.LINK, userLink.toString())
                .header(HttpHeaders.LINK, friendLink.toString())
                .body(result);
    }

    //método DELETE para eliminar un usuario
    //link al servicio en users/{id}
    @DeleteMapping(
            path = "{id}"
    )
    //recoge la variable del id, pues necesita buscar el email para eliminar el usuario
    //solo puede el propio usuario
    //@PreAuthorize("#email == principal")
    ResponseEntity<User> delete(@PathVariable("id") String email) {
        //si el usuario no existe
        if (!users.get(email).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //eliminamos el usuario
        users.delete(email);
        //eliminamos los comentarios de dicho usuario
        for (Assessment a : assessments.getAll()) {
            if (a.getUser().getEmail().equals(email)) {
                assessments.delete(a.getId());
            }
        }
        //eliminamos las amistades de dicho usuario
        for (Friendship f : friendships.getAll()) {
            if (f.getUser().equals(email)) {
                friendships.delete(f.getId());
            }
        }

        //creamos los enlaces correspondientes
        List<String> sort = new ArrayList<>();
        sort.add("");
        Link all = linkTo(
                methodOn(UserController.class).get(0, 0, sort, null, null)
        ).withRel(relationProvider.getItemResourceRelFor(User.class));

        //devolvemos código de error 204 al ir todo bien
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    //método DELETE para eliminar una amistad
    //link al servicio en users/{id}/friendships, consumes, pues necesita los datos del body
    @DeleteMapping(
            path = "{user}/friendships/{friendship}"
    )
    //recoge la variable del user_id, pues necesita buscar el usuario que quiere eliminar el amigo
    //recoge la variable del id, pues necesita buscar la amistad que desea eliminarse
    //solo puede el propio usuario
    //@PreAuthorize("#user == principal")
    ResponseEntity<Friendship> delete(@PathVariable("user") String user, @PathVariable("friendship") String friendship) {
        //si el usuario no se encuentra en la base de datos
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");
        }
        //si el usuario no es el user o el friend
        if ((!user.equals(friendships.get(friendship).get().getUser()) &&
                !user.equals(friendships.get(friendship).get().getFriend()))) {
            //devolvemos código de error 400 al intentar eliminar una amistad que no es suya
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the friendship");
        }
        //si no se ha aceptado la amistad todavía
        if (friendships.get(friendship).get().getConfirmed() == null) {
            //devolvemos código de error 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friendship not accepted yet");
        }
        //eliminamos la amistad
        friendships.delete(friendship);

        //creamos los enlaces correspondientes
        Link all = linkTo(
                methodOn(UserController.class).getFriends(0, 0, user)
        ).withRel(relationProvider.getItemResourceRelFor(Friendship.class));

        //devolvemos código de error 204 al ir todo bien
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }
}