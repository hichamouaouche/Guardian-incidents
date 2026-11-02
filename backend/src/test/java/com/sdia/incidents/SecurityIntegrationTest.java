package com.sdia.incidents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests d'integration de la securite (contexte Spring complet + H2). */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    private void register(String name, String email) throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"Password123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    private long createIncident(String token) throws Exception {
        String body = mvc.perform(post("/api/incidents").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Poste infecte\",\"description\":\"Malware detecte par l'antivirus\",\"category\":\"MALWARE\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    @Test @DisplayName("Inscription : role force a EMPLOYEE meme si ADMIN est envoye")
    void registerForcesEmployeeRole() throws Exception {
        register("Test Role", "role.test@test.ma");
    }

    @Test @DisplayName("401 : acces a /api/** sans JWT")
    void noToken() throws Exception {
        mvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise : token JWT manquant"));
    }

    @Test @DisplayName("401 : JWT invalide")
    void invalidToken() throws Exception {
        mvc.perform(get("/api/profile").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token invalide"));
    }

    @Test @DisplayName("403 : EMPLOYEE sur une route ADMIN")
    void employeeOnAdminRoute() throws Exception {
        register("Emp Admin", "emp.admin@test.ma");
        String token = login("emp.admin@test.ma", "Password123");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("403 : EMPLOYEE sur l'incident d'un autre employe")
    void employeeOnOtherIncident() throws Exception {
        register("Emp A", "emp.a@test.ma");
        register("Emp B", "emp.b@test.ma");
        long id = createIncident(login("emp.b@test.ma", "Password123"));
        mvc.perform(get("/api/incidents/" + id).header("Authorization", "Bearer " + login("emp.a@test.ma", "Password123")))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("409 : email deja utilise")
    void duplicateEmail() throws Exception {
        register("Dup", "dup@test.ma");
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Dup 2\",\"email\":\"dup@test.ma\",\"password\":\"Password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test @DisplayName("400 : incident avec donnees invalides")
    void invalidIncident() throws Exception {
        register("Emp V", "emp.v@test.ma");
        mvc.perform(post("/api/incidents").header("Authorization", "Bearer " + login("emp.v@test.ma", "Password123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test @DisplayName("Cycle complet : affectation, statuts, note, CLOSED bloque")
    void fullLifecycle() throws Exception {
        register("Emp L", "emp.l@test.ma");
        long id = createIncident(login("emp.l@test.ma", "Password123"));
        String admin = login("hichamouaouche@gmail.com", "Admin@2026");
        String tech1 = login("tech1@incidents.ma", "Tech@2026");
        String tech2 = login("tech2@incidents.ma", "Tech@2026");

        JsonNode techs = mapper.readTree(mvc.perform(get("/api/admin/technicians").header("Authorization", "Bearer " + admin))
                .andReturn().getResponse().getContentAsString());
        long tech1Id = 0;
        for (JsonNode t : techs) if (t.get("email").asText().equals("tech1@incidents.ma")) tech1Id = t.get("id").asLong();

        mvc.perform(put("/api/incidents/" + id + "/assign/" + tech1Id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ASSIGNED"));

        // technicien non affecte -> 403
        mvc.perform(put("/api/incidents/" + id + "/status").header("Authorization", "Bearer " + tech2)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/incidents/" + id + "/status").header("Authorization", "Bearer " + tech1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/incidents/" + id + "/status").header("Authorization", "Bearer " + tech1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/incidents/" + id + "/comments").header("Authorization", "Bearer " + tech1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Analyse antivirus effectuee.\"}"))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/incidents/" + id + "/status").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk());
        // CLOSED -> plus de modification
        mvc.perform(put("/api/incidents/" + id + "/status").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/incidents/" + id).header("Authorization", "Bearer " + login("emp.l@test.ma", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history.length()").value(5))
                .andExpect(jsonPath("$.comments.length()").value(1));
    }

    @Test @DisplayName("403 : utilisateur desactive ne peut plus se connecter")
    void disabledUser() throws Exception {
        register("Emp D", "emp.d@test.ma");
        String admin = login("hichamouaouche@gmail.com", "Admin@2026");
        JsonNode users = mapper.readTree(mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + admin))
                .andReturn().getResponse().getContentAsString());
        long uid = 0;
        for (JsonNode u : users) if (u.get("email").asText().equals("emp.d@test.ma")) uid = u.get("id").asLong();
        mvc.perform(put("/api/admin/users/" + uid + "/disable").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"emp.d@test.ma\",\"password\":\"Password123\"}"))
                .andExpect(status().isForbidden());
    }
}
