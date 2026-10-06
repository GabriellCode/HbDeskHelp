package com.hb.api.controller;

import com.hb.api.model.Ticket;
import com.hb.api.model.TicketReply;
import com.hb.api.model.User;
import com.hb.api.repository.TicketReplyRepository;
import com.hb.api.repository.TicketRepository;
import com.hb.api.service.CorneliusAiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketReplyRepository replyRepository;
    private final CorneliusAiService aiService;

    public TicketController(TicketRepository ticketRepository, TicketReplyRepository replyRepository, CorneliusAiService aiService) {
        this.ticketRepository = ticketRepository;
        this.replyRepository = replyRepository;
        this.aiService = aiService;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        return (User) request.getAttribute("user");
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        String title = payload.get("title");
        String description = payload.get("description");

        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription(description);
        t.setCreatedBy(user);
        
        // IA define prioridade
        String priority = aiService.evaluatePriority(title, description);
        t.setPriority(priority);

        return ticketRepository.save(t);
    }

    @GetMapping
    public List<Ticket> listTickets(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user.getRole().equals("FUNCIONARIO")) {
            return ticketRepository.findByCreatedById(user.getId());
        }
        return ticketRepository.findAll();
    }

    @PostMapping("/{id}/assign")
    public Ticket assignTicket(@PathVariable Long id, HttpServletRequest request) {
        Ticket t = ticketRepository.findById(id).orElseThrow();
        t.setAssignedTo(getAuthenticatedUser(request));
        t.setStatus("LARANJA"); // Em andamento
        return ticketRepository.save(t);
    }

    @PostMapping("/{id}/reply")
    public TicketReply replyTicket(@PathVariable Long id, @RequestBody Map<String, String> payload, HttpServletRequest request) {
        Ticket t = ticketRepository.findById(id).orElseThrow();
        TicketReply reply = new TicketReply(t, getAuthenticatedUser(request), payload.get("message"));
        return replyRepository.save(reply);
    }

    @GetMapping("/{id}/replies")
    public List<TicketReply> getReplies(@PathVariable Long id) {
        return replyRepository.findByTicketIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/{id}/complete")
    public Ticket completeTicket(@PathVariable Long id) {
        Ticket t = ticketRepository.findById(id).orElseThrow();
        t.setStatus("AMARELO"); // Pendente confirmação
        return ticketRepository.save(t);
    }

    @PostMapping("/{id}/confirm")
    public Ticket confirmTicket(@PathVariable Long id) {
        Ticket t = ticketRepository.findById(id).orElseThrow();
        t.setStatus("VERDE"); // Funcionario confirmou
        return ticketRepository.save(t);
    }

    @PostMapping("/{id}/delete")
    public void deleteTicket(@PathVariable Long id, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if ("CHEFE_TI".equals(user.getRole())) {
            ticketRepository.deleteById(id);
        } else {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
        }
    }
}
