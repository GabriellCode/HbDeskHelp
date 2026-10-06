package com.hb.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hb.api.model.User;
import com.hb.api.repository.TicketRepository;
import com.hb.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HbDeskHelpSecurityAndFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        // Os usuários padrão (funcionario, dev, chefe) já são criados pelo DataInitializer
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        Map<String, Object> data = mapper.readValue(response, Map.class);
        return (String) data.get("token");
    }

    @Test
    public void testSecurity_UnauthenticatedAccess_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuth_ValidLogin_ShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "funcionario", "password", "123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testAuth_InvalidLogin_ShouldFail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "funcionario", "password", "senhaerrada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testAuthorization_DevCannotCreateUser() throws Exception {
        String devToken = loginAndGetToken("dev", "123");

        // Dev tenta criar usuário
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + devToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "hacker", "password", "123", "role", "DEV"))))
                .andExpect(status().isForbidden()); // Controller joga RuntimeException se nao for Chefe
    }

    @Test
    public void testAuthorization_ChiefCanCreateUser() throws Exception {
        String chiefToken = loginAndGetToken("chefe", "123");

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + chiefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "novouser", "password", "123", "role", "DEV"))))
                .andExpect(status().isOk());
    }

    @Test
    public void testFlow_CreateTicketAndAssign() throws Exception {
        String funcToken = loginAndGetToken("funcionario", "123");
        String devToken = loginAndGetToken("dev", "123");

        // Funcionario cria chamado
        String ticketRes = mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + funcToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("title", "Meu Mouse Quebrou", "description", "Socorro"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERMELHO"))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> ticketData = mapper.readValue(ticketRes, Map.class);
        Integer ticketId = (Integer) ticketData.get("id");

        // Dev assume chamado
        mockMvc.perform(post("/api/tickets/" + ticketId + "/assign")
                .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LARANJA"));

        // Dev conclui chamado
        mockMvc.perform(post("/api/tickets/" + ticketId + "/complete")
                .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AMARELO"));

        // Funcionario confirma chamado
        mockMvc.perform(post("/api/tickets/" + ticketId + "/confirm")
                .header("Authorization", "Bearer " + funcToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERDE"));
    }
}
