package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;

    //Instancia
    @Autowired
    public UserService(UserRepository people) {
        this.users = people;
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> get(String email) {
        return users.findById(email);
    }

    //devuelve la lista de usuarios paginados
    public Optional<Page<User>> get(int page, int size, Sort sort/*, User user*/) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAllUsers(/*user.getName(), user.getEmail(), */request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //inserta el usuario
    public void insert(User user) {
        users.insert(user);
    }

    //añade el amigo al usuario
    public void addFriend(String email, User user) {
        //buscamos los amigos del usuario
        List<User> friends = users.findById(email).get().getFriends();
        //añadimos el amigo
        friends.add(user);
        //guardamos la lista modificada de amigos
        users.save(users.findById(email).get().setFriends(friends));
    }

    //modifica el usuario
    public void put(User user) {
        users.save(user);
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
