package br.univille.chamados.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.univille.chamados.entity.Categoria;
import br.univille.chamados.entity.Cliente;
import br.univille.chamados.entity.Tecnico;
import br.univille.chamados.repository.CategoriaRepository;
import br.univille.chamados.repository.ChamadoRepository;
import br.univille.chamados.repository.ClienteRepository;
import br.univille.chamados.repository.TecnicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ChamadoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    private Long categoriaId;
    private Long clienteId;
    private Long tecnicoId;

    @BeforeEach
    void prepararDados() {
        chamadoRepository.deleteAll();
        tecnicoRepository.deleteAll();
        categoriaRepository.deleteAll();
        clienteRepository.deleteAll();

        categoriaId = categoriaRepository.save(new Categoria("Software", "Sistemas")).getId();
        clienteId = clienteRepository.save(
            new Cliente("Cliente", "cliente-" + System.nanoTime() + "@mail.com", null)).getId();
        tecnicoId = tecnicoRepository.save(
            new Tecnico("Técnico", "tecnico-" + System.nanoTime() + "@mail.com",
                br.univille.chamados.entity.Perfil.TECNICO)).getId();
    }

    @Test
    void deveCriarChamadoEIniciarAberto() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Falha","descricao":"Não inicia","prioridade":"ALTA",
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(clienteId, categoriaId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ABERTO"))
            .andExpect(jsonPath("$.dataAbertura").isNotEmpty());
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"","descricao":"","prioridade":null,
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(clienteId, categoriaId)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404ParaChamadoInexistente() throws Exception {
        mockMvc.perform(get("/chamados/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveAtribuirEFiltrarChamadoPorStatusPrioridadeECliente() throws Exception {
        String chamado = mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Falha","descricao":"Não inicia","prioridade":"ALTA",
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(clienteId, categoriaId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long chamadoId = ((Number) com.jayway.jsonpath.JsonPath.read(chamado, "$.id")).longValue();

        mockMvc.perform(patch("/chamados/{id}/atribuir", chamadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tecnicoId\":%d}".formatted(tecnicoId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tecnicoId").value(tecnicoId));

        mockMvc.perform(get("/chamados")
                .param("status", "ABERTO")
                .param("prioridade", "ALTA")
                .param("clienteId", clienteId.toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void naoDeveAlterarChamadoFechado() throws Exception {
        String chamado = mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Falha","descricao":"Não inicia","prioridade":"ALTA",
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(clienteId, categoriaId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long chamadoId = ((Number) com.jayway.jsonpath.JsonPath.read(chamado, "$.id")).longValue();

        mockMvc.perform(patch("/chamados/{id}/status", chamadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(patch("/chamados/{id}/status", chamadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"FECHADO\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(patch("/chamados/{id}/atribuir", chamadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tecnicoId\":%d}".formatted(tecnicoId)))
            .andExpect(status().isConflict());
    }

    @Test
    void deveCriarComentarioEListarChamadosDoCliente() throws Exception {
        String chamado = mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Falha","descricao":"Não inicia","prioridade":"MEDIA",
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(clienteId, categoriaId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long chamadoId = ((Number) com.jayway.jsonpath.JsonPath.read(chamado, "$.id")).longValue();

        mockMvc.perform(post("/chamados/{id}/comentarios", chamadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Em análise\",\"autor\":\"Técnico\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.texto").value("Em análise"));

        mockMvc.perform(get("/chamados/{id}", chamadoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comentarios", hasSize(1)));

        mockMvc.perform(get("/clientes/{id}/chamados", clienteId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));
    }
}
