package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PatchMethod {
    private final ObjectMapper mapper;

    //Instancias
    @Autowired
    public PatchMethod(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T patch(T data, List<Map<String, Object>> updates) throws JsonPatchException {
        //transformamos nuestro objeto Film en un objeto JsonNode empleando Jackson
        JsonPatch operations = mapper.convertValue(updates, JsonPatch.class);
        //transformamos nuestro objeto Film en un objeto JsonNode empleando Jackson
        JsonNode json = mapper.convertValue(data, JsonNode.class);
        //aplicamos las operaciones definidas en el objeto JsonPatch sobre el JSON de la película
        JsonNode updatedJson = operations.apply(json);
        //volvemos a transformar el JSON en una instancia de película empleando Jackson
        return (T) mapper.convertValue(updatedJson, data.getClass());
    }
}
