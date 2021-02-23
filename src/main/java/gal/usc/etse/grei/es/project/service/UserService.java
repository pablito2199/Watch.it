package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;

    @Autowired
    public UserService(UserRepository people) {
        this.users = people;
    }

    public Optional<User> get(String email) {
        return users.findById(email);
    }

    public Optional<Page<User>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAllUsers(request);

       if(result.isEmpty())
            return Optional.empty();

       else return Optional.of(result);
    }

    public void insert(User user) {
        users.insert(user);
    }

    public void delete(String email) {
        users.deleteById(email);
    }
}