package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Frienship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FriendshipService;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

//link al servicio que se encuentra en /users
@RestController
@RequestMapping("users")
public class UserController {
    private final AssessmentService assessments;
    private final FriendshipService friendships;
    private final UserService users;

    //Instancias
    @Autowired
    public UserController(AssessmentService assessments, FriendshipService friendships, UserService users) {
        this.assessments = assessments;
        this.friendships = friendships;
        this.users = users;
    }

    //método GET al recuperar un usuario
    //link al servicio en users/{id}, produces lo que devuelve
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //cogemos la variable id del path y la identificamos con el email
    @PreAuthorize("hasRole('ADMIN') or #email == principal or @userService.areFriends(#email, principal)")
    public ResponseEntity<User> get(@PathVariable("id") String email) {
        //devolvemos el usuario obtenido
        return ResponseEntity.of(users.get(email));
    }

    //método GET al recuperar usuarios
    //produces lo que devuelve
    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recogemos todos los usuarios paginando con los requestparam
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

        //devolvemos los usuarios obtenidos
        return ResponseEntity.of(users.get(page, size, Sort.by(criteria), email, name));
    }

    //método GET al recuperar valoraciones de un usuario
    //link al servicio en users/assessments, produces lo que devuelve
    @GetMapping(
            path = "assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<List<Assessment>> getAssessmentsUser(
            //parámetro a continuación de la interrogación para el filtrado
            @RequestParam(name = "user") String user
    ) {
        //si el usuario no existe
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //devolvemos las valoraciones obtenidas
        return ResponseEntity.of(assessments.getAssessmentsUser(user));
    }

    //método GET al recuperar los amigos de un usuario
    //link al servicio en users/{user}/friendships/{friend}, produces lo que devuelve
    @GetMapping(
            path = "{user}/friendships/{friendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Frienship> get(@PathVariable("user") String user, @PathVariable("friendship") String friendship) {
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si el usuario no es el user o el friend, o no se ha aceptado la amistad todavía
        if ((!user.equals(friendships.get(friendship).get().getUser()) &&
                !user.equals(friendships.get(friendship).get().getFriend())) ||
                friendships.get(friendship).get().getConfirmed() == null) {
            //devolvemos código de error 400 al intentar recuperar una amistad que no es suya
            return ResponseEntity.badRequest().build();
        }
        //devolvemos la amistad obtenida
        return ResponseEntity.of(friendships.get(friendship));
    }

    //método GET al recuperar los amigos de un usuario
    //link al servicio en users/{id}/friendships, produces lo que devuelve
    @GetMapping(
            path = "{id}/friendships",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<List<String>> getFriends(@PathVariable("id") String user) {
        //si el usuario no existe
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        Optional<List<String>> result = Optional.of(friendships.getFriends(user));
        //si la lista está vacía
        if (result.get().size() == 0) {
            //devolvemos código de error 204 al ir todo bien
            return ResponseEntity.noContent().build();
        }
        //devolvemos los amigos obtenidos
        return ResponseEntity.of(result);
    }

    //método POST al crear un nuevo usuario
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> insert(@RequestBody @Valid User user) {
        //si el usuario ya existe en la base de datos
        if (users.get(user.getEmail()).isPresent()) {
            //devolvemos código de error 409 al haber un conflicto, pues ya existe un usuario con ese correo
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        //devolvemos el usuario insertado
        return ResponseEntity.of(users.insert(user));
    }

    //método POST al crear una nueva amistad
    //link al servicio en users/{id}/friendships, consumes, pues necesita los datos del body
    @PostMapping(
            path = "{id}/friendships",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Frienship> insert(@PathVariable("id") String user, @RequestBody User friend) {
        //si el amigo o el usuario no se encuentran en la base de datos
        if (!users.get(friend.getEmail()).isPresent() || !users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si el campo friend es nulo, o se intenta añadir a si mismo como amigo
        if (friend.getEmail() == null || user.equals(friend.getEmail())) {
            //devolvemos código de error 400 al intentar añadir un amigos con campos inválidos
            return ResponseEntity.badRequest().build();
        }
        //si la amistad ya existe
        if (friendships.getFriends(user).contains(friend.getEmail())) {
            //devolvemos código de error 409 al producirse un conflicto
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        //devolvemos la amistad creada
        return ResponseEntity.of(friendships.insert(user, friend.getEmail()));
    }

    //método PATCH para modificar una película
    //link al servicio en users/{id}, consumes, pues necesita los datos del body
    @PatchMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    ResponseEntity<User> patch(@PathVariable("id") String email, @RequestBody List<Map<String, Object>> updates) throws JsonPatchException {
        //si el usuario no está presente en la base de datos
        if (!users.get(email).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si se trata de modificar el aniversario o el email
        if (updates.get(0).containsValue("replace") &&
                (updates.get(0).containsValue("/email") || updates.get(0).containsValue("/birthday"))) {
            //devolvemos código de error 400 al intentar modificar un usuario con datos inválidos
            return ResponseEntity.badRequest().build();
        }
        //si se trata de eliminar el email, nombre o aniversario
        if (updates.get(0).containsValue("remove") &&
                (updates.get(0).containsValue("/email") || updates.get(0).containsValue("/birthday") ||
                        updates.get(0).containsValue("/name"))) {
            //devolvemos código de error 400 al intentar modificar un usuario con datos inválidos
            return ResponseEntity.badRequest().build();
        }
        //devolvemos el usuario modificado
        return ResponseEntity.of(users.patch(email, updates));
    }

    //método PUT para modificar una amistad
    //recoge la variable del user, pues necesita buscar el usuario que desea modificar una amistad
    //recoge la variable del friendship, pues necesita buscar la amistad que desea modificar
    @PutMapping(
            path = "{user}/friendships/{friendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el id que modificar, y el body con el objeto
    ResponseEntity<Frienship> put(@PathVariable("user") String user, @PathVariable("friendship") String friendship)  {
        //si el usuario no está presente en la base de datos
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si el usuario no es el friend, o ya se ha aceptado la amistad
        if (!user.equals(friendships.get(friendship).get().getFriend()) ||
                friendships.get(friendship).get().getConfirmed() != null) {
            //devolvemos código de error 400 al intentar eliminar una amistad que no es suya
            return ResponseEntity.badRequest().build();
        }
        //devolvemos la amistad modificada
        return ResponseEntity.of(friendships.put(friendship));
    }

    //método DELETE para eliminar un usuario
    //link al servicio en users/{id}
    @DeleteMapping(
            path = "{id}"
    )
    //recoge la variable del id, pues necesita buscar el email para eliminar el usuario
    ResponseEntity<User> delete(@PathVariable("id") String email) {
        //si el usuario existe, podremos eliminar el usuario
        if (!users.get(email).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //eliminamos el usuario
        users.delete(email);
        //devolvemos código de error 204 al ir todo bien
        return ResponseEntity.noContent().build();
    }

    //método DELETE para eliminar una amistad
    //link al servicio en users/{id}/friendships, consumes, pues necesita los datos del body
    @DeleteMapping(
            path = "{user}/friendships/{friendship}"
    )
    //recoge la variable del user_id, pues necesita buscar el usuario que quiere eliminar el amigo
    //recoge la variable del id, pues necesita buscar la amistad que desea eliminarse
    ResponseEntity<Frienship> delete(@PathVariable("user") String user, @PathVariable("friendship") String friendship) {
        //si el usuario no se encuentra en la base de datos
        if (!users.get(user).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si la amistad no se encuentra en la base de datos
        if (!friendships.get(friendship).isPresent()) {
            //devolvemos código de error 404 al producirse un error de búsqueda
            return ResponseEntity.notFound().build();
        }
        //si el usuario no es el user o el friend, o no se ha aceptado la amistad todavía
        if ((!user.equals(friendships.get(friendship).get().getUser()) &&
                !user.equals(friendships.get(friendship).get().getFriend())) ||
                friendships.get(friendship).get().getConfirmed() == null) {
            //devolvemos código de error 400 al intentar eliminar una amistad que no es suya
            return ResponseEntity.badRequest().build();
        }
        //eliminamos la amistad
        friendships.delete(friendship);
        //devolvemos código de error 204 al ir todo bien
        return ResponseEntity.noContent().build();
    }
}