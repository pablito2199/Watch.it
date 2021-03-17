package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessmentService {
    private final AssessmentRepository assessments;
    private final FilmService films;
    private final UserService users;
    private final MongoTemplate mongo;
    private final PatchMethod patchMethod;

    //Instancias
    @Autowired
    public AssessmentService(AssessmentRepository assessments, FilmService films, UserService users, MongoTemplate mongo, PatchMethod patchMethod) {
        this.assessments = assessments;
        this.films = films;
        this.users = users;
        this.mongo = mongo;
        this.patchMethod = patchMethod;
    }

    //devuelve la valoración con el id correspondiente
    public Optional<Assessment> get(String id) {
        return assessments.findById(id);
    }

    //devuelve todas las valoraciones
    public List<Assessment> getAll() {
        return assessments.findAll();
    }

    //devuelve las valoraciones de la película correspondiente
    public Optional<List<Assessment>> getAssessments(String film, String user) {
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("film._id").is(film);
        criteria.and("user._id").is(user);
        Query query = Query.query(criteria);
        List<Assessment> result = mongo.find(query, Assessment.class);
        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(result);
    }

    //devuelve las valoraciones de la película correspondiente
    public Optional<Page<Assessment>> getAssessmentsFilm(int page, int size, String film) {
        Pageable request = PageRequest.of(page, size);
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("film._id").is(film);
        Query query = Query.query(criteria).with(request);
        List<Assessment> result = mongo.find(query, Assessment.class);

        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(PageableExecutionUtils.getPage(result, request,
                    () -> mongo.count(Query.query(criteria), Assessment.class)));
    }

    //devuelve las valoraciones del usuario correspondiente
    public Optional<Page<Assessment>> getAssessmentsUser(int page, int size, String user) {
        Pageable request = PageRequest.of(page, size);
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("user._id").is(user);
        Query query = Query.query(criteria);
        List<Assessment> result = mongo.find(query, Assessment.class);

        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(PageableExecutionUtils.getPage(result, request,
                    () -> mongo.count(Query.query(criteria), Assessment.class)));
    }

    //inserta la valoración
    public Assessment insert(Assessment assessment) {
        //si la película está presente en la base de datos, indicamos el título de la película
        if (films.get(assessment.getFilm().getId()).isPresent()) {
            assessment.getFilm().setTitle(films.get(assessment.getFilm().getId()).get().getTitle());
        }
        //si el usuario está presente en la base de datos, indicamos el nombre del usuario
        if (users.get(assessment.getUser().getEmail()).isPresent()) {
            assessment.getUser().setName(users.get(assessment.getUser().getEmail()).get().getName());
        }
        return assessments.insert(assessment);
    }

    //modifica la valoración
    public Assessment patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        //si la valoración se encuentra presente en la base de datos
        if (this.get(id).isPresent()) {
            //obtenemos la valoración de la base de datos
            Assessment assessment = this.get(id).get();
            //actualizamos los datos con el patch
            assessment = patchMethod.patch(assessment, updates);
            //actualizamos en la base de datos
            return assessments.save(assessment);
        }
        return null;
    }

    //elimina la valoración con el id correspondiente
    public void delete(String id) {
        assessments.deleteById(id);
    }
}
