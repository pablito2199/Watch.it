package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//link al servicio que se encuentra en /users
@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;

    //Instancia
    @Autowired
    public UserController(UserService users) {
        this.users = users;
    }

    //método GET al recuperar un usuario
    //link al servicio en users/{id}, produces lo que devuelve
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //cogemos la variable id del path y la identificamos con el email
    ResponseEntity<User> get(@PathVariable("id") String email) {
        //devolvemos el usuario obtenido
        return ResponseEntity.of(users.get(email));
    }

    //método GET al recuperar usuarios
    //produces lo que devuelve
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    //recogemos todos los usuarios paginando con los requestparam
    ResponseEntity<Page<User>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort/*,
            @RequestBody User user*/
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
        return ResponseEntity.of(users.get(page, size, Sort.by(criteria)));
    }

    //método POST al crear un nuevo usuario
    //consumes, pues necesita los datos del body
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> insert(@RequestBody User user) {
        //debe tener el email, nombre y aniversario obligatoriamente
        if (user.getEmail() != null && user.getName() != null && user.getBirthday() != null) {
            if (checkFriends(user.getFriends()) == 0) {
                if (!users.get(user.getEmail()).isPresent()) {
                    //insertamos el usuario correspondiente
                    users.insert(user);
                    //devolvemos código de error 200 al ir todo bien
                    return ResponseEntity.ok().build();
                } else {
                    //devolvemos código de error 409 al ir haber un conflicto, pues ya existe un usuario con ese correo
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            } else if (checkFriends(user.getFriends()) == 1) {
                //devolvemos código de error 404 al producirse un de búsqueda
                return ResponseEntity.notFound().build();
            } else {
                //devolvemos código de error 409 al ir haber un conflicto, pues ya existe el amigo que se intenta añadir
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            //devolvemos código de error 404 al producirse un error
            return ResponseEntity.notFound().build();
        }
    }

    //método POST al añadir un nuevo amigo
    //consumes, pues necesita los datos del body
    @PostMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> addFriend(@PathVariable("id") String email, @RequestBody User friend) {
        if (users.get(friend.getEmail()).isPresent()) {
            //debe tener el email, nombre y aniversario obligatoriamente
            if (friend.getEmail() != null && friend.getName() != null) {
                if (checkFriend(email, friend) == 0) {
                    //insertamos el usuario correspondiente
                    users.addFriend(email, friend);
                    //devolvemos código de error 200 al ir todo bien
                    return ResponseEntity.ok().build();
                } else if (checkFriend(email, friend) == 1) {
                    //devolvemos código de error 404 al producirse un de búsqueda
                    return ResponseEntity.notFound().build();
                } else {
                    //devolvemos código de error 409 al ir haber un conflicto, pues ya existe el amigo que se intenta añadir
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            } else {
                //devolvemos código de error 404 al producirse un error
                return ResponseEntity.notFound().build();
            }
        } else {
            //devolvemos código de error 404 al producirse un de búsqueda
            return ResponseEntity.notFound().build();
        }
    }

    //método PUT para modificar un usuario
    //link al servicio en users/{id}, consumes, pues necesita los datos del body
    @PutMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el email que modificar, y el body con el objeto
    ResponseEntity<Movie> put(@PathVariable("id") String email, @RequestBody User user) {
        //si el email existe, y si los nuevos email y aniversario coinciden, pues no se pueden modificar, modificamos
        if (users.get(email).isPresent() &&
                users.get(email).get().getBirthday().equals(user.getBirthday()) &&
                users.get(email).get().getEmail().equals(user.getEmail())) {
            if (checkFriends(user.getFriends()) == 0) {
                //modificamos el usuario correspondiente
                users.put(user);
                //devolvemos código de error 200 al ir todo bien
                return ResponseEntity.ok().build();
            } else if (checkFriends(user.getFriends()) == 1) {
                //devolvemos código de error 404 al producirse un de búsqueda
                return ResponseEntity.notFound().build();
            } else {
                //devolvemos código de error 409 al ir haber un conflicto, pues ya existe el amigo que se intenta añadir
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            //devolvemos código de error 404 al producirse un error
            return ResponseEntity.notFound().build();
        }
    }

    //método DELETE para eliminar un usuario
    //link al servicio en users/{id}
    @DeleteMapping(
            path = "{id}"
    )
    //recoge la variable del id, pues necesita buscar el email para eliminar el usuario
    ResponseEntity<User> delete(@PathVariable("id") String email) {
        users.delete(email);
        return ResponseEntity.ok().build();
    }

    //método DELETE para eliminar un usuario
    //link al servicio en users/{id}
    @DeleteMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    //recoge la variable del id, pues necesita buscar el email para eliminar el amigo de un usuario
    //que se encuetra en el @RequestBody
    ResponseEntity<User> deleteFriend(@PathVariable("id") String user1, @RequestBody User user2) {
        //si el amigo existe
        if (friendExists(user1, user2.getEmail())) {
            users.deleteFriend(user1, user2.getEmail());
            //devolvemos código de error 200 al ir todo bien
            return ResponseEntity.ok().build();
        } else {
            //devolvemos código de error 404 al producirse un error
            return ResponseEntity.notFound().build();
        }
    }

    //comprobamos que los amigos cumplan los requisitos necesarios
    private Integer checkFriend(String user, User friend) {
        //comprobamos que este tenga email y nombre
        if (friend.getEmail() == null || friend.getName() == null) {
            return 1;
        }
        //comprobamos que el usuario coincide con uno existente en la base de datos
        if (friend.getEmail().equals(users.get(friend.getEmail()).get().getEmail()) &&
                friend.getName().equals(users.get(friend.getEmail()).get().getName())) {
            //comprobamos si existe algún amigo
            for (User us1 : users.get(user).get().getFriends()) {
                //si existe algún usuario con ese email en amigos, no se introducirá
                if (us1.getEmail().equals(friend.getEmail())) {
                    return 2;
                }
            }
            return 0;
        } else {
            return 1;
        }
    }

    //comprobamos que los amigos cumplan los requisitos necesarios
    private Integer checkFriends(List<User> friends) {
        //para cada amigo introducido
        for (User u : friends) {
            //comprobamos que este tenga email y nombre
            if (u.getEmail() == null || u.getName() == null) {
                return 1;
            }
            //comprobamos que el usuario coincide con uno existente en la base de datos
            if (u.getEmail().equals(users.get(u.getEmail()).get().getEmail()) &&
                    u.getName().equals(users.get(u.getEmail()).get().getName())) {
                //comprobamos si existe algún amigo
                for (User us1 : users.get(u.getEmail()).get().getFriends()) {
                    for (User us2 : u.getFriends()) {
                        //si existe algún usuario con ese email en amigos, no se introducirá
                        if (us1.getEmail().equals(us2.getEmail())) {
                            return 2;
                        }
                    }
                }
                return 0;
            } else {
                return 1;
            }
        }
        return 0;
    }

    //comprobamos que los amigos cumplan los requisitos necesarios
    private boolean friendExists(String user1, String user2) {
        for (User u : users.get(user1).get().getFriends()) {
            if (u.getEmail().equals(user2)) {
                return true;
            }
        }
        return false;
    }
}

