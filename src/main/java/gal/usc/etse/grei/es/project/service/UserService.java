package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;

    //Instancias
    @Autowired
    public UserService(UserRepository people) {
        this.users = people;
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> get(String email) {
        return users.findById(email);
    }

    //devuelve la lista de usuarios paginados
    public Optional<Page<User>> get(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);
        //el filtro deberá contener lo especificado debido al "CONTAINS"
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //filtrado si se pasa un email o nombre
        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);
        //buscamos los usuarios según los filtros
        Page<User> result = users.findAll(filter, request);
        //debido a que los campos de friends e email no se verán, los ponemos a null
        for (User u : result) {
            u.setFriends(null).setEmail(null);
        }

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //inserta el usuario
    public Optional<User> insert(User user) {
        return Optional.of(users.insert(user));
    }

    //añade el amigo al usuario
    public Optional<User> addFriend(String email, User user) {
        //buscamos los amigos del usuario
        List<User> friends = users.findById(email).get().getFriends();
        //añadimos el amigo solo con los campos de email y nombre
        friends.add(new User().setEmail(user.getEmail()).setName(user.getName()));
        //guardamos la lista modificada de amigos
        return Optional.of(users.save(users.findById(email).get().setFriends(friends)));
    }

    //modifica el usuario
    public Optional<User> put(User user) {
        return Optional.of(users.save(user));
    }

    //elimina el usuario con el email correspondiente
    public void delete(String email) {
        users.deleteById(email);
    }

    //elimina el amigo del usuario correspondiente
    public void deleteFriend(String user1, String user2) {
        //buscamos los amigos del usuario
        List<User> friends = users.findById(user1).get().getFriends();
        for (User f : friends) {
            if (f.getEmail().equals(user2)) {
                //eliminamos el amigo y salimos del bucle
                friends.remove(f);
                break;
            }
        }
        //guardamos la lista modificada de amigos
        users.save(users.findById(user1).get().setFriends(friends));
    }
}
