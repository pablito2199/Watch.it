package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        //guardamos el usuario
        Optional<User> user = users.findById(email);
        //si el usuario está presente en la base de datos, pero no tiene amigos, no se muestra el campo friends
        if (user.isPresent() && user.get().getFriends() == null) {
            user.get().setFriends(null);
        }
        //devolvemos el usuario
        return user;
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> getAllUserData(String email) {
        //devolvemos el usuario
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

    //devuelve la lista de usuarios paginados
    public List<String> getFriendsIds(String user) {
        List<String> result = new ArrayList<>();
        //guardamos el usuario que estamos buscando
        Optional<User> objective = this.getAllUserData(user);
        //si el usuario existe en la base de datos
        if (objective.isPresent()) {
            //añadimos la lista de amigos del usuario
            for (User u : objective.get().getFriends()) {
                result.add(u.getEmail());
            }
        }
        //devolvemos la lista de ids de amigos existentes
        return result;
    }

    //inserta el usuario
    public Optional<User> insert(User user) {
        //si tiene algun amigo
        if (user.getFriends() != null) {
            //guardamos la lista de amigos
            List<User> friends = user.getFriends();
            //mostraremos solo email y nombre
            for (User f : friends) {
                f.setFriends(null).setBirthday(null).setCountry(null).setPicture(null);
            }
        }
        //devolvemos el usuario
        return Optional.of(users.insert(user));
    }

    //añade el amigo al usuario
    public Optional<User> addFriend(String email, User user) {
        //si el usuario está presente, añadiremos el amigo
        if (users.findById(email).isPresent()) {
            //buscamos los amigos del usuario
            List<User> friends = users.findById(email).get().getFriends();
            //añadimos el amigo solo con los campos de email y nombre
            friends.add(new User().setEmail(user.getEmail()).setName(user.getName()));
            //guardamos la lista modificada de amigos
            return Optional.of(users.save(users.findById(email).get().setFriends(friends)));
        }
        return Optional.empty();
    }

    //modifica el usuario
    public Optional<User> put(User user) {
        //si tiene algún amigo
        if (user.getFriends() != null) {
            //guardamos la lista de amigos
            List<User> friends = user.getFriends();
            //mostraremos solo email y nombre
            for (User f : friends) {
                f.setFriends(null).setBirthday(null).setCountry(null).setPicture(null);
            }
        }
        return Optional.of(users.save(user));
    }

    //elimina el usuario con el email correspondiente
    public void delete(String email) {
        users.deleteById(email);
    }

    //elimina el amigo del usuario correspondiente
    public void deleteFriend(String user1, String user2) {
        //si el usuario está presente, eliminaremos el amigo
        if (users.findById(user1).isPresent()) {
            List<User> friends = users.findById(user1).get().getFriends();
            //buscamos los amigos del usuario
            for (User f : users.findById(user1).get().getFriends()) {
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
}
