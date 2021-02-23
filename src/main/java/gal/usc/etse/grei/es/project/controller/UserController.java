package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;

    @Autowired
    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> get(@PathVariable("id") String email) {
        return ResponseEntity.of(users.get(email));
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<User>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(users.get(page, size, Sort.by(criteria)));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> insert(@RequestBody User user) {
        users.insert(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(
            path = "{id}"
    )
    ResponseEntity<User> delete(@PathVariable("id") String email)  {
        users.delete(email);
        return ResponseEntity.ok().build();
    }
}

